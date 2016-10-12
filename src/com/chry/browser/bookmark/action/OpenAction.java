package com.chry.browser.bookmark.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class OpenAction extends Action implements ISelectionChangedListener, IDoubleClickListener {
	BookPage _bookPage;
	public OpenAction( BookPage bookPage ) {
		_bookPage = bookPage;		setText("打开...@Alt+O" );
	}

	public void run()
	{
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.size() != 1 ) {
			return;
		}
		BookMark selectedItem = (BookMark)selection.getFirstElement();
		if( selectedItem.isFolder()) {
			//TODO
		}
		else if(selectedItem.isFolder()){
			_bookPage.openFolder(selectedItem);
		}
	}

	public void selectionChanged( SelectionChangedEvent event ) {
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.size() != 1 ) {
			setEnabled( false );
			setToolTipText( getToolTipText() + " (Only enabled when exactly one item is selected)" );
			return;
		}
		BookMark bookmark = (BookMark) selection.getFirstElement();
		if( bookmark.isUrl()){
			setEnabled( true );
			setText( "打开网页");
		}
		else {
			setEnabled( true );
			setText( "打开文件夹 ");
		}
	}

	public void doubleClick( DoubleClickEvent event ) {
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.size() != 1 ) {
			return;
		}
		BookMark selectedItem = (BookMark)selection.getFirstElement();
		if(selectedItem.isUrl()){
		}
		else {
			_bookPage.openFolder(selectedItem );
		}
	}

}
