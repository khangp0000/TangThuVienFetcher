package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

public class EpubBuilder {
	public static String PANDOC = "pandoc";

	public static void buildEpubFromMarkdownFolder(File contentFolder, File css, boolean enableTableOfContent,
			File output) {

		if (!contentFolder.isDirectory()) {
			throw new IllegalArgumentException("contentFolder must be a valid direcory");
		}

		StringBuilder commandBuilder = new StringBuilder();

		commandBuilder.append(PANDOC);

		commandBuilder.append(" -f markdown -t epub ");

		if (enableTableOfContent) {
			commandBuilder.append(" --toc ");
		}

		if (css != null) {
			commandBuilder.append("--template=" + css.getAbsolutePath());
		}

		File cover = new File(contentFolder.getAbsolutePath()
				.toString() + "/cover.png");
		if (cover.isFile()) {
			commandBuilder.append(" --epub-cover-image=" + cover.getName());
		}

		commandBuilder.append(" -o ");
		commandBuilder.append(output.getAbsolutePath());

		for (File f : contentFolder.listFiles((dir, name) -> name.toLowerCase()
				.endsWith(".md"))) {
			commandBuilder.append(" " + f.getName());
		}

		int status;

		String command = commandBuilder.toString();
		try {
			System.out.println("Executing: " + command);
			Process process = Runtime.getRuntime()
					.exec(command, null, contentFolder);
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			status = process.waitFor();
		} catch (InterruptedException ex) {
			throw new RuntimeException("Could not execute: " + command, ex);
		} catch (IOException ex) {
			throw new UncheckedIOException("Could not execute. Maybe pandoc is not in PATH?: " + command, ex);
		}

		if (status != 0) {
			throw new RuntimeException(
					"Conversion failed with status code: " + status + ". Command executed: " + command);
		}
	}
}
