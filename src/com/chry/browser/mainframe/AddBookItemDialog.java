package com.chry.browser.mainframe;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class AddBookItemDialog extends Dialog {

	protected Object result;
	protected Shell _shell;
	private BrowserWindow _window = null;
	private Text bookname;
	private String _name;
	private String _sUrl;
	
	public AddBookItemDialog(Shell parent, int style) {
		super(parent, style);
		setText("添加新书签");
		_name = "";
		_sUrl = "";
		_shell = new Shell(getParent(), SWT.BORDER | SWT.APPLICATION_MODAL);
	}
	
	public AddBookItemDialog(BrowserWindow window, String name, String sUrl) {
		super(window.getShell(), SWT.APPLICATION_MODAL);
		setText("添加新书签");
		_window = window;
		_name = name;
		_sUrl = sUrl;
		_shell = new Shell(getParent(), SWT.BORDER | SWT.APPLICATION_MODAL);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		_shell.open();
		_shell.layout();
		Display display = getParent().getDisplay();
		while (!_shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	public void setLocation(int x, int y) {
		_shell.setLocation(x, y);  
	}

	private void createContents() {
		_shell.setSize(356, 186);
		_shell.setText("添加新书签");
		
		Label label_1 = new Label(_shell, SWT.NONE);
		label_1.setBounds(10, 37, 45, 17);
		label_1.setText("名字：");
		
		Label label_2 = new Label(_shell, SWT.NONE);
		label_2.setText("文件夹：");
		label_2.setBounds(10, 82, 45, 17);
		
		bookname = new Text(_shell, SWT.BORDER);
		bookname.setBounds(60, 37, 275, 23);
		bookname.setText(_name);

		final Combo folderName = new Combo(_shell, SWT.READ_ONLY);
		folderName.setBounds(60, 74, 275, 25);
		String[] items = BookMark.bookFolderNames.toArray(new String[0]);
		folderName.setItems(items);
		folderName.select(0);
		
		Button _btnOk = new Button(_shell, SWT.NONE);
		_btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int index = folderName.getSelectionIndex();
				BookMark newBookmark = new BookMark(BookMark.Type.url, bookname.getText(), _sUrl);
				if (index > 0) {
					BookMark bookFolder = BookMark.bookFolders.get(index);
					bookFolder.children.add(newBookmark);
					_window.addNewBookmark(bookFolder.name, newBookmark);
				} else {
					BookMark.bookMarks.add(newBookmark);
					_window.addNewBookmark("", newBookmark);
				}
		    	BookMark.save();
				_shell.dispose();
			}
		});
		_btnOk.setBounds(65, 120, 83, 27);
		_btnOk.setText("完成");
		
		Button _btnCancel = new Button(_shell, SWT.NONE);
		_btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				_shell.dispose();
			}
		});
		_btnCancel.setBounds(260, 120, 80, 27);
		_btnCancel.setText("取消");
		
		Label label = new Label(_shell, SWT.NONE);
		label.setFont(SWTResourceManager.getFont("宋体", 12, SWT.NORMAL));
		label.setBounds(10, 8, 93, 23);
		label.setText("添加新书签");

	}
}
