package com.chry.browser.bookmark.action;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.bookmark.util.FileUtil;
import com.chry.browser.page.BookPage;

public class CreateNewFolderAction extends Action
{
	private BookPage _bookPage;
	private static final String ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_TITLE = "Create new folder";
	private static final String ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_INFO = "Please input folder's name";
	private static final String ACTION_CREATE_NEW_FOLDER_INFO_FAILED = "Create new folder operation failed!";
	private static final String ACTION_CREATE_NEW_FOLDER_INFO_FOLDER_NAME_EXIST = "The folder has existed!";
	private static final String ACTION_CREATE_NEW_FOLDER_DEFAULT_NAME = "new folder";

	public CreateNewFolderAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText("新建文件夹@Ctrl+N" );
	}

	public void run()
	{
		IStructuredSelection selection = _bookPage.getTableSelection();
		File file_path = null;
		if( selection.isEmpty() )
		{
			selection = ( IStructuredSelection ) _bookPage.getTreeViewer().getSelection();
			file_path = ( File ) selection.getFirstElement();
			if( file_path == null )
			{
				file_path = _bookPage.getCurrentRoot();
			}
		}
		else
		{
			file_path = ( ( File ) selection.getFirstElement() ).getParentFile();
		}

		// TODO need write a class InputValidator implements IInputValidator
		InputDialog inputDlg = new InputDialog( _bookPage.getShell(), ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_TITLE,
				ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_INFO, ACTION_CREATE_NEW_FOLDER_DEFAULT_NAME, null );

		if( inputDlg.open() == InputDialog.OK )
		{
			String folderName = inputDlg.getValue();
			File newFolder = new File( file_path.getAbsolutePath() + File.separator + folderName );

			try
			{
				if( FileUtil.isFileExist( newFolder, file_path ) )
				{
					throw new Exception( ACTION_CREATE_NEW_FOLDER_INFO_FOLDER_NAME_EXIST );
				}
				if( newFolder.mkdir() )
				{
					_bookPage.refresh();
					StructuredSelection new_selection = new StructuredSelection( newFolder );
					//window.getTreeViewer().setSelection( new_selection );
					_bookPage.getTreeViewer().expandToLevel( new_selection, 1 );
				}
				else
				{
					throw new Exception( ACTION_CREATE_NEW_FOLDER_INFO_FAILED );
				}
			}
			catch( Exception e )
			{
				MessageBox msgBox = new MessageBox(_bookPage.getShell());
				msgBox.setMessage( e.getMessage() );
				msgBox.open();
			}
		}

	}
}
