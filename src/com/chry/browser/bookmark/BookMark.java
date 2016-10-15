package com.chry.browser.bookmark;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.MessageBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chry.browser.config.BrowserConfig;
import com.chry.browser.config.ImageConfig;
import com.chry.util.FileUtil;
import com.chry.util.swt.SWTResourceManager;

public class BookMark {
	static Logger logger = LogManager.getLogger(BookMark.class.getName());
	private static final BookMark rootBook = new BookMark(Type.folder, "", "") ;
	private static final BookMark bookBar = new BookMark(Type.folder, "书签栏", "") ;
	private static List<BookMark> _bookMarks = new LinkedList<BookMark>();
	public static List<String> bookFolderNames = new ArrayList<String>();
	public static List<BookMark> bookFolders = new ArrayList<BookMark>();
	
	private static List<BookMark> _clipBookmars = new LinkedList<BookMark>();
	
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

	private String _name;
	private String _url;
    private Type _type;    
    private List<BookMark> _children = null;
    private BookMark _parent = null;
            
    public BookMark(Type type, String name, String url) {
    	this._name = name;
    	this._type = type;
    	if (type == Type.url) {
    		this._url = url;
    	}
    	_children = new LinkedList<BookMark>();
    }
     
	public static void addClipBookmars(BookMark bookmark){
		_clipBookmars.add(bookmark);
	};
	
	public static void setClipBookmars(BookMark bookmark){
		_clipBookmars.clear();
		_clipBookmars.add(bookmark);
	};
	
	public static void setClipBookmars(List<BookMark> bookmarks){
		_clipBookmars.clear();
		_clipBookmars.addAll(bookmarks);
	};

	public static List<BookMark> getClipBookmars(){
		return _clipBookmars;
	}

    public void addChildren(List<BookMark> children) {
    	this._children = children;
    	for (BookMark child : children) {
    		child._parent = this;
    	}
    }
    
    public static boolean copyChild(BookMark targetFolder, BookMark sourceBookmark) {
    	if (sourceBookmark.isDescendant(targetFolder)) {
    		return false;
    	}
		if(targetFolder.getChildren().contains(sourceBookmark)) {
			return false;
		}
    	targetFolder.addChild(sourceBookmark);
    	return true;
    }
    
    public boolean isDescendant(BookMark child) {
    	BookMark ancestor = child;
    	while (ancestor != bookBar) {
        	if (ancestor == this) {
        		return true;
        	} else if (ancestor == _parent) {
        		return false;
        	}
        	ancestor = ancestor.getParent();
    	}
    	return false;
    }
    
    public static void moveChild(BookMark targetFolder, BookMark sourceBookmark) {
    	sourceBookmark.getParent().removeChild(sourceBookmark);
    	copyChild(targetFolder, sourceBookmark);
    }
    
    public void addChild(BookMark child) {
    	_children.add(child);
		child._parent = this;
    	if (child.isFolder()) {
    		bookFolderNames.add(child.getName());
    		bookFolders.add(child);
    	}
    }
    
    public boolean removeChild(BookMark child) {
    	if (child.isFolder()) {
    		bookFolderNames.remove(child.getName());
    		bookFolders.remove(child);
    	}
    	return _children.remove(child);
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
						JSONArray jChildren = oJson.getJSONArray(CHILDREN);
						List<BookMark> children = load(jChildren);
						bookmark.addChildren(children);
						bookFolders.add(bookmark);
						bookFolderNames.add(bookmark._name);
					}
					bookmarks.add(bookmark);
				} catch (Exception e) {
					logger.warn("invalid bookmark item, ignore!", e);
				}
			}
		}
		return bookmarks;
	}
	
	public boolean isFolder() {
		return _type == Type.folder;
	}
	
	public boolean isUrl() {
		return _type == Type.url;
	}
	
	public String getName() {
		return this._name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getUrl() {
		return this._url;
	}
	
	public List<BookMark> getChildren() {
		return this._children;
	}

	public BookMark getParent() {
		return this._parent;
	}
		
	public static List<BookMark> getBooksOnBar() {
		return bookBar._children;
	}
	
	public static BookMark getRootBook() {
		return rootBook;
	}
	
	public static BookMark getBarBook() {
		return bookBar;
	}
	
	public boolean hasChild(String name) {
		for (BookMark bookmark : _children) {
			if (bookmark.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static void load(){
		try {
			String jsonStr = null;
			try {
				jsonStr = FileUtil.readFileToString(BrowserConfig.BookFile);
			} catch (FileNotFoundException e) {
				jsonStr = SWTResourceManager.loadFile(BookMark.class, "/com/chry/browser/resource/defaultBookmarks");
			}
			JSONObject json = new JSONObject(jsonStr);
			JSONObject roots = json.getJSONObject(ROOTS);
			JSONObject bookmark_bar = roots.getJSONObject("bookmark_bar");
			JSONArray children = bookmark_bar.getJSONArray(CHILDREN);
			bookFolderNames.add("书签栏");
			bookFolders.add(rootBook);
			_bookMarks = load(children);
			bookBar.addChildren(_bookMarks);
			rootBook.addChild(bookBar);
		} catch(Exception e) {
			logger.warn("can not load BookMark !",e);
		}
	}

	private static JSONArray toJsonArray(List<BookMark> booksmarks) {
    	JSONArray aJson = new JSONArray();
		for (BookMark bookmark : booksmarks) {
	    	JSONObject oJson = new JSONObject();
			try {
				oJson.put(NAME, bookmark._name);
				if (bookmark._type == Type.url) {
					oJson.put(TYPE, "url");
					oJson.put(URL, bookmark._url);
				} else {
					oJson.put(TYPE, "folder");
					JSONArray aJsonChildren = toJsonArray(bookmark._children);
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
		if (_type == Type.url) {
			icon = ImageConfig.getIcon(_url);
		} else {
			icon = ImageConfig.getFolderIcon();
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
	    	JSONArray children = toJsonArray(_bookMarks);
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
			if (!FileUtil.exists(BrowserConfig.BookFile)) {
				FileUtil.createFile(BrowserConfig.BookFile);
			}
			FileUtil.WriteStringToFile(bookmarks, BrowserConfig.BookFile);
		} catch (Exception e) {
			logger.error("Failed to save bookmarks!", e);
		}
    }
}
