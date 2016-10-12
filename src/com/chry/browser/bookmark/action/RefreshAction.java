package com.chry.browser.bookmark.action;

import org.eclipse.jface.action.Action;

import com.chry.browser.page.BookPage;

public class RefreshAction extends Action {
	BookPage _bookPage;

	public RefreshAction( BookPage bookPage ) {
		_bookPage = _bookPage;
		setToolTipText( "Refresh jExplorer" );
		setText( "&Refresh@F5" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/refresh.gif" ) ) );
	}

	public void run() {
		_bookPage.refresh();
	}
}
