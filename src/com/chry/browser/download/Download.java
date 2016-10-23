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
    public static enum Status { Finding, Downloading, NotFound, Abort, Finished };
    
    private String path;
	private String filename;
	private String sUrl;
	private long totalSize;
	private long curSize;
	private long epochStart;
	private long epochDone;
	private Status status;
	
	private boolean downloadJustCompleted;
	private AsyncHttpClient httpclient = new AsyncHttpClient(new IHttpLoadProgressListener() {
		boolean isShutdown = false;
		@Override
		public void findingResource() {
			setEpochStart(System.currentTimeMillis());
			setProgress(0);
	        status = Status.Finding;
		}

		@Override
		public void loadStart() {
			setEpochStart(System.currentTimeMillis());
			setProgress(0);
	        status = Status.Downloading;
		}

		@Override
		public void loadFinished(LoadEvent e) {
			if (e.status == LoadEvent.OK) {
		        logger.info("save " + getUrl() + " as file: " + getPath() + File.separator + getFilename());
				setEpochDone(System.currentTimeMillis());
				setProgress(10000);
				downloadJustCompleted = true;
		        status = Status.Finished;
			} else {
		        logger.info("download " + getUrl() + " failed: " + e.error.getMessage());        
				setEpochDone(System.currentTimeMillis());
		        status = Status.NotFound;
			}
		}

		@Override
		public void loadAborted(LoadEvent e) {
	        logger.info("download " + getUrl() + " aborted, partial file is: " + getPath() + File.separator + getFilename());        
			setEpochDone(System.currentTimeMillis());
	        status = Status.Abort;
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

		@Override
		public void setShutdown(boolean isShutdown) {
			this.isShutdown = isShutdown;
		}

		@Override
		public boolean isShutdown() {
			return isShutdown;
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
	    downloadJustCompleted = false;
	    status = Status.Finding;
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
	    downloadJustCompleted = false;
	    status = Status.Finding;
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

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
		
	public void setStatus(String status) {
		if (Status.Finding.name().equalsIgnoreCase(status)) {
			this.status = Status.Finding;
		} else if (Status.Abort.name().equalsIgnoreCase(status)) {
			this.status = Status.Abort;
		} else if (Status.Finished.name().equalsIgnoreCase(status)) {
			this.status = Status.Finished;
		} else if (Status.NotFound.name().equalsIgnoreCase(status)) {
			this.status = Status.NotFound;
		} else if (Status.Downloading.name().equalsIgnoreCase(status)) {
			this.status = Status.Downloading;
		} else {
			this.status = Status.Abort;
		}
	}
	
	public boolean isJustFinished() {
		boolean isJustFinished = downloadJustCompleted;
		downloadJustCompleted = false;
		return isJustFinished;
	}
	
	public void start() {
		httpclient.startDownload(sUrl, path, filename);
	}	

	public void stop() {
		httpclient.stop();
		if (status != Status.Downloading) {
			epochDone = System.currentTimeMillis();
			status = Status.Abort;
		}
	}
}
