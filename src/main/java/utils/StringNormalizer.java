package utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class StringNormalizer {
	public static String NormalizeToASCII(String value) {
		return Normalizer.normalize(value, Form.NFD)
				.replaceAll("đ", "d")
				.replaceAll("Đ", "D")
				.replace("\u00a0", " ")
				.replaceAll("[^\\p{ASCII}]", "");
	}

	public static String NormalizeToUrlFriendly(String value) {
		return Normalizer.normalize(value, Form.NFD)
				.replaceAll("đ", "d")
				.replaceAll("Đ", "D")
				.replace("\u00a0", " ")
				.replaceAll("\\s+", "-")
				.replaceAll("[^-a-zA-Z0-9]", "");
	}
}
