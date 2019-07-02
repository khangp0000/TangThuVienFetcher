package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class NovelSearcher {
	public static class NovelSearchResult {
		private final String name;
		private final String url;

		public NovelSearchResult(JSONObject input) throws Exception {
			if (!input.getString("type").equals("story")) {
				throw new Exception();
			}
			if (input.getInt("story_type") == 0) {
				url = "https://truyen.tangthuvien.vn/doc-truyen/" + input.getString("url").trim();
			} else {
				url = "https://ngontinh.tangthuvien.vn/doc-truyen/" + input.getString("url").trim();
			}

			name = input.getString("name").trim();
		}

		@Override
		public String toString() {
			return name;
		}
		
		public String getName() {
			return name;
		}
		
		public String getURL() {
			return url;
		}
	}
	
	public static List<NovelSearchResult> search(String str) throws IOException {
		JSONArray list = new JSONArray(
				Jsoup.connect("https://ngontinh.tangthuvien.vn/tim-kiem?term=" + str).ignoreContentType(true).get().text());
		List<NovelSearchResult> results = new ArrayList<>();
		list.forEach(r -> {
			try {
				results.add(new NovelSearchResult((JSONObject) r));
			} catch (Exception e) {
			}
		});
		return results;
	}
}
