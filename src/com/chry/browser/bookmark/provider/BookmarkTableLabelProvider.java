package com.chry.browser.bookmark.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.config.ImageConfig;

public class BookmarkTableLabelProvider implements ITableLabelProvider {

	public String getColumnText( Object element, int column_index ) {
		BookMark bookmark = (BookMark)element;
		if( column_index == 0 ) {
			return bookmark.getName();
		}
		return "";
	}

	public void addListener( ILabelProviderListener ilabelproviderlistener ){
	}

	public void dispose() {
	}

	public boolean isLabelProperty( Object obj, String s ) {
		return false;
	}

	public void removeListener( ILabelProviderListener ilabelproviderlistener ) {
	}

	public Image getColumnImage( Object element, int column_index ) {
		BookMark bookmark = (BookMark)element;
		if( column_index != 0 ) {
			return null;
		}
		if(bookmark.isFolder()) {
			return ImageConfig.getFolderIcon();
		}
		else {
			return ImageConfig.getIcon(bookmark.getUrl());
		}
	}
}
