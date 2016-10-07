package com.chry.browser.mainframe;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.chry.browser.safe.SafeGate;
import com.chry.browser.safe.Site;
import com.chry.util.FileUtil;
import com.chry.util.http.HttpException;
import com.chry.util.http.IHttpLoadProgressListener;
import com.chry.util.http.LoadEvent;
import com.chry.util.http.AsyncHttpClient;

public class WebPage extends CTabItem {
	static Logger logger = LogManager.getLogger(WebPage.class.getName());
	
	public static enum Type {
		NormalPage("新空白页"), ViewSourcePage("页面源码"), DownloadPage("下载内容"), SettingUserPage("设置:用户"), SettingSitePage("设置：策略"), pageGenerator("+");
		String _title;
		private Type(String title) {
			_title = title;
		}
		public String title(){
			return _title;
		};
	};
	
	private WebPage.Type _type;
	private String _injectJS = "";
	private Browser _browser;
	private Browser _browserIE;
	private Browser _browserIE6;
	private Browser _browserFF;
//	private StackLayout _browserLayout;
	private BrowserWindow _window;
	private int _loadPercent = 100;
	private boolean _loadCompleted = true;
	private String _curUrl = "";
	private String _newUrl = "";
	IHttpLoadProgressListener _iconListener;
	private AsyncHttpClient _httpClient;
	
