package com.chry.browser;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ReportErrorDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ReportErrorDialog(Shell parent) {
		this(parent, SWT.APPLICATION_MODAL);
	}
	
	public ReportErrorDialog(Shell parent, int style) {
		super(parent, style);
		setText("问题上报");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		
		Rectangle parentBounds = getParent().getBounds();  
		Rectangle shellBounds = shell.getBounds();  		  
		shell.setLocation(parentBounds.x + (parentBounds.width - shellBounds.width)/2, parentBounds.y + (parentBounds.height - shellBounds.height)/2);  
		
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(635, 359);
		shell.setText(getText());
		
		StyledText styledText = new StyledText(shell, SWT.BORDER);
		styledText.setBounds(29, 104, 575, 169);
		
		text = new Text(shell, SWT.BORDER);
		text.setBounds(29, 42, 575, 23);
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(29, 19, 77, 17);
		lblNewLabel.setText("问题摘要");
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setBounds(29, 82, 61, 17);
		lblNewLabel_1.setText("问题详述：");
		
		Button btnReport = new Button(shell, SWT.NONE);
		btnReport.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				shell.dispose();
			}
		});
		btnReport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (text.getText().trim().isEmpty()) {
					return;
				}
				shell.dispose();
			}
		});
		btnReport.setBounds(385, 293, 80, 27);
		btnReport.setText("上报");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (text.getText().trim().isEmpty()) {
					return;
				}
				shell.dispose();
			}
		});
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				shell.dispose();
			}
		});
		btnCancel.setBounds(524, 293, 80, 27);
		btnCancel.setText("取消");
	}
}
