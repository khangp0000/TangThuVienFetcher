package controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class TTVTitlePage {
	Document novel;
	Elements chapterList;

	public TTVTitlePage(String url) throws IOException {
		novel = Jsoup.connect(url).get();
		String story_id = novel.getElementById("story_id_hidden").attr("value");
		Document chapterListPage = Jsoup.connect("https://truyen.tangthuvien.vn/story/chapters?story_id=" + story_id)
				.get();
		chapterList = chapterListPage.getElementsByAttribute("href");
	}

	public String getNovelName() {
		return novel.getElementsByClass("book-info").first().wholeText().split("\n")[1].trim();
	}

	public String getNovelAuthor() {
		return novel.getElementsByClass("book-info").first().getElementsByClass("blue").first().wholeText();
	}

	public Stream<String> getTranslator() {
		return novel.getElementsByClass("book-state").first().getElementsByClass("detail").first()
				.getElementsByAttribute("href").stream().map(c -> c.wholeText().trim());
	}

	public String getNovelIntro() {
		return novel.getElementsByClass("book-info").first().wholeText().split("\n")[1].trim();
	}

	public int getChapterNum() {
		return chapterList.size();
	}

	public String getChapterName(int i) {
		return chapterList.get(i).attr("title");
	}

	public String getChapterContent(int i) throws IOException, InterruptedException {
		Document content;
		int j = 0;
		while (true) {
			try {
				content = Jsoup.connect(chapterList.get(i).attr("href")).get();
				return content.getElementsByClass("box-chap").first().wholeText().trim().replaceAll("\\n\\s+", "\n\n");
			} catch (IOException e) {
				if (j == 40) {
					throw e;
				} else {
					j++;
					Thread.sleep(250);
				}
			}
		}
	}
	
	public BufferedImage getCover() throws IOException {
		URL imgUrl = new URL(novel.getElementById("bookImg").select("img").first().attr("src"));
		return ImageIO.read(imgUrl);
	}
}
