package com.chry.browser.download;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.chry.browser.download.Download.Status;
import com.chry.browser.page.DownloadPage;

public class DownloadItem {
    static Logger logger = LogManager.getLogger(DownloadItem.class.getName());
	DownloadPage _downloadPage;
    Composite _parent;
	int _y;
	Download _download;
	Label lbProgressInfo;
	Label lbPercent;
	ProgressBar progressBar;
	int _height;
	
	public DownloadItem(DownloadPage downloadPage, Composite parent, Download download, int y) {
		_downloadPage = downloadPage;
		_parent = parent;
		_y = y;
		_download = download;
		createContent();
	}
	
	private void createContent() {
		final Composite downloadComposite = new Composite(_parent, SWT.BORDER);
		final boolean downloadUI = (_download.getStatus() == Status.Downloading);
		_height = downloadUI ? 110 : 80;
		downloadComposite.setBounds(20, _y, 618, _height);
		
		Label lbFileName = new Label(downloadComposite, SWT.NONE);
		lbFileName.setBounds(22, 0, 592, 17);
		lbFileName.setText("存放位置: " + _download.getFullPath());
		
		Label lbUrl = new Label(downloadComposite, SWT.NONE);
		lbUrl.setBounds(22, 23, 592, 17);
		lbUrl.setText("下载地址: " + _download.getUrl());
		Label lbShowInFolder = new Label(downloadComposite, SWT.NONE);
		Label lbClear = new Label(downloadComposite, SWT.NONE);
		if (downloadUI) {
			progressBar = new ProgressBar(downloadComposite, SWT.SMOOTH);
			progressBar.setBounds(22, 66, 570, 17);
			progressBar.setMaximum(100);
			progressBar.setMinimum(0);
			  
		    lbPercent = new Label(downloadComposite, SWT.NONE);
			lbPercent.setBounds(600, 66, 50, 17);
			lbPercent.setText("0%");
			lbShowInFolder.setBounds(22, 89, 92, 17);
			lbClear.setBounds(570, 89, 40, 17);
		} else {
			lbShowInFolder.setBounds(22, 66, 92, 17);
			lbClear.setBounds(570, 66, 40, 17);
		}
		Cursor handCursor = new Cursor(null,SWT.CURSOR_HAND);
		lbShowInFolder.setText("在文件夹中显示");
		lbShowInFolder.setCursor(handCursor);
		Display display = Display.getCurrent();
		Color fontColor = display.getSystemColor(SWT.COLOR_BLUE);
		lbShowInFolder.setForeground(fontColor); 
		lbClear.setForeground(fontColor); 
		lbShowInFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try {
					Runtime.getRuntime().exec("explorer " + _download.getPath());
				} catch (IOException e1) {
					logger.info("Can not open folder " + _download.getPath());
				}
			}
		});
		
		lbProgressInfo = new Label(downloadComposite, SWT.NONE);
		lbProgressInfo.setBounds(22, 46, 568, 17);
		refreshProgress(true);
		
		lbClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (downloadUI) {
					_download.stop();
				} else {
					Downloads.remove(_download);
					_downloadPage.refresh();
					Downloads.save();
				}
			}
		});
		lbClear.setCursor(handCursor);
		if (downloadUI) {
			lbClear.setText("停止");
		} else {
			lbClear.setText("清除");
		}
	}
	
	public static String getSizeWithAutoUnit(long size) {
		int m = (int) size / (1024 * 1024);
		int left = (int) size % (1024 * 1024);
		int k = left / 1024;
		int b = left % 1024;
		if (m > 0) {
			return m + "." + (k * 100 / 1024 ) + "MB";
		} else if (k > 0) {
			return k + "." + (b * 100 / 1024 ) + "KB";
		} else {
			return b + "B";
		}
	}

	public int getHeight() {
		return _height;
	}
	
	public Download getDownload() {
		return _download;
	}
	
	public static String getTimeWithAutoUnit(long total, long cur, long epochStart) {
		long left = total - cur;
		long epoch = System.currentTimeMillis();
		long passedTime = epoch - epochStart;
		long leftTime = left * passedTime / cur;
		long seconds = leftTime / 1000;
		long minutes = seconds / 60;
		long hours = seconds / 360;
		long day = hours / 24;
		if (day > 0) {
			return day + "天" + (hours % 24) + "小时";
		} else if (hours > 0) {
			return hours + "小时" + (minutes % 60) + "分";
		} else if (minutes > 0) {
			return minutes + "分" + (seconds % 60) + "秒";
		} else {
			return seconds + "秒";
		}
	}

	public static String getTime(long epoch) {
		return new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch));
	}
	
	public static String getSpeedWithAutoUnit(long cur, long epochStart) {
		long elapseSeconds = (System.currentTimeMillis() - epochStart) / 1000;
		long speed =  8 * cur / elapseSeconds;
		int m = (int) speed / (1024 * 1024);
		int left = (int) speed % (1024 * 1024);
		int k = left / 1024;
		int b = left % 1024;
		if (m > 0) {
			return m + "." + (k * 100 / 1024 ) + "Mb/s";
		} else if (k > 0) {
			return k + "." + (b * 100 / 1024 ) + "Kb/s";
		} else {
			return b + "b/s";
		}
	}
	
	public void refreshProgress(boolean refreshFinishedItem) {
    	final boolean refreshFinishedItems = refreshFinishedItem;
    	try {
			if (_download.getStatus()==Status.Abort && refreshFinishedItems) {
				lbProgressInfo.setText("下载已中断 --"
				   + " 总大小：" + getSizeWithAutoUnit(_download.getTotalSize()) 
				   + ", 已下载:" + getSizeWithAutoUnit(_download.getCurSize()) 
				   + ", 剩余:" + getSizeWithAutoUnit(_download.getLeftSize())
				   + ", 终止时间:" + getTime(_download.getEpochDone()));
			} else if (_download.getStatus()==Status.Finding) {
				lbProgressInfo.setText("正在寻找资源...");
			} else if (_download.getStatus()==Status.NotFound) {
				lbProgressInfo.setText("下载失败 --"
				   + " 下载文件未找到" 
				   + ",下载时间:" + getTime(_download.getEpochStart()));
			} else if (_download.getStatus()==Status.Downloading) {
				lbProgressInfo.setText("正在下载 --"
				   + " 总大小：" + getSizeWithAutoUnit(_download.getTotalSize()) 
				   + ", 已下载:" + getSizeWithAutoUnit(_download.getCurSize()) 
				   + ", 剩余:" + getSizeWithAutoUnit(_download.getLeftSize())
				   + ", 平均速率：" + getSpeedWithAutoUnit(_download.getCurSize(), _download.getEpochStart())
				   + ", 大约需要:" + getTimeWithAutoUnit(_download.getTotalSize(), _download.getCurSize(), _download.getEpochStart()));
		    	int percent = (int)(_download.getCurSize() * 100 / _download.getTotalSize());
				progressBar.setSelection(percent);
				lbPercent.setText(percent + "%");
			} else if (_download.isJustFinished()) {
					_downloadPage.setHasDownloadComplete();
					progressBar.setSelection(100);
					lbPercent.setText("100%");
					lbProgressInfo.setText("总大小：" + getSizeWithAutoUnit(_download.getTotalSize()) 
					   + ", 完成时间:" + getTime(_download.getEpochDone()));
			} else if (_download.getStatus()==Status.Finished && refreshFinishedItems) {
				lbProgressInfo.setText("下载已完成 --"
				   + " 总大小：" + getSizeWithAutoUnit(_download.getTotalSize()) 
				   + ", 完成时间:" + getTime(_download.getEpochDone()));
			}
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    	}
	}
}
