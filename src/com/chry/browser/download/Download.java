package com.chry.browser.download;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chry.browser.config.BrowserConfig;
import com.chry.util.http.AsyncHttpClient;
import com.chry.util.http.IHttpLoadProgressListener;
import com.chry.util.http.LoadEvent;

public class Download {
    static Logger logger = LogManager.getLogger(Download.class.getName());

    private String path;
	private String filename;
	private String sUrl;
	private long totalSize;
	private long curSize;
	private long epochStart;
	private long epochDone;
	
	private AsyncHttpClient httpclient = new AsyncHttpClient(new IHttpLoadProgressListener() {
		@Override
		public void loadStart() {
			setEpochStart(System.currentTimeMillis());
			setProgress(0);
		}

		@Override
		public void loadFinished(LoadEvent e) {
	        logger.info("save " + getUrl() + " as file: " + getPath() + File.separator + getFilename());        
			setEpochDone(System.currentTimeMillis());
			setProgress(10000);
		}

		@Override
		public void progress(int size) {
//	        logger.info("download " + size + " bytes");        
			curSize += size;
		}

		@Override
		public void initSize(int size) {
			totalSize = size;
		}
	}
);

	private static String GetFileName(String file) {
	    StringTokenizer st=new StringTokenizer(file,"/");
	    while(st.hasMoreTokens()) {
	      file=st.nextToken();
	    }
	    return file;
	}
	
	public Download() {
		path = "";
		sUrl = "";
		filename = "";
	    totalSize = 0L;
	    curSize = -1L;
	    epochStart = 0L;
	    epochDone = 0L;
	}
	
	public Download(String sUrl) {
		path = BrowserConfig.DownloadFolder;
		this.sUrl = sUrl;
		try {
			URL url = new URL(sUrl);
		    filename = url.getFile();
		    filename = GetFileName(filename);
		} catch (MalformedURLException e) {
			sUrl = "";
			filename = "";
		}
	    totalSize = 0L;
	    curSize = -1L;
	    epochStart = 0L;
	    epochDone = 0L;
	}
	
	public String getPath() {
		return path;
	}
	public String getFilename() {
		return filename;
	}
	public String getFullPath() {
		return path + File.separator + filename;
	}
	public String getUrl() {
		return sUrl;
	}
	public long getTotalSize() {
		return totalSize;
	}
	public long getCurSize() {
		return curSize;
	}
	public long getLeftSize() {
		return totalSize - curSize;
	}
	public int getProgress() {
		return (int)((curSize * 1000) / totalSize);
	}
	public long getEpochStart() {
		return epochStart;
	}
	public long getEpochDone() {
		return epochDone;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setUrl(String sUrl) {
		this.sUrl = sUrl;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public void setCurSize(long curSize) {
		this.curSize = curSize;
	}
	public void setProgress(long progress) {
		this.curSize = totalSize * progress / 10000;
	}
	public void setEpochStart(long epoch) {
		this.epochStart = epoch;
	}
	public void setEpochDone(long epoch) {
		this.epochDone = epoch;
	}

	public boolean isFinished() {
		return totalSize == curSize;
	}
	
	public void start() {
		httpclient.startDownload(sUrl, path, filename);
	}	
}
