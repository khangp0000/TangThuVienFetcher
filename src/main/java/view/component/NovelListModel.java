package view.component;

import java.io.IOException;
import java.util.List;

import javax.swing.AbstractListModel;

import controller.NovelSearcher;
import controller.NovelSearcher.NovelSearchResult;

public class NovelListModel extends AbstractListModel<NovelSearchResult> {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 1094186871213393432L;

	private List<NovelSearchResult> list;
	private String current;

	public NovelListModel() {
		this("");
	}

	public NovelListModel(String str) {
		try {
			current = str;
			list = NovelSearcher.search(str);
		} catch (IOException e) {
			list = null;
		}
	}

	@Override
	public NovelSearchResult getElementAt(int arg0) {
		return list == null ? null : list.get(arg0);
	}

	@Override
	public int getSize() {
		return list == null ? 0 : list.size();
	}

	public String getCurrentSearch() {
		return current;
	}
}
