package userInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import controller.ChaptersFetcher;
import controller.EpubBuilder;
import controller.TTVTitlePage;

public class EpubMaker {
	private static Options options;
	static {
		options = new Options();

		options.addOption(Option.builder("u")
				.longOpt("url")
				.desc("Url of tangthuvien novel to fetch (https://truyen.tangthuvien.vn/doc-truyen/novel-name)")
				.hasArg()
				.argName("URL")
				.required()
				.build());

		options.addOption(Option.builder("f")
				.longOpt("file")
				.desc("The file to output. Default using url file name .epub")
				.hasArg()
				.argName("FILE")
				.build());

		options.addOption(Option.builder("y")
				.longOpt("no-YAML")
				.desc("Disable YAML metadata")
				.build());

		options.addOption(Option.builder("i")
				.longOpt("no-cover-image")
				.desc("Disable cover image")
				.build());

		options.addOption(Option.builder("s")
				.longOpt("start")
				.desc("Starting chapter number, default 1")
				.hasArg()
				.argName("START")
				.build());

		options.addOption(Option.builder("e")
				.longOpt("e")
				.desc("Stopping chapter number, if larger than maximum limit to maximum")
				.hasArg()
				.argName("END")
				.build());

		options.addOption(Option.builder("c")
				.longOpt("start")
				.desc("Number of concurrent connection, default 1")
				.hasArg()
				.argName("CONNECTION")
				.build());

		options.addOption(Option.builder("h")
				.longOpt("no-table-of-content")
				.desc("Disable table of content")
				.build());

		options.addOption(Option.builder("t")
				.longOpt("template")
				.desc("Template file for pandoc to use")
				.hasArg()
				.argName("TEMPLATE FILE")
				.build());
	}

	public static String coverImage = null;
	public static String template = null;
	public static String PANDOC = "pandoc";
	public boolean enableTableOfContent = true;

	public static void main(String[] args) throws IOException, ParseException {
		CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (MissingOptionException e) {
			HelpFormatter helpFormat = new HelpFormatter();
			helpFormat.printHelp("java userInterface.Epubmaker", "Make epub novel from tangthuvien.vn\n\n", options,
					"\nPlease report issue at:\n     khangp0000@gmail.com", true);
			return;
		}
		
		generateEpub(cmd);
	}

	private static File fetchChapterInTemp(CommandLine cmd, StringBuilder suffix) throws IOException {
		int start = 1;
		int end = Integer.MAX_VALUE;
		int connection = 1;
		TTVTitlePage book;
		File outputFolder = Files.createTempDirectory("EpubMaker").toFile();
		boolean titleFile = true;
		boolean imageFile = true;

		FileUtils.forceDeleteOnExit(outputFolder);

		String urlStr = cmd.getOptionValue("u");
		try {
			book = new TTVTitlePage(urlStr);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to connect to " + urlStr, e);
		}

		if (cmd.hasOption("s")) {
			try {
				start = Integer.parseInt(cmd.getOptionValue("s"));
				if (start < 1) {
					start = 1;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid start argument", e);
			}
			
			suffix.append("-s-" + start);
		}

		if (cmd.hasOption("e")) {
			try {
				end = Integer.parseInt(cmd.getOptionValue("e"));
				if (end > book.getChapterNum()) {
					end = book.getChapterNum();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid end argument", e);
			}
			
			suffix.append("-e-" + end);
		}

		if (start > end) {
			throw new IllegalArgumentException("Start must be larger or equal to end");
		}

		if (cmd.hasOption("c")) {
			try {
				connection = Integer.parseInt(cmd.getOptionValue("c"));
				if (connection < 1) {
					connection = 1;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid start argument", e);
			}
		}

		if (cmd.hasOption("y")) {
			titleFile = false;
		}

		if (cmd.hasOption("i")) {
			imageFile = false;
		}

		new ChaptersFetcher(book, start, end, connection, titleFile, imageFile, outputFolder).createFiles();

		return outputFolder;
	}

	private static void generateEpub(CommandLine cmd) throws IOException {
		File contentFolder;
		File template;
		boolean enableTableOfContent = true;
		File output;

		StringBuilder builder = new StringBuilder();
		contentFolder = fetchChapterInTemp(cmd, builder);
		
		String file = cmd.getOptionValue("f");
		if (file != null) {
			output = new File(file);
		} else {
			String[] tokens = cmd.getOptionValue("u")
					.split("/");
			output = new File(tokens[tokens.length - 1] + builder.toString() + ".epub");
		}

		String templateStr = cmd.getOptionValue("t");
		if (templateStr != null) {
			template = new File(templateStr);
		} else {
			template = createTempTemplateFile();
		}

		if (cmd.hasOption("h")) {
			enableTableOfContent = false;
		}	

		try {
			EpubBuilder.buildEpubFromMarkdownFolder(contentFolder, template, enableTableOfContent, output);
		} catch (Exception e) {
			FileUtils.forceDelete(output);
			throw e;
		}
	}
	
	public static File createTempTemplateFile() throws IOException {
		URL templateURI = EpubMaker.class.getClassLoader()
				.getResource("MyTemplate.epub3");
		File template = File.createTempFile(FilenameUtils.getBaseName(templateURI.getFile()),
				'.' + FilenameUtils.getExtension(templateURI.getFile()));
		IOUtils.copy(templateURI.openStream(), FileUtils.openOutputStream(template));
		FileUtils.forceDeleteOnExit(template);
		return template;
	}
}
