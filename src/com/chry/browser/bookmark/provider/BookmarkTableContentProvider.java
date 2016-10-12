package com.chry.browser.bookmark.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.chry.browser.bookmark.BookMark;

public class BookmarkTableContentProvider implements IStructuredContentProvider
{
	public Object[] getElements( Object element ) {		
		BookMark bookmark = (BookMark) element;
		Object[] kids = null;
		kids = bookmark.getChildren().toArray();
		return kids == null ? new Object[ 0 ] : kids;
	}

	public void dispose() {
	}

	public void inputChanged( Viewer viewer, Object old_object, Object new_object ) {
	}
}
