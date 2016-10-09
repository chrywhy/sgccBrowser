package com.chry.browser.mainframe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chry.util.FileUtil;

public class BookMark {
	static Logger logger = LogManager.getLogger(BookMark.class.getName());
	public static List<BookMark> bookMarks = new LinkedList<BookMark>();
	public static List<String> bookFolderNames = new ArrayList<String>();
	public static List<BookMark> bookFolders = new ArrayList<BookMark>();
	
	public static enum Type {url, folder;
		public static Type String2Type(String type) {
			if ("url".equals(type)) {
				return url;
			} else if ("folder".equals(type)) {
				return folder;
			} else {
				return null;
			}
		}
	};
	
	public static String ROOTS = "roots";
	public static String VERSION = "version";
	public static String NAME = "name";
	public static String URL = "url";
	public static String TYPE = "type";
	public static String CHILDREN = "children";

    String name;
    String url;
    Type type;
    
    List<BookMark> children = null;
            
    public BookMark(Type type, String name, String url) {
    	this.name = name;
    	this.type = type;
    	if (type == Type.url) {
    		this.url = url;
    	}
    	children = null;
    }
        
	private static List<BookMark> load(JSONArray roots){
		List<BookMark> bookmarks = new LinkedList<BookMark>();
		if (roots != null) {
			for (int i=0; i<roots.length(); i++) {
				try {
					JSONObject oJson = roots.getJSONObject(i);
					String name = oJson.getString(NAME);
					String sType = oJson.getString(TYPE);
					Type type = Type.String2Type(sType);
					if (type == null) {
						logger.warn("Invalid bookmark type:" + sType);
						continue;
					}
					String url = null;
					if (type == Type.url) {
						url = oJson.getString(URL);
						if (url.startsWith("chrome://")) {
							continue;
						}
					}
					BookMark bookmark = new BookMark(type, name, url); 
					if (type == Type.folder) {
						JSONArray children = oJson.getJSONArray(CHILDREN);
						List<BookMark> folders = load(children);
						bookmark.children = folders;
						bookFolders.add(bookmark);
						bookFolderNames.add(bookmark.name);
					}
					bookmarks.add(bookmark);
				} catch (Exception e) {
					logger.warn("invalid bookmark item, ignore!", e);
				}
			}
		}
		return bookmarks;
	}
	
	public static void load(){
		try {
			String jsonStr = FileUtil.readFileToString(BrowserConfig.BookPath);
			JSONObject json = new JSONObject(jsonStr);
			JSONObject roots = json.getJSONObject(ROOTS);
			JSONObject bookmark_bar = roots.getJSONObject("bookmark_bar");
			JSONArray children = bookmark_bar.getJSONArray(CHILDREN);
			bookFolderNames.add("书签栏");
			bookFolders.add(null);
			bookMarks = load(children);
		} catch(Exception e) {
			logger.warn("can not load BookMark !", e);
		}
	}

	private static JSONArray toJsonArray(List<BookMark> booksmarks) {
    	JSONArray aJson = new JSONArray();
		for (BookMark bookmark : booksmarks) {
	    	JSONObject oJson = new JSONObject();
			try {
				oJson.put(NAME, bookmark.name);
				if (bookmark.type == Type.url) {
					oJson.put(TYPE, "url");
					oJson.put(URL, bookmark.url);
				} else {
					oJson.put(TYPE, "folder");
					JSONArray aJsonChildren = toJsonArray(bookmark.children);
					oJson.put(CHILDREN, aJsonChildren);
				}
				aJson.put(oJson);
			} catch (JSONException e) {
				logger.warn("Can not fully conver bookemark to Json Array", e);
			}
		}
		return aJson;
    }
	
	public Image getIcon() {
		Image icon;
		if (type == Type.url) {
			icon = BrowserConfig.getIcon(url);
		} else {
			icon = BrowserConfig.getFolderIcon();
		}
		return icon;
	}
	
    public static JSONObject toJson() {
    	JSONObject json = new JSONObject();
		try {
	    	JSONObject roots = new JSONObject();
	    	json.put(ROOTS, roots);
	    	JSONObject bookmark_bar = new JSONObject();
	    	roots.put("bookmark_bar", bookmark_bar);
	    	JSONArray children = toJsonArray(bookMarks);
	    	bookmark_bar.put(CHILDREN, children);
	    	json.put(VERSION, "1.0");
		} catch (JSONException e) {
			logger.warn("Can not conver bookemark to Json", e);
		}
    	return json;
    }
    
    public static void save() {
    	try {
			String bookmarks = toJson().toString(2);
			FileUtil.WriteStringToFile(bookmarks, BrowserConfig.BookPath);
		} catch (JSONException e) {
			logger.error("Failed to save bookmarks!", e);
		}
    }
}
