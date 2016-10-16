package com.chry.browser.bookmark.action;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class DeleteAction extends Action implements ISelectionChangedListener
{
	private static final String ACTION_DELETE_ERROR_FOLDER_NOT_EMPTY = "书签文件夹非空， 不能删除！";
	private static final String ACTION_DELETE_ERROR_FAILED = "删除失败！";
	private BookPage _bookPage;

	public DeleteAction( BookPage bookPage )
	{
		_bookPage = bookPage;
//		setToolTipText( "Delete Files" );
		setText( "删除" );
//		setText( "剪切@Ctrl+X" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/delete.gif" ) ) );
	}

	public void run()
	{
		IStructuredSelection selection = _bookPage.getTableSelection();
		if(selection.isEmpty()) {
			return;
		}
		BookMark bookmark = (BookMark)selection.getFirstElement();
		MessageBox messageBox = new MessageBox( _bookPage.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO );
		messageBox.setText( "Delete" );

		if(selection.size() == 1) {
			// if select one file
			messageBox.setMessage( "你确定要删除 \"" + bookmark.getName() + "\" ?" );
		}
		else {
			// if select multifile
			messageBox.setMessage( "你确定要删除这 " + selection.size() + "个书签吗 ?" );
		}

		int response = messageBox.open();

		if(response == SWT.YES) {
			BookMark parent = bookmark.getParent();
			List<BookMark> children = parent.getChildren();			
			for( Iterator i = selection.iterator(); i.hasNext(); ) {
				try {
					bookmark = (BookMark)i.next();
					if( bookmark.isFolder() && bookmark.getChildren().size() != 0 ) {
						throw new Exception( ACTION_DELETE_ERROR_FOLDER_NOT_EMPTY );
					}
					parent.removeChild(bookmark);
					_bookPage.removeBookBar(bookmark);
					BookMark.save();
				} catch( Exception e ) {
					MessageBox msgBox = new MessageBox(_bookPage.getShell());
					msgBox.setMessage( e.getMessage() );
					msgBox.open();
				}
			}
			_bookPage.refresh();
		}
	}

	public void selectionChanged( SelectionChangedEvent event ) {
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.size() == 0 ) {
			setEnabled( false );
			return;
		}
		else {
			setEnabled( true );
			return;
		}
	}
}
