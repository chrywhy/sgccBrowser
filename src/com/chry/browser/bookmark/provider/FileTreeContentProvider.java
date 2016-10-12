package com.chry.browser.bookmark.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.chry.browser.bookmark.BookMark;

public class FileTreeContentProvider implements ITreeContentProvider {
	public Object[] getChildren(Object element) {
		BookMark bookmark = (BookMark)element;
		return bookmark.getChildren().toArray();
	}

	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	public boolean hasChildren( Object element ) {
		return getChildren( element ).length > 0;
	}

	public Object getParent(Object element) {
		BookMark bookmark = (BookMark)element;
		BookMark parent = null;
		try {
			parent = bookmark.getParent();
			
		} catch(ClassCastException cce) {
			cce.printStackTrace();
		}
		return parent;
	}

	public void dispose() {
	}

	public void inputChanged( Viewer viewer, Object old_input, Object new_input ) {
	}
}
