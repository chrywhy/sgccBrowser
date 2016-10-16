package com.chry.browser.bookmark.action;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class PasteAction extends Action {
	private BookPage _bookPage;

	public PasteAction( BookPage bookPage ) {
		_bookPage = bookPage;
		setText( "粘贴" );
//		setText( "粘贴@Ctrl+V" );
	}

	public void run(){
		List<BookMark> sourceBookmarks = (List<BookMark>)BookMark.getClipBookmars();
		if(sourceBookmarks==null || sourceBookmarks.isEmpty()){
			return;
		}

		IStructuredSelection selection = _bookPage.getTableSelection();		
		// if the selection in tableViewer is empty, choose selection from treeViewer
		if( selection.isEmpty() ) {
			selection = ( IStructuredSelection ) _bookPage.getTreeViewer().getSelection();
		}

		BookMark selectedBookmark = (BookMark)selection.getFirstElement();
		BookMark targetFolder = null;
		// get the target folder which file will be pasted in
		if(selectedBookmark.isUrl()){
			targetFolder = selectedBookmark.getParent();
		} else {
			targetFolder = selectedBookmark;
		}

		// check whether the same name file exist in target folder
		for(BookMark bookmark : sourceBookmarks ) {
			if(targetFolder.hasChild(bookmark.getName())) {
				MessageBox msgBox = new MessageBox( _bookPage.getShell(), SWT.OK);
				msgBox.setMessage( "\"" + bookmark.getName() + "\" 已经存在 ！" );
				msgBox.open();
				return;
			}
			if (!BookMark.copyChild(targetFolder, bookmark)) {
				MessageBox msgBox = new MessageBox( _bookPage.getShell(), SWT.OK );
				msgBox.setMessage( "\"" + bookmark.getName() + "\" 不能复制到自己的下级文件夹" );
				msgBox.open();
				return;
			}
		}
		_bookPage.refresh();
		BookMark.save();
	}
}
