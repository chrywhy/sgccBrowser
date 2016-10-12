package com.chry.browser.safe;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.chry.browser.BrowserWindow;
import com.chry.browser.config.BrowserConfig;
import com.chry.util.http.SyncHttpClient;
import com.chry.util.swt.SWTResourceManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginDialog extends Dialog {
	static Logger logger = LogManager.getLogger(LoginDialog.class.getName());

	protected Object result;
	protected Shell _shell;
	private Text text_username;
	private Text text_password;
	private BrowserWindow _window;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public LoginDialog(BrowserWindow window) {
		super(window.getShell(), SWT.APPLICATION_MODAL);
		_window = window;
		setText("用户登录");
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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		_shell = new Shell(getParent(), SWT.APPLICATION_MODAL);
		_shell.setImage(SWTResourceManager.getImage(LoginDialog.class, "/com/chry/browser/resource/images/group.png"));
		_shell.setSize(280, 156);

		Rectangle parentBounds = getParent().getBounds();  
		Rectangle shellBounds = _shell.getBounds();  		  
		_shell.setLocation(parentBounds.x + (parentBounds.width - shellBounds.width)/2, parentBounds.y + (parentBounds.height - shellBounds.height)/2);  

		_shell.setText("用户登录");
		
		text_username = new Text(_shell, SWT.BORDER);
		text_username.setToolTipText("请输入用户账号");
		text_username.setBounds(91, 34, 130, 23);
		
		Label label = new Label(_shell, SWT.NONE);
		label.setBounds(37, 37, 48, 17);
		label.setText("用户名：");
		
		Label label_1 = new Label(_shell, SWT.NONE);
		label_1.setText("密　码：");
		label_1.setBounds(37, 66, 48, 17);
		
		text_password = new Text(_shell, SWT.BORDER|SWT.PASSWORD);
		text_password.setEchoChar('*');
		text_password.setBounds(91, 63, 130, 23);
		
		Button button = new Button(_shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String user = text_username.getText();
				String password = text_password.getText();
				SyncHttpClient httpClient = new SyncHttpClient();
				SafeGate.register();
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("token", SafeGate.getToken());
				String token = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/login/" + user + "/" + password, headers);
				if (token != null) {
					String json = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/users/" + user, headers);
			    	ObjectMapper mapper = new ObjectMapper();
			    	Map<String, String> map = null;
			    	try {
						map = mapper.readValue(json, new TypeReference<HashMap<String,String>>(){});
						String role = map.get("role");
						_window.setLogin(user, role);
					} catch (Exception e1) {
						logger.error(e1);
					}
				}
				_shell.close();
			}
		});
		button.setBounds(152, 89, 80, 27);
		button.setText("登录");
		
		Button button_1 = new Button(_shell, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_shell.close();
			}
		});
		button_1.setText("取消");
		button_1.setBounds(66, 89, 80, 27);
		
		Label label_2 = new Label(_shell, SWT.NONE);
		label_2.setAlignment(SWT.CENTER);
		label_2.setBounds(102, 10, 61, 17);
		label_2.setText("用户登录");

	}
}
