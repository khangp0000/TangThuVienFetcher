package view.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.NovelSearcher.NovelSearchResult;
import utils.StringNormalizer;

public class NovelSearchTextField extends JTextField implements FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1958439925607542056L;

	private final String hint;
	private final JList<NovelSearchResult> list;
	private final Timer updateList;
	private FutureTask<NovelListModel> future;
	private boolean showingHint;

	public static final int DELAY = 500;

	private class updateListAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!showingHint) {
				try {
					NovelListModel model = future.get();
					list.setModel(model);
				} catch (InterruptedException | ExecutionException e1) {
					e1.printStackTrace();
				} catch (CancellationException e2) {					
				}
			}
			updateList.stop();
		}

	}

	public NovelSearchTextField(final String hint, final JList<NovelSearchResult> list) {
		super(hint);
		this.hint = hint;
		this.list = list;
		this.showingHint = true;
		future = new FutureTask<>(() -> new NovelListModel());
		this.updateList = new Timer(DELAY, new updateListAction());

		this.getDocument()
				.addDocumentListener(new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						updateAction();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						updateAction();
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						updateAction();
					}
					
					private void updateAction() {
						String text = StringNormalizer.NormalizeToASCII(getText());
						future.cancel(true);
						if (!((NovelListModel) list.getModel()).getCurrentSearch()
								.equals(text)) {
							future = new FutureTask<>(() -> new NovelListModel(text));
							new Thread(future).start();
							updateList.restart();
						}
					}
				});
		super.addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (this.getText()
				.isEmpty()) {
			super.setText("");
			showingHint = false;
		}
	}
	@Override
	public void focusLost(FocusEvent e) {
		if (this.getText()
				.isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
}