package com.chry.browser.download;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chry.browser.page.DownloadPage;

public class DownLoadMonitor {
    static Logger logger = LogManager.getLogger(DownLoadMonitor.class.getName());
	public static class Task implements Runnable {
		private DownloadPage _downloadPage;
		boolean isRun;
		Task(DownloadPage downloadPage) {
			_downloadPage = downloadPage;
		}
		public void setDownloadPage(DownloadPage page) {
			_downloadPage = page;
		}
		@Override
		public void run() {
//			logger.info("refresh download page begin");
			_downloadPage.refreshProgress();
//			logger.info("refresh download page done");
		}
	}

	private DownloadPage _downloadPage;
	ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1); 
	private ScheduledFuture<?> _detectSchedule;
	Task _task;
	
	public DownLoadMonitor(DownloadPage downloadPage) {
		_downloadPage = downloadPage;
		_task = new Task(_downloadPage);
	}

	public void startMonitor() {
    	_detectSchedule = _executor.scheduleWithFixedDelay(_task, 1, 1, TimeUnit.SECONDS);
	}
	
	public void stopMonitor() {
		_detectSchedule.cancel(true);
		_executor.shutdown();
	}
}
