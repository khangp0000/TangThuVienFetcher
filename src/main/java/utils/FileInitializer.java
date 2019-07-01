package utils;

import java.io.File;
import java.io.IOException;

public class FileInitializer {
	public static File folderInit(String dirStr) {
		File dir = new File(dirStr);
		if (dir.exists() && !dir.isDirectory()) {
			throw new IllegalArgumentException("Invalid folder: " + dirStr);
		}

		if (!dir.exists()) {
			dir.mkdirs();
		}

		return dir;
	}

	public static File fileInit(String fileStr) throws IOException {
		File file = new File(fileStr);
		if (file.exists() && file.isDirectory()) {
			throw new IllegalArgumentException("Invalid file, is directory: " + file);
		}

		if (!file.exists()) {
			file.createNewFile();
		}

		return file;
	}
}
