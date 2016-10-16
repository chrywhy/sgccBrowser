package com.chry.browser.bookmark.action;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class CutAction extends Action
{
	BookPage _bookPage;

	public CutAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText( "剪切" );
//		setText( "剪切@Ctrl+X" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/cut.gif" ) ) );
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
			BookMark parent = bookmark.getParent();
			BookMark.addClipBookmars(bookmark);
			parent.removeChild(bookmark);
			_bookPage.removeBookBar(bookmark);
		}
		BookMark.save();
	}
}
