package com.chry.browser.bookmark.provider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.config.ImageConfig;

public class FileTreeLabelProvider extends LabelProvider {
	public String getText( Object element ) {
		BookMark bookmark = (BookMark)element;
		return bookmark.getName();
	}

	public Image getImage( Object element ) {
		BookMark bookmark = (BookMark)element;
		if(bookmark.isFolder()) {
			return ImageConfig.getFolderIcon();
		}
		else{
			return ImageConfig.getFileIcon();
		}
	}

}
