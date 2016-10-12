package com.chry.browser.bookmark.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.page.BookPage;

public class RenameAction extends Action {
	private BookPage _bookPage;
	private static final String ACTION_RENAME_INPUTDIALOG_TITLE = "重命名";
	private static final String ACTION_RENAME_INPUTDIALOG_INFO = "请输入新书签名";

	public RenameAction( BookPage bookPage ) {
		_bookPage = bookPage;
		// setEnabled( false );
//		setToolTipText( "Rename file" );
		setText( "重命名@F2" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/rename.gif" ) ) );
	}

	public void run() {
		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.isEmpty()){
			return;
		}

		if( selection.size() > 1 ){
			// TODO implement batch rename
			return;
		}

		
		BookMark selectedItem = (BookMark) selection.getFirstElement();
		String oldFileName = selectedItem.getName();
		// TODO need write a class InputValidator implements IInputValidator
		InputDialog inputDlg = new InputDialog( _bookPage.getShell(), ACTION_RENAME_INPUTDIALOG_TITLE,
				ACTION_RENAME_INPUTDIALOG_INFO, oldFileName, null );

		if( inputDlg.open() == InputDialog.OK ){
			String newName = inputDlg.getValue();
			// TODO need check name duplicated and rename failed
			selectedItem.setName(newName);
			_bookPage.refresh(selectedItem);
		}

	}
}
