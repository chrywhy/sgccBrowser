package com.chry.browser.download;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.chry.browser.bookmark.BookMark;
import com.chry.browser.config.BrowserConfig;
import com.chry.util.FileUtil;

public class Downloads {
	static Logger logger = LogManager.getLogger(BookMark.class.getName());
	private static List<Download> downloads = new LinkedList<Download>();
	
	public static List<Download> getDownloads() {
		if (downloads.isEmpty()) {
			load();
		}
		return downloads;
	}
	
	public static void add(Download download) {
		downloads.add(download);
		download.start();
	}

	public static void remove(Download download) {
		downloads.remove(download);
	}
	
	public static void load() {
		String jsonStr = null;
		try {
			jsonStr = FileUtil.readFileToString(BrowserConfig.DownloadHistory);
			JSONObject json = new JSONObject(jsonStr);
			JSONArray jArray = json.getJSONArray("downloads");
			for (int i = 0; i< jArray.length(); i++) {
				JSONObject item = jArray.getJSONObject(i);
				Download download = new Download();
				download.setFilename(item.getString("filename"));
				download.setUrl(item.getString("url"));
				download.setPath(item.getString("path"));
				download.setTotalSize(item.getLong("totalSize"));
				download.setCurSize(item.getLong("curSize"));
				download.setEpochStart(item.getLong("epochStart"));
				download.setEpochDone(item.getLong("epochDone"));
				downloads.add(download);
			}
		} catch(Exception e) {
			logger.warn("can not load download history !",e);
		}
	}

	private static JSONObject _toJson() {
		JSONObject json = new JSONObject();
		JSONArray jArray = new JSONArray();
		try {
			json.put("downloads", jArray);
			for (Download download : downloads) {
				JSONObject item = new JSONObject();
				item.put("filename", download.getFilename());
				item.put("url", download.getUrl());
				item.put("path", download.getPath());
				item.put("totalSize", download.getTotalSize());
				item.put("curSize", download.getCurSize());
				item.put("epochStart", download.getEpochStart());
				item.put("epochDone", download.getEpochDone());
				jArray.put(item);
			}
		} catch(Exception e) {
			
		}	
		return json;
	}
	
	public static void save() {
    	try {
			String downloads = _toJson().toString(2);
			if (!FileUtil.exists(BrowserConfig.DownloadHistory)) {
				FileUtil.createFile(BrowserConfig.DownloadHistory);
			}
			FileUtil.WriteStringToFile(downloads, BrowserConfig.DownloadHistory);
		} catch (Exception e) {
			logger.error("Failed to save download history!", e);
		}
	}
}
