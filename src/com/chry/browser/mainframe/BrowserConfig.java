package com.chry.browser.mainframe;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.wb.swt.SWTResourceManager;

import com.chry.browser.safe.SafeGate;
import com.chry.util.FileUtil;
import com.chry.util.PropConf;

public class BrowserConfig {
	static Logger logger = LogManager.getLogger(BrowserConfig.class.getName());
	
	public static String SafePolicyServer = "127.0.0.1";
	public static String[] KEYPROPOSAL = new String[] { 
			"keywords",
			"http://www.baidu.com",
			"http://www.sgcc.com.cn",
			"http://wldx.sgcc.com.cn", 
			"www.baidu.com", 
			"www.aostarit.com",
			"www.sgcc.com.cn",
			"https://www.aostarit.com.cn:9000/pmis/bsp/jsp/login.jsp", 
			"portal.sgcc.com.cn",
			"www.qq.com"
	};
	public static String[] URLPROPOSAL = new String[] { 
			"http://www.baidu.com",
			"http://www.sgcc.com.cn",
			"http://wldx.sgcc.com.cn", 
			"www.baidu.com", 
			"www.aostarit.com",
			"www.sgcc.com.cn",
			"https://www.aostarit.com.cn:9000/pmis/bsp/jsp/login.jsp", 
			"portal.sgcc.com.cn",
			"www.qq.com",
			"keywords"
	};
	
	public static String HOME = "http://www.baidu.com"; 
	public static String ROOT = System.getProperty("user.home");
	public static String FavoritePath = ROOT + File.separator + ".safeBrowserFavorite";
	public static String FaviconPath = FavoritePath + File.separator + "favicons";
	public static String BookFile = FavoritePath + File.separator + "bookmarks";
	public static String HistoryFile = FavoritePath + File.separator + "history";
	public static PropConf history;
	static {
		try {
			FileUtil.createDirectory(FavoritePath);
			FileUtil.hideDirectory(FavoritePath);
			FileUtil.createDirectory(FaviconPath);
			FileUtil.createFile(HistoryFile);
			history = new PropConf(HistoryFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, String> favicons = new HashMap<String, String>();
	
	public static void init() {
		InputStream in = null;
		try {
			String xulrunner = System.getProperty("user.dir") + File.separator + "xulrunner";
			System.setProperty("org.eclipse.swt.browser.XULRunnerPath",xulrunner);
			System.setProperty("org.eclipse.swt.browser.IEVersion","11000");
		    in = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + File.separator +  "browser.conf"));
			Properties p = new Properties();
			p.load(in);
			SafePolicyServer = p.getProperty("MysqlHost");
		} catch (Exception e){
			logger.error("failed to load browser.conf !", e);
		} finally {
			try {
				if (in != null) in.close();
			} catch(Exception ee) {
			}
		}
		SafeGate.loadSafePolicy();
		BookMark.load();
	}

    public static Image getIcon(String sUrl) {
    	String iconPath;
    	if (sUrl == null || sUrl.trim().isEmpty()) {
    		iconPath = null;
    	} else {
    		iconPath = getIconPath(sUrl);
    	}
    	if (iconPath != null) {
	    	try {
		    	ImageLoader loader = new ImageLoader();
		    	ImageData[] imageData = loader.load(iconPath);
		    	Image image = null;
		    	if(imageData.length > 0){
		    		image = new Image(null, imageData[0].scaledTo(16, 16));
		    	}
		    	return image;
	    	} catch (Exception e) {
	    		logger.error("Invalid icon file : " + iconPath, e);
	    		FileUtil.deleteFile(iconPath);
	    	}
    	}
    	return SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/page.png");
    }
    
    public static Image getFolderIcon() {
    	return SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/folder.png");
    }
    
	public static String getIconPath(String sUrl) {
    	try {
        	URL url;
			url = new URL(sUrl);
			return getIconPath(url);
		} catch (MalformedURLException e) {
			logger.error("failed to set icon", e);
			return null;
		}
	}
	
	public static String getIconPath(URL url) {
		String host = url.getHost();
		String path = FaviconPath + File.separator + host + File.separator + "favicon.ico";
		if (FileUtil.exists(path)) {
    		return path;
    	} else {
    		return null;
    	}
	}
	
	public static void addNewSite(String host) {
		try {
			FileUtil.createDirectory(FaviconPath + File.separator + host);
		} catch (IOException e) {
			logger.warn("Failed to add site favicon", e);
		}
	}
}
