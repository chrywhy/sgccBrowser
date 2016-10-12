package com.chry.browser.bookmark.action;

import java.io.File;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.page.BookPage;

public class DeleteAction extends Action implements ISelectionChangedListener
{
	private static final String ACTION_DELETE_ERROR_FOLDER_NOT_EMPTY = "The folder is not empty, can not delete it!";
	private static final String ACTION_DELETE_ERROR_FAILED = "Delete file failed!";
	private BookPage _bookPage;

	public DeleteAction( BookPage bookPage )
	{
		_bookPage = bookPage;
//		setToolTipText( "Delete Files" );
		setText( "&删除@Delete" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/delete.gif" ) ) );
	}

	public void run()
	{
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.isEmpty() )
		{
			return;
		}

		File selected_file = ( File ) selection.getFirstElement();
		MessageBox messageBox = new MessageBox( _bookPage.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO );
		messageBox.setText( "Delete" );

		if( selection.size() == 1 )
		{
			// if select one file
			messageBox.setMessage( "Do you really want to delete \"" + selected_file.getName() + "\" ?" );
		}
		else
		{
			// if select multifile
			messageBox.setMessage( "Do you really want to delete these " + selection.size() + " files ?" );
		}

		int response = messageBox.open();

		if( response == SWT.YES )
		{
			for( Iterator i = selection.iterator(); i.hasNext(); )
			{
				try
				{
					File file = ( File ) i.next();
					if( file.isDirectory() && file.listFiles().length != 0 ) 
					{
						//the folder is not empty
						throw new Exception( ACTION_DELETE_ERROR_FOLDER_NOT_EMPTY );
					}
					
					if( !file.delete() )
					{
						throw new Exception( ACTION_DELETE_ERROR_FAILED);
					}
				}
				catch( Exception e )
				{
					MessageBox msgBox = new MessageBox(_bookPage.getShell());
					msgBox.setMessage( e.getMessage() );
					msgBox.open();
				}
			}
			_bookPage.refresh();
		}
	}

	public void selectionChanged( SelectionChangedEvent event )
	{
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.size() == 0 )
		{
			setEnabled( false );
			return;
		}
		else
		{
			setEnabled( true );
			return;
		}

	}
}
