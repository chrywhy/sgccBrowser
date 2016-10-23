package com.chry.browser.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.chry.browser.BrowserWindow;
import com.chry.browser.download.DownLoadMonitor;
import com.chry.browser.download.Download;
import com.chry.browser.download.DownloadItem;
import com.chry.browser.download.Downloads;

public class DownloadPage extends CTabItem {
	static Logger logger = LogManager.getLogger(DownloadPage.class.getName());
	List<DownloadItem> downloadItems;
	BrowserWindow _window;
	ScrolledComposite _sc;
	Composite _contents;
	DownLoadMonitor progressMonitor;
	
	public DownloadPage(CTabFolder parent, int style, int index) {
		super(parent, style, index);
		this.addDisposeListener(new DisposeListener(){
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				progressMonitor.stopMonitor();
				progressMonitor = null;
			}
		});
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
		int y = 50;
		for (int i = downloads.size() - 1; i >= 0; i--) {
			Download download = downloads.get(i);
			DownloadItem item = new DownloadItem(this, _contents, download, y);
			downloadItems.add(item);
			y += item.getHeight() + 5;
		}
	    _contents.setSize(_contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	    _contents.layout();    
	}
	
	public void refreshProgress() {
		for (DownloadItem downloadItem : downloadItems) {
			downloadItem.refreshProgress(false);
		}
	}
	
	public void setHasDownloadComplete() {
		progressMonitor.setHasDownloadComplete();
	}
}
