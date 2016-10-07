package com.chry.browser.mainframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultipleBrowser {
	private MultipleBrowser() {}
	static Logger logger = LogManager.getLogger(MultipleBrowser.class.getName());
	public static void main(String[] args) {
		try {
			BrowserConfig.init();
			BrowserWindow window = new BrowserWindow();
			window.open();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void OpenNewWindow(String url) {
		BrowserWindow window = new BrowserWindow();
		window.open(url);			
	}	
}
