package com.chry.browser.config;

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

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.safe.SafeGate;
import com.chry.util.FileUtil;
import com.chry.util.PropConf;
import com.chry.util.swt.SWTResourceManager;

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
	
	public static String ROOT = System.getProperty("user.home");
	public static String FavoritePath = ROOT + File.separator + ".safeBrowserFavorite";
	public static String FaviconPath = FavoritePath + File.separator + "favicons";
	public static String BookFile = FavoritePath + File.separator + "bookmarks";
	public static String DownloadFolder = ROOT + File.separator + "downloads";
	public static String DownloadHistory = FavoritePath + File.separator + "downloadHistory";
	public static PropConf history;
	static {
		try {
			FileUtil.createDirectory(FavoritePath);
			FileUtil.hideDirectory(FavoritePath);
			FileUtil.createDirectory(FaviconPath);
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
	
	public static void addNewSite(String host) {
		try {
			FileUtil.createDirectory(FaviconPath + File.separator + host);
		} catch (IOException e) {
			logger.warn("Failed to add site favicon", e);
		}
	}
}
