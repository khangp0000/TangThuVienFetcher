package userInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import controller.ChaptersFetcher;
import controller.TTVTitlePage;
import utils.FileInitializer;

public class FetcherMain {
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
				.longOpt("folder")
				.desc("The folder to output files. Default using url file name")
				.hasArg()
				.argName("FOLDER")
				.build());

		options.addOption(Option.builder("y")
				.longOpt("no-YAML")
				.desc("Disable ouput title.md containing YAML  metadata")
				.build());

		options.addOption(Option.builder("i")
				.longOpt("no-cover-image")
				.desc("Disable ouput cover.png containing cover image")
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
	}

	public static void main(String[] args) throws ParseException, FileNotFoundException, IOException  {
		CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (MissingOptionException e) {
			HelpFormatter helpFormat = new HelpFormatter();
			helpFormat.printHelp("java userInterface.FetcherMain", "Fetch novel from tangthuvien.vn\n\n", options,
					"\nPlease report issue at:\n     khangp0000@gmail.com", true);
			return;
		}
		
		ChaptersFetcher fetcher = argHandler(cmd);
		if (fetcher != null) {
			fetcher.createFiles();
		}
	}

	private static ChaptersFetcher argHandler(CommandLine cmd) {
		int start = 1;
		int end = Integer.MAX_VALUE;
		int connection = 1;
		TTVTitlePage book;
		File outputFolder;
		boolean titleFile = true;
		boolean imageFile = true;

		String urlStr = cmd.getOptionValue("u");
		try {
			book = new TTVTitlePage(urlStr);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to connect to " + urlStr, e);
		}

		String folder = cmd.getOptionValue("f");
		if (folder != null) {
			outputFolder = FileInitializer.folderInit(folder);
		} else {
			String[] tokens = urlStr.split("/");
			outputFolder = FileInitializer.folderInit(tokens[tokens.length - 1]);
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
		}

		if (cmd.hasOption("e")) {
			try {
				end = Integer.parseInt(cmd.getOptionValue("e"));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid end argument", e);
			}
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

		return new ChaptersFetcher(book, start, end, connection, titleFile, imageFile, outputFolder);
	}
}
