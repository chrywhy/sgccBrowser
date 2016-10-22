package com.chry.util.http;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chry.browser.download.Download;
import com.chry.util.FileUtil;

public class AsyncHttpClient extends HttpClient {
    static Logger logger = LogManager.getLogger(AsyncHttpClient.class.getName());

    protected Map<Integer,String> _results = new ConcurrentHashMap<Integer,String>();
	private int _taskCount = 0;
    ExecutorService _executor = Executors.newFixedThreadPool(1); 
	
	protected abstract class Task implements Runnable {
		protected String _sUrl; 
		protected String _filePath;
		protected int _id;

		public Task(String sUrl, String filePath) {
			_sUrl = sUrl;
			_filePath = filePath;
			_id = _taskCount++;
		}
		
		public int getId() {
			return _id;
		}
		public abstract void run();
	}
	
	public class AccessTask extends Task {
		public AccessTask(String sUrl) {
			super(sUrl, null);
		}

		@Override
		public void run() {
        	HttpResponseStream rspStream = get(_sUrl);
	    	if (rspStream.getResponseCode() == 200) {
	        	_results.put(_id, rspStream.decodeToString(_progressListener));
	    	} else {
	    		_progressListener.loadFinished(LoadEvent.getEvent(new HttpException(rspStream.getResponseCode(), rspStream.getResponseMessage())));
	    	}
		}		
	}
	
	public class DownloadTask extends Task {
		private String _filename;
		public DownloadTask(String sUrl, String filePath, String filename) {
			super(sUrl, filePath + File.separator + filename);
			_filename = filename;
		}

		@Override
		public void run() {
			logger.info("download begin ...");
	    	try {
	    		boolean redirect = true;
	    		String sUrl = _sUrl;
	    		while (redirect) {
	    			redirect = false;
			    	HttpResponseStream rspStream = get(sUrl, redirect);
			    	HttpURLConnection conn = rspStream.getHttpURLConnection();
			    	int code = rspStream.getResponseCode();
			    	if (code == 200) {
				    	int size = conn.getContentLength();
				    	_progressListener.initSize(size);
				    	FileUtil.createFile(_filePath);
				    	FileOutputStream fos = new FileOutputStream(_filePath);
				        rspStream.decodeToStream(fos, _progressListener);
			    	} else if (code == 302 || code == 301){
				    	String location = conn.getHeaderField("Location");
				    	if (location.endsWith(_filename)) {
				    		redirect = true;
				    		sUrl = location;
				    	} else {
				    		_progressListener.loadFinished(LoadEvent.getEvent(new HttpException(rspStream.getResponseCode(), rspStream.getResponseMessage())));
				    	}
			    	} else {
			    		_progressListener.loadFinished(LoadEvent.getEvent(new HttpException(rspStream.getResponseCode(), rspStream.getResponseMessage())));
			    	}
	    		}
	    	} catch (Exception e) {
	    		_progressListener.loadFinished(LoadEvent.getEvent(e));
	    	}
			logger.info("download done ...");
		}		
	}
	
	private IHttpLoadProgressListener _progressListener;
    public AsyncHttpClient(IHttpLoadProgressListener progressListener) {
        super();
        _progressListener = progressListener;
    }

    public int access(String sUrl) {
        AccessTask task = new AccessTask(sUrl);
        _executor.execute(task);
        return task.getId();
    }
    
    public int startDownload(String sUrl, String fullPath) {
    	String path;
    	String filename;
    	int index = fullPath.lastIndexOf(File.separator);
    	if (index < 0) {
    		path = ".";
    		filename = fullPath;
    	} else {
    		path = fullPath.substring(0, index);
    		filename = fullPath.substring(index + 1);
    	}
    	return startDownload(sUrl, path, filename);
    }
    
    public int startDownload(String sUrl, String path, String filename) {
    	DownloadTask task = new DownloadTask(sUrl, path, filename);
        _executor.execute(task);
        return task.getId();
    }
    
    public String getText(int id) {
    	return _results.get(id);
    }

    public void clear() {
    	_results.clear();
    	_taskCount = 0;
    	_executor.shutdown();
    }
    
    public void stop() {
    	_progressListener.setShutdown(true);
    	_executor.shutdown();
    }
}
