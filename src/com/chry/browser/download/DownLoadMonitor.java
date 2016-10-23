package com.chry.browser.download;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

import com.chry.browser.BrowserWindow;
import com.chry.browser.page.DownloadPage;

public class DownLoadMonitor {
    static Logger logger = LogManager.getLogger(DownLoadMonitor.class.getName());
	public static class Task implements Runnable {
		private BrowserWindow _window;
		boolean _hasDownloadComplete = false;
		boolean isRun;
		Task(BrowserWindow window) {
			_window = window;
		}
						
		@Override
		public void run() {
			Display display = Display.getDefault();
	    	display.asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						CTabItem activePage = _window.getActivePage();
						if (activePage instanceof DownloadPage) {
							logger.debug("download monitor begin");
							DownloadPage monitoringPage = (DownloadPage)activePage;
							if (_hasDownloadComplete) {
								logger.info("has download done, refresh");
								monitoringPage.refresh();
								_hasDownloadComplete = false;
							} else {
								monitoringPage.refreshProgress();
							}
							logger.debug("download monitor done");
						}
			    	} catch (Exception e) {
			    		logger.error(e.getMessage());
			    	}
				}
	    	});
		}
	}

	ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1); 
	private ScheduledFuture<?> _detectSchedule;
	Task _task;
	
	public DownLoadMonitor(BrowserWindow window) {
		_task = new Task(window);
	}
		
	public void startMonitor() {		
    	_detectSchedule = _executor.scheduleWithFixedDelay(_task, 1, 1, TimeUnit.SECONDS);
	}
	
	public void stopMonitor() {
		_detectSchedule.cancel(true);
		_executor.shutdown();
		logger.info("download monitor shutdown");
	}
	
	public void setHasDownloadComplete() {
		_task._hasDownloadComplete = true;
	}
}
