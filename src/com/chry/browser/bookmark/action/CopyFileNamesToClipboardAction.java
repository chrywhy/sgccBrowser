package com.chry.browser.bookmark.action;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class CopyFileNamesToClipboardAction extends Action {
	private BookPage _bookPage;

	public CopyFileNamesToClipboardAction(BookPage bookPage){
		_bookPage = bookPage;
		setText("复制" );
//		setText("复制@Ctrl+C" );
	}

	public void run() {
		IStructuredSelection selection = _bookPage.getTableSelection();
		if(selection.isEmpty()) {
			return;
		}
		
		List<BookMark> copyBookmarks = new LinkedList<BookMark>();
		BookMark.setClipBookmars(copyBookmarks);
		
		for(Iterator i = selection.iterator(); i.hasNext();){
			BookMark bookmark = (BookMark) i.next();
			BookMark.addClipBookmars(bookmark);
		}
	}
}