	private WebPage(CTabFolder pageFolder, int style) {
		super(pageFolder, style);
    	addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (_type == Type.NormalPage) {
					_browserIE.dispose();
					_browserFF.dispose();
					if (_httpClient != null) {
						_httpClient.clear();
					}
				}
			}
    	});
    	_iconListener = new IHttpLoadProgressListener() {
			@Override
			public void loadStart() {
			}

			@Override
			public void loadFinished(LoadEvent e) {
				final String sUrl;
				if (e.status == LoadEvent.ERROR) {
					logger.warn("failed to get favicon: " + e.error.getMessage());
					sUrl = null;
				} else {
					sUrl = _curUrl;
				}
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				    	Image image = BrowserConfig.getIcon(sUrl);
						setImage(image);
				    }
				}); 
			}
			
			@Override
			public void progress(int paramInt) {
			}
    	};
	}
	
    public static WebPage createBlankPage(BrowserWindow window) {
		WebPage webPage = new WebPage(window.getPageFolder(), SWT.CLOSE | SWT.BORDER);
		webPage._type = Type.NormalPage;
		webPage.setText(Type.NormalPage.title());
    	webPage._window = window;
    	window.setRefreshButton();
    	webPage._createBrowsers();
    	webPage.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/page.png"));
    	return webPage;
	}
    
    public static WebPage createSettingUserPage(BrowserWindow window) {
		WebPage webPage = new WebPage(window.getPageFolder(), SWT.CLOSE | SWT.BORDER);
		webPage._type = Type.SettingUserPage;
		webPage.setText(Type.SettingUserPage.title());
    	webPage._window = window;
		webPage._injectJS = "launchAdminUser.js";
    	webPage._curUrl = "https://" + BrowserConfig.SafePolicyServer + "/adminuser";
    	window.setRefreshButton();
    	webPage._createBrowsers();
    	webPage.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/setting.png"));
    	return webPage;
	}
    
    public static WebPage createSettingSitePage(BrowserWindow window) {
		WebPage webPage = new WebPage(window.getPageFolder(), SWT.CLOSE | SWT.BORDER);
		webPage._type = Type.SettingSitePage;
		webPage.setText(Type.SettingSitePage.title());
    	webPage._window = window;
		webPage._injectJS = "launchAdminSite.js";
    	webPage._curUrl = "https://" + BrowserConfig.SafePolicyServer + "/adminsite";
    	window.setRefreshButton();
    	webPage._createBrowsers();
    	webPage.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/setting.png"));
    	return webPage;
	}
    
    public static WebPage createDownloadPage(BrowserWindow window) {
		WebPage webPage = new WebPage(window.getPageFolder(), SWT.BORDER);
		webPage._type = Type.DownloadPage;
		webPage.setText(Type.DownloadPage.title());
    	webPage._window = window;
    	return webPage;
	}
    
    public static WebPage createPageGenerator(BrowserWindow window) {
		WebPage webPage = new WebPage(window.getPageFolder(), SWT.BORDER);
		webPage._type = Type.pageGenerator;
		webPage.setText(Type.pageGenerator.title());
    	webPage._window = window;
    	return webPage;
	}
    
    public Type getType() {
    	return _type;
    }
    
    private void _createBrowsers() {
		System.setProperty("org.eclipse.swt.browser.IEVersion","6000");
    	_browserIE6 = _createBrowser(getParent(), SWT.NONE);
		System.setProperty("org.eclipse.swt.browser.IEVersion","11000");
    	_browserIE = _createBrowser(getParent(), SWT.NONE);
    	_browserFF = _createBrowser(getParent(), SWT.MOZILLA);
		setBrowserCore(SWT.NONE, "11000");
    }
    
    private Browser _createBrowser(Composite composite, int browserType){
		Browser browser = new Browser(composite, browserType);
		new DomControl (_window, browser, "clickHyperLink");				
		browser.addProgressListener(new ProgressListener(){
			@Override
			public void changed(ProgressEvent e) {
				if (e.total > 0) {
					_loadPercent = (e.current * 100 / e.total);
					 logger.debug("progress_changed: " +  _browser.getUrl());
//					_window.appendStatus(_loadPercent + "%");;
//					_window.loadProgressChanged(e);				
				}
			}

			@Override
			public void completed(ProgressEvent e) {
				_loadCompleted = true;
				_window.setRefreshButton();
				String sUrl = _browser.getUrl();
				if (_type == Type.SettingUserPage || _type == Type.SettingSitePage ) {
					String token = SafeGate.getToken();
					String script = SWTResourceManager.loadFile(BrowserWindow.class, "/com/chry/browser/resource/js/" + _injectJS);
					script = script.replaceAll("#TOKEN#", token);
					try {
						_browser.evaluate(script);
						logger.info("inject token into JS: " + token);
					} catch(Exception ex) {
						logger.info("inject token into JS failed !", ex);
					}
				}
            	setCurUrl(_browser.getUrl());
//            	_window.refreshBookmarks();
            	logger.info("load Completed: " + _browser.getUrl());
            	_window.setStatus("加载结束");
			}
		});

		browser.addTitleListener(new TitleListener() { 
			@Override
			public void changed(TitleEvent event) {
				if(event.title.equalsIgnoreCase("about:blank")) {
					setText(Type.NormalPage.title());
				} else {
					setTitle(event.title);
				}
			}
		});
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent e) {
				WebPage webPage = _window.createPage(_newUrl);
				e.browser = webPage._browser;
			}
		});
		browser.addLocationListener(new LocationAdapter() {
            @Override
		    public void changing(LocationEvent e) {
            	if (e.location.equalsIgnoreCase("about:blank")) {
                	_window.setStatus("");
            	} else {
            		Site blackSite = SafeGate.findBlackSite(e.location);
        	    	if(blackSite != null) {
        	    		stop();
        	    		load("http://" + BrowserConfig.SafePolicyServer + "/restrict?url=" + e.location);
           	        	return;
        	    		/*
        	        	MessageBox messageBox = new MessageBox(_window.getShell(), SWT.OK|SWT.ICON_WARNING);   
        	        	messageBox.setText("安全限制");;  
        	        	messageBox.setMessage(blackSite.getType() + ":" + blackSite.getRisk());  
        	        	messageBox.open();
        	        	*/
            		}
                	_window.setStatus("正在加载 " + e.location + "...");
    				logger.debug("正在加载:" + e.location + " ...");
            	}
				logger.debug("URL:'" + _browser.getUrl() + "',     " + "location:" + "'" + e.location + "'");
		    }

            @Override
		    public void changed(LocationEvent e) {
		    }
		});
		
		browser.addStatusTextListener(new StatusTextListener() {
			@Override
			public void changed(StatusTextEvent e) {
				if (_loadPercent < 100) {
//					label_status.setText(e.text);
				}
				try {
				    new URL(e.text); //check if it is a valid URL
					_newUrl = e.text;
                	_window.setStatus(_newUrl);
					logger.debug("New URL:" + "'" + _newUrl + "'");
				} catch (Exception ex) {
//                	_window.setStatus("");
					logger.debug("statusText_changed:" + "'" + _newUrl + "'");
				}
			}
		});
		return browser;
    }
