package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import utils.FileInitializer;
import utils.StringNormalizer;

public class ChaptersFetcher {
	protected int start;
	protected int end;
	protected int connection;
	protected TTVTitlePage book;
	protected File outputFolder;
	protected boolean titleFile;
	protected boolean imageFile;

	protected CompletionService<Exception> service;

	protected ChaptersFetcher(TTVTitlePage book, int start, int end, int connection, boolean titleFile,
			boolean imageFile) throws IOException {
		this(book, start, end, connection, titleFile, imageFile, Files.createTempDirectory("ChaptersFetcher")
				.toFile());
		FileUtils.forceDeleteOnExit(outputFolder);
	}

	public ChaptersFetcher(TTVTitlePage book, int start, int end, int connection, boolean titleFile, boolean imageFile,
			File outputFolder) {
		service = new ExecutorCompletionService<>(new ForkJoinPool());
		this.book = book;
		this.start = start;
		this.end = end;
		this.connection = connection;
		this.outputFolder = outputFolder;
		this.titleFile = titleFile;
		this.imageFile = imageFile;
	}

	public static void createTitleFile(File file, TTVTitlePage book) throws FileNotFoundException, IOException {
		StringBuilder YAML = new StringBuilder();
		YAML.append("---\n");
		YAML.append("title: " + book.getNovelName() + '\n');
		YAML.append("author: " + book.getNovelAuthor() + '\n');
		YAML.append("uploader:\n");
		Stream<String> uploaderList = book.getUploader();
		uploaderList.forEach(t -> {
			YAML.append(" - " + t + '\n');
		});
		YAML.append("lang: vi\n");
		YAML.append("...");

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false),
				StandardCharsets.UTF_8)) {
			writer.write(YAML.toString());
		}
	}

	public void createFiles() throws  IOException  {
		if (book.getChapterNum() < end) {
			end = book.getChapterNum();
		}

		if (titleFile) {
			createTitleFile(FileInitializer.fileInit(outputFolder.getAbsolutePath()
					.replace("\\", "/") + "/title.md"), book);
		}

		if (imageFile) {
			try {
				createCoverImage(FileInitializer.fileInit(outputFolder.getAbsolutePath()
						.replace("\\", "/") + "/cover.png"), book);
			} catch (Exception e) {
				System.out.println("Failed fetching cover image, cover.png will not be created.");
				e.printStackTrace();
			}
		}

		int j = 0;
		for (int i = start; i <= end; i++) {
			if (j == connection) {
				waitATask();
			} else {
				j++;
			}
			service.submit(new GetChapter(this, i));
		}

		while (j > 0) {
			waitATask();
			j--;
		}
	}

	public void createChapterFile(File file, int chapter)
			throws FileNotFoundException, IOException, InterruptedException {
		String chapterName = book.getChapterName(chapter);

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false),
				StandardCharsets.UTF_8)) {
			String content = book.getChapterContent(chapter);
			if (content.isEmpty()) {
				throw new IOException("Empty content: " + StringNormalizer.NormalizeToASCII(chapterName));
			}
			writer.write("# " + book.getChapterName(chapter) + " #\n");
			writer.write(content);
			writer.write('\n');
		}
	}

	public static void createCoverImage(File file, TTVTitlePage book) throws IOException {
		ImageIO.write(book.getCover(), "png", file);
	}

	private void waitATask() {
		Future<Exception> f;
		try {
			f = service.take();
		} catch (InterruptedException e1) {
			throw new IllegalStateException("Something is terribly wrong!!!");
		}
		if (f == null) {
			throw new IllegalStateException("Something is wrong!!!");
		}
		try {
			Exception e = f.get();
			if (e != null) {
				e.printStackTrace();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static class GetChapter implements Callable<Exception> {
		int chapter;
		ChaptersFetcher fetcher;

		public GetChapter(ChaptersFetcher fetcher, int chapter) {
			this.chapter = chapter;
			this.fetcher = fetcher;
		}

		@Override
		public Exception call() {
			String chapterName = fetcher.book.getChapterName(chapter - 1);
			try {
				fetcher.createChapterFile(FileInitializer.fileInit(fetcher.outputFolder.getAbsolutePath()
						.replace("\\", "/") + "/" + String.format("%05d", chapter) + "."
						+ StringNormalizer.NormalizeToUrlFriendly(chapterName) + ".md"), chapter - 1);
			} catch (Exception e) {
				return e;
			}
			System.out.println(
					"Chapter " + chapter + " fetch succeed: " + StringNormalizer.NormalizeToASCII(chapterName));
			return null;
		}
	}
}
