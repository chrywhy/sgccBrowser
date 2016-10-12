package com.chry.browser.bookmark.logic.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.chry.browser.bookmark.BookMark;

public class AllowOnlyFoldersFilter extends ViewerFilter
{
	public boolean select( Viewer viewer, Object parent, Object element ){
		BookMark bookmark = (BookMark)element;
		return (bookmark.isFolder());
	}
}
