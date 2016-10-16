package com.chry.browser.bookmark.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class CreateNewFolderAction extends Action {
	private BookPage _bookPage;
	private static final String ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_TITLE = "创建新文件夹";
	private static final String ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_INFO = "文件夹名字";
	private static final String ACTION_CREATE_NEW_FOLDER_INFO_FAILED = "创建文件夹失败！";
	private static final String ACTION_CREATE_NEW_FOLDER_INFO_FOLDER_NAME_EXIST = "文件夹已经存在！";
	private static final String ACTION_CREATE_NEW_FOLDER_DEFAULT_NAME = "新文件夹";

	public CreateNewFolderAction( BookPage bookPage ) {
		_bookPage = bookPage;
		setText("新建文件夹" );
//		setText("新建文件夹@Ctrl+N" );
	}

	public void run() {
		IStructuredSelection selection = _bookPage.getTableSelection();
		BookMark parentFolder = null;
		if( selection.isEmpty() ){
			selection = ( IStructuredSelection ) _bookPage.getTreeViewer().getSelection();
			parentFolder = (BookMark)selection.getFirstElement();
			if(parentFolder == null){
				parentFolder = BookMark.getBarBook();
			}
		}
		else {
			parentFolder = ((BookMark)selection.getFirstElement()).getParent();
		}

		// TODO need write a class InputValidator implements IInputValidator
		InputDialog inputDlg = new InputDialog( _bookPage.getShell(), ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_TITLE,
				ACTION_CREATE_NEW_FOLDER_INPUTDIALOG_INFO, ACTION_CREATE_NEW_FOLDER_DEFAULT_NAME, null );

		if( inputDlg.open() == InputDialog.OK ) {
			String folderName = inputDlg.getValue();
			try {
				if( parentFolder.hasChild(folderName) ) {
					throw new Exception( ACTION_CREATE_NEW_FOLDER_INFO_FOLDER_NAME_EXIST );
				}
				BookMark newFolder = new BookMark(BookMark.Type.folder, folderName,"");
				parentFolder.addChild(newFolder);
				_bookPage.addBookFolder(parentFolder, newFolder);
				BookMark.save();
			}
			catch( Exception e ) {
				MessageBox msgBox = new MessageBox(_bookPage.getShell());
				msgBox.setMessage( e.getMessage() );
				msgBox.open();
			}
		}
		_bookPage.refresh();
	}
}
