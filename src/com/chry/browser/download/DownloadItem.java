package com.chry.browser.download;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.chry.browser.config.ImageConfig;

public class DownloadItem {
    static Logger logger = LogManager.getLogger(DownloadItem.class.getName());
	Composite _parent;
	int _index;
	Download _download;
	Label lbProgressInfo;
	Label lbPercent;
	ProgressBar progressBar;
	
	public DownloadItem(Composite parent, Download download, int index) {
		_parent = parent;
		_index = index;
		_download = download;
		createContent();
	}
	
	private void createContent() {
		Composite downloadComposite = new Composite(_parent, SWT.BORDER);
		int y = 50 + (130 * _index);
		downloadComposite.setBounds(20, y, 618, 110);
		
		Label lbFileName = new Label(downloadComposite, SWT.NONE);
		lbFileName.setBounds(22, 0, 592, 17);
		lbFileName.setText("存放位置: " + _download.getFullPath());
		
		Label lbUrl = new Label(downloadComposite, SWT.NONE);
		lbUrl.setBounds(22, 23, 592, 17);
		lbUrl.setText("下载地址: " + _download.getUrl());
		
		progressBar = new ProgressBar(downloadComposite, SWT.SMOOTH);
		progressBar.setBounds(22, 66, 570, 17);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		  
	    lbPercent = new Label(downloadComposite, SWT.NONE);
		lbPercent.setBounds(600, 66, 50, 17);
		lbPercent.setText("0%");

		Label lbShowInFolder = new Label(downloadComposite, SWT.NONE);
		lbShowInFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		lbShowInFolder.setBounds(22, 89, 92, 17);
		lbShowInFolder.setText("在文件夹中显示");
		
		lbProgressInfo = new Label(downloadComposite, SWT.NONE);
		lbProgressInfo.setBounds(22, 46, 568, 17);
		refreshProgress();
		
		Label lbClear = new Label(downloadComposite, SWT.NONE);
		lbClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		lbClear.setBounds(570, 89, 40, 17);
		lbClear.setText("清除");
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

	public void refreshProgress() {
		if (!_download.isFinished()) {
			Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	int percent = (int)(_download.getCurSize() * 100 / _download.getTotalSize());
					lbProgressInfo.setText("总大小：" + getSizeWithAutoUnit(_download.getTotalSize()) 
									   + ", 已下载:" + getSizeWithAutoUnit(_download.getCurSize()) 
									   + ", 剩余:" + getSizeWithAutoUnit(_download.getLeftSize())
									   + ", 大约需要:" + getTimeWithAutoUnit(_download.getTotalSize(), _download.getCurSize(), _download.getEpochStart()));
					progressBar.setSelection(percent);
					lbPercent.setText(percent + "%");
			    }
			}); 			
		}
	}
}