/*    
    public void setComposit() {
		Composite compositeIE = new Composite(_window.getPageFolder(), SWT.NONE);  
		Composite compositeFF = new Composite(_window.getPageFolder(), SWT.NONE);  
        setControl(composite);
		composite.setLayoutData(BorderLayout.CENTER);
		_browserLayout = new StackLayout();
		composite.setLayout(_browserLayout);
		_setBrowserCore(_browserIE);
    }
*/
        
    public void setBrowserCore(int browserType, String ieVer) {
//		_browserLayout.topControl = _browser; 
    	_browserIE.setVisible(false);
    	_browserIE6.setVisible(false);
    	_browserFF.setVisible(false);
		if (browserType == SWT.MOZILLA) {
			_browser = _browserFF;
		} else if ("6000".equals(ieVer)){
			_browser = _browserIE6;
		} else {
			_browser = _browserIE;
		}
		_browser.setVisible(true);
		setControl(_browser);
    }
        
    public void setTitle(String txt) {
    	setText(txt);
    }
    
    public Shell getShell() {
    	return (Shell)getParent().getParent();
    }
    
    public void load(String urlString) {
    	
    	Site blackSite = SafeGate.findBlackSite(urlString);
    	if(blackSite != null) {
    		urlString = "http://" + BrowserConfig.SafePolicyServer + "/restrict?url=" + urlString;
        }
        _window.setStopButton();
		_loadPercent = 0;
		_loadCompleted = false;
		if (_type == Type.SettingSitePage || _type == Type.SettingUserPage) {
			SafeGate.register();
		}
    	setCurUrl(urlString);
		_browser.setUrl(urlString);
		_window.setStatus("正在连接 " + urlString + " ...");
    }
    
    public void startChangePageIcon() {
		int slashIndex;
		String urlString = _curUrl;
		if (urlString.startsWith("http://")) {
			slashIndex = urlString.indexOf("/", 7);
		} else if (urlString.startsWith("https://")) {
			slashIndex = urlString.indexOf("/", 8);
		} else {
			slashIndex = 0;
		}
		if (slashIndex != 0) {
			String iconUrl;
			if (slashIndex < 0) {
				iconUrl = urlString + "/favicon.ico";
			} else {
				iconUrl = urlString.substring(0, slashIndex) + "/favicon.ico";
			}
	    	URL url;
	    	String host = "";
	    	try {
				url = new URL(iconUrl);
				host = url.getHost();
			} catch (MalformedURLException e) {
				throw new HttpException(e);
			}
			String path = BrowserConfig.getIconPath(url);
			if (path == null) {
				path = BrowserConfig.FaviconPath + File.separator + host;
				if (_httpClient == null) {
					_httpClient = new AsyncHttpClient(_iconListener);
				}
				BrowserConfig.addNewSite(host);
				_httpClient.startDownload(iconUrl, path, "favicon.ico");
			} else {
				_iconListener.loadFinished(LoadEvent.OKEvent);
			}
		} 
    }
    
    public void refresh() {
		String url = _browser.getUrl();
		logger.info("reload:" + url);
		try {
			new URL(url);
			load(url);
		} catch (Exception e) {
		}
    }

    public void stop() {
		_window.setRefreshButton();
		_loadPercent = 100;
		_loadCompleted = true;
		_browser.stop();
    }
    
    public void refreshOrStop() {
    	if (_loadCompleted) {
    		refresh();
    	} else {
    		stop();
    	}
    }
    
    public Browser getBrowser() {
		return _browser;
    }
    
    public int loadPercent() {
    	return _loadPercent;
    }
    
    public boolean isLoadCompleted() {
    	return _loadCompleted;
    }
    
    public String getUrl() {
    	return _curUrl;
    }

    public void setCurUrl(String url) {
		if (_type == WebPage.Type.NormalPage) {
	    	_curUrl = url;
	    	_window.setUrl(_curUrl);
			startChangePageIcon();
		} else {
			_window.setUrl(_type.title());
		}
    }

    public void setType(Type type) {
    	_type = type;
    }
}
