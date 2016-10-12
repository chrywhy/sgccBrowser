package com.chry.browser.bookmark.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.bookmark.BookPageConstant;
import com.chry.browser.bookmark.util.FileUtil;
import com.chry.browser.page.BookPage;
import com.chry.util.swt.SwtUtil;

public class PasteAction extends Action
{
	private BookPage _bookPage;

	private static final int FILE_IO_BUFFER_SIZE = BookPageConstant.FILE_IO_BUFFER_SIZE;

	public PasteAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		// setEnabled( false );
//		setToolTipText( "Paste" );
		setText( "粘贴@Ctrl+V" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/paste.gif" ) ) );
	}

	public void run()
	{
		Clipboard clipboard = SwtUtil.getClipboard();
		TextTransfer text_transfer = TextTransfer.getInstance();

		String filesStr = ( String ) clipboard.getContents( text_transfer );

		if( "".equals( filesStr ) || filesStr == null )
		{
			return;
		}

		String[] sourceFiles = filesStr.split( BookPageConstant.FILE_COPY_SEPARATOR );

		IStructuredSelection selection = _bookPage.getTableSelection();
		
		// if the selection in tableViewer is empty, choose selection from treeViewer
		if( selection.isEmpty() )
		{
			selection = ( IStructuredSelection ) _bookPage.getTreeViewer().getSelection();
		}

		File selected_file = ( File ) selection.getFirstElement();
		File target_folder = null;
		// get the target folder which file will be pasted in
		if( selected_file.isFile() )
		{
			target_folder = selected_file.getParentFile();
		}
		else
		{
			target_folder = selected_file;
		}

		// check whether the same name file exist in target folder
		for( String str : sourceFiles )
		{
			File source_file = new File( str );

			//if( !source_file.exists() )
			//{
			//	return;
			//}
			//TODO �ݲ���Ŀ¼�����κβ���
			if( source_file.isDirectory() )
			{
				continue;
			}

			if( FileUtil.isFileExist( source_file, target_folder ) )
			{
				MessageBox msgBox = new MessageBox( _bookPage.getShell(), SWT.YES | SWT.NO );
				msgBox.setMessage( "\"" + source_file.getName() + "\" has existed, cover it?" );
				if( SWT.NO == msgBox.open() )
				{
					//skip this file
					continue;
				}
			}

			try
			{
				_bookPage.setStatus( "copy " + source_file.getPath() + " to " + target_folder.getPath() + "..." );
				FileUtil.doPaste( source_file, target_folder, FILE_IO_BUFFER_SIZE );
				//if cut then delete source target
				if( _bookPage.isCut() )
				{
					source_file.delete();
				}
			}
			catch( FileNotFoundException e )
			{
				MessageBox msgBox = new MessageBox( _bookPage.getShell(), SWT.YES );
				msgBox.setMessage( e.getMessage() );
				msgBox.open();
				e.printStackTrace();
			}
			catch( IOException e )
			{
				MessageBox msgBox = new MessageBox( _bookPage.getShell(), SWT.YES );
				msgBox.setMessage( e.getMessage() );
				msgBox.open();
				e.printStackTrace();
			}
			
		}
		_bookPage.refresh();
		
	}
}
