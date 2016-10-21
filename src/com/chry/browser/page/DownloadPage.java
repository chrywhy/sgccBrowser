package com.chry.browser.page;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.chry.browser.BrowserWindow;
import com.chry.browser.download.DownLoadMonitor;
import com.chry.browser.download.Download;
import com.chry.browser.download.DownloadItem;
import com.chry.browser.download.Downloads;

public class DownloadPage extends CTabItem {
	List<DownloadItem> downloadItems;
	BrowserWindow _window;
	ScrolledComposite _sc;
	Composite _contents;
	DownLoadMonitor progressMonitor;
	
	public DownloadPage(CTabFolder parent, int style, int index) {
		super(parent, style, index);
	}

	public DownloadPage(BrowserWindow window, int index) {
		this(window.getPageFolder(), SWT.BORDER | SWT.CLOSE, index);
		_window = window;
		downloadItems = new ArrayList<DownloadItem>();
	    _sc = new ScrolledComposite(_window.getPageFolder(), SWT.V_SCROLL | SWT.BORDER);
	    setControl(_sc);
	}

	public void startMonitor() {
		progressMonitor = new DownLoadMonitor(this);
		progressMonitor.startMonitor();
	}
	
	public void refresh() {
		if (_contents != null) {
			_contents.dispose();
			_contents = null;
		}
	    _contents = new Composite(_sc, SWT.NONE);
	    _sc.setContent(_contents);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    _contents.setLayout(layout);
	    _contents.setSize(_contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	    _contents.layout();     
	    _loadDownloadItems();
	}
	
	private void _loadDownloadItems() {
		List<Download> downloads = Downloads.getDownloads();
		for (int i = downloads.size() - 1; i >= 0; i--) {
			Download download = downloads.get(i);
			DownloadItem item = new DownloadItem(_contents, download, i);
			downloadItems.add(item);
		}
	    _contents.setSize(_contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	    _contents.layout();    
	}
	
	public void refreshProgress() {
		for (DownloadItem downloadItem : downloadItems) {
			downloadItem.refreshProgress();
		}
	}
}
