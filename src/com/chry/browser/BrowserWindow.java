package com.chry.browser;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.chry.browser.bookmark.AddBookItemDialog;
import com.chry.browser.bookmark.BookMark;
import com.chry.browser.config.BrowserConfig;
import com.chry.browser.config.ImageConfig;
import com.chry.browser.download.Download;
import com.chry.browser.download.Download.Status;
import com.chry.browser.download.Downloads;
import com.chry.browser.page.BookPage;
import com.chry.browser.page.DownloadPage;
import com.chry.browser.page.WebPage;
import com.chry.browser.safe.LoginDialog;
import com.chry.browser.safe.SafeGate;
import com.chry.util.swt.SWTResourceManager;
import com.chry.util.swt.layout.BorderLayout;

public class BrowserWindow {    
    static Logger logger = LogManager.getLogger(BrowserWindow.class.getName());
    private BrowserWindow _window = this;
    private Shell _shell;
    private Menu _menu;
    private String _user;
    private int _userRole;

    private int _coreType = SWT.NONE;
    
    //define top toolbars
    private Composite _northArea;
    private ToolBar _toolBar;
    private ToolItem _btnForward;
    private ToolItem _btnBackward;
    private ToolItem _btnRefreshOrStop;
    private Text _inputUrl;
    private boolean _userInputChange = false;
    protected boolean _isInputKeyWords;
    private TextContentAdapter _textAda;
    private ContentProposalAdapter _inputAuto;
    private SimpleContentProposalProvider _proposalPrivider;
    
    private ToolBar _menuBar;
    private ToolItem _btnMenu;
    private MenuItem _itemSysMenu;
    private MenuItem _itemSave;
    private MenuItem _itemLoginOrLogout;
    private MenuItem _itemAdminUser;
    private MenuItem _itemAdminSite;
    
    private ToolItem _btnGo;
    private ToolItem _btnSearch;
    private ToolItem _btnBook;
    private ToolItem _btnSwitchCore;
    private Menu _menuCore;
    private MenuItem _mIE;
    private MenuItem _mIE6;
    private MenuItem _mFireFox;

    int _bookBarWidth;
    private Map<String, Menu> _menuFolders = new HashMap<String, Menu>();
    
    private Composite _centerArea;
    private ToolBar _bookBar;
    private Composite _pageArea;
    private CTabFolder _pageFolder;
    private CTabItem _activePage = null;
    private WebPage _pageGenerator = null;

    private Composite _southArea;
//    private ProgressBar _progressBar;
    private Label _labelStatus;
    private boolean _isBookBarFull = false;
    private Menu _moreMenu = null;

    int newPageHashCode;
    
    public BrowserWindow() {
        _shell = new Shell();
    }
    
    private void _initWindow() {
        _initTitleArea();
        _initWindowEvents();
        _initMenuArea();
        _initToolBarArea();
        _initToolBarEvents();
        _initCenterArea();
        _initBookArea();        
        _initBookEvents();
        _initPageArea();
        _initPageEvents();
        _initFootArea();        
        _initPageFolderEvent(); //events related to web page
    }
    
    private void _initTitleArea() {
        _shell.setToolTipText("国网自主虚拟化双核浏览器V1.0");
        _shell.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/sg-logo.png"));
        _shell.setText("国网自主双核安全浏览器V1.0(2016-10-23)");
        _shell.setLayout(new BorderLayout(0, 0));
    }
    
    private boolean _killDownloading() {
    	boolean forceStop = false;
		for (Download download: Downloads.getDownloads()) {
			if (download.getStatus() == Status.Downloading) {
				if (forceStop == false) {
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
				    MessageBox messageBox = new MessageBox(_shell, style);
				    messageBox.setText("还有下载任务未完成");
				    messageBox.setMessage("继续关闭将中断下载任务， 你确认继续吗？");
				    forceStop = messageBox.open() == SWT.YES;
				    if (!forceStop) {
				    	return false;
				    }
				}
				download.stop();
			}
		}
		return true;
    }
    
    private void _initWindowEvents(){
        _shell.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent arg0) {
            }

            @Override
            public void controlResized(ControlEvent arg0) {
                if (_bookBar != null) {
                    _bookBar.dispose();
                }
                _initBookArea();
            }
        });
        _shell.addListener(SWT.Close, new Listener() {  
            public void handleEvent(Event event) {  
            	event.doit = _killDownloading();
            }  
        });        
    }
    
    private void _initMenuArea() {
    	_menu = new Menu(_shell, SWT.POP_UP);

        _itemSave = new MenuItem(_menu, SWT.NONE);
        _itemSave.setText("保存网页");
        _itemSave.addSelectionListener(new SelectionAdapter() {            
            public void widgetSelected(SelectionEvent e) {
/*                
                String pageText = getActiveWebPage().getBrowser().getText();
                FileDialog filedlg=new FileDialog(_shell, SWT.OPEN);
                filedlg.setText("文件选择");
                filedlg.setFilterPath(BrowserConfig.ROOT);
                String selected = filedlg.open();
                FileUtil.WriteStringToFile(pageText, selected);
*/
            	if (getActivePage() instanceof WebPage) {
            		getActiveWebPage().getBrowser().execute("document.execCommand('SaveAs')");
            	}
            }
        });
        
        new MenuItem(_menu, SWT.SEPARATOR);
                                
        MenuItem menuItem_download = new MenuItem(_menu, SWT.NONE);
        menuItem_download.setText("下载管理");
        menuItem_download.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	_window.createDownloadPage();
            }
        });
        
        MenuItem menuItem_downloadFolder = new MenuItem(_menu, SWT.NONE);
        menuItem_downloadFolder.setText("下载目录...");
        menuItem_downloadFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	DirectoryDialog folderdlg = new DirectoryDialog(_shell);
            	folderdlg.setText("文件夹选择");
            	folderdlg.setFilterPath(BrowserConfig.DownloadFolder);
            	folderdlg.setMessage("请选择相应的文件夹");
            	BrowserConfig.DownloadFolder = folderdlg.open();
            }
        });

        _itemSysMenu = new MenuItem(_menu, SWT.NONE);
        _itemSysMenu.setText("开启系统右键菜单");
        _itemSysMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	BrowserConfig.EnableSysMenu = !BrowserConfig.EnableSysMenu;
            }
        });
        
        new MenuItem(_menu, SWT.SEPARATOR);

        _itemLoginOrLogout = new MenuItem(_menu, SWT.NONE);
        _itemLoginOrLogout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (_user == null || _user.isEmpty()) {
                    LoginDialog login = new LoginDialog(_window);
                    login.open();
                } else {
                    setLogin(null,"1");
                }
            }
        });
        _itemLoginOrLogout.setText("用户登录");
        _itemLoginOrLogout.setEnabled(SafeGate.isSafeGateEnabled());    
        
        _itemAdminUser = new MenuItem(_menu, SWT.NONE);
        _itemAdminUser.setText("用户管理");
        _itemAdminUser.setEnabled(false);    
        _itemAdminUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPage(WebPage.Type.SettingUserPage);
            }
        });
        
        _itemAdminSite = new MenuItem(_menu, SWT.NONE);
        _itemAdminSite.setText("安全管理");
        _itemAdminSite.setEnabled(false);    
        _itemAdminSite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPage(WebPage.Type.SettingSitePage);
            }
        });

        new MenuItem(_menu, SWT.SEPARATOR);
        
        MenuItem menuItem_help = new MenuItem(_menu, SWT.CASCADE);
        menuItem_help.setText("帮助");
        
        Menu menu_3 = new Menu(menuItem_help);
        menuItem_help.setMenu(menu_3);
        
        MenuItem menuItem_usehelp = new MenuItem(menu_3, SWT.NONE);
        menuItem_usehelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
                messageBox.setMessage("国网安全浏览器和后台安全策略服务器配合，可屏蔽黑名单风险网站，\n未连接安全服务器的情况下和普通浏览器一样使用。");
                messageBox.open();
            }
        });
        menuItem_usehelp.setText("使用帮助");
        
        MenuItem menuItem_errorslog = new MenuItem(menu_3, SWT.NONE);
        menuItem_errorslog.setText("报告错误");
        menuItem_errorslog.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	String text; 
            	if (SafeGate.isSafeGateEnabled()) {
            		text = "错误上报成功";
                    if (_user == null || _user.isEmpty()) {
                    	ReportErrorDialog reportDialog = new ReportErrorDialog(_window.getShell());
                    	reportDialog.open();
                    }
            	} else {
            		text = "安全服务器未连接，无法上报错误！";
            	}
                MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
                messageBox.setMessage(text);
                messageBox.open();
            }
        });
        
        new MenuItem(menu_3, SWT.SEPARATOR);
        
        MenuItem menuItem_update = new MenuItem(menu_3, SWT.NONE);
        menuItem_update.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
                messageBox.setMessage("已经是最新版本！");
                messageBox.open();
            }
        });
        menuItem_update.setText("在线升级");
        
        MenuItem menuItem_about = new MenuItem(menu_3, SWT.NONE);
        menuItem_about.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
                messageBox.setText("国网自主双核安全浏览器v1.0");
                messageBox.setMessage("\n国家电网公司 版权所有 \nCopyright © 2003-2016 State Grid Corporation of China (SGCC). \nAll rights reserved");
                messageBox.open();
            }
        });
        menuItem_about.setText("关于...");        

        MenuItem menuItem_exit = new MenuItem(_menu, SWT.NONE);
        menuItem_exit.setText("退出");
        menuItem_exit.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
            	if (_killDownloading()) {
            		System.exit(0);
            	}
            }
        });
    }
    
     private void _initToolBarArea() {
        _northArea = new Composite(_shell, SWT.NONE);
        _northArea.setLayoutData(BorderLayout.NORTH);
        _northArea.setLayout(new GridLayout(5, false));        
        
        _toolBar = new ToolBar(_northArea, SWT.FLAT | SWT.LEFT);
        //Add main menu button
        _btnMenu = new ToolItem(_toolBar, SWT.NONE);
        _btnMenu.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/logo.png"));
        _btnMenu.setToolTipText("菜单");
        _addSplitLine(_toolBar);
        //Add navigation button
        _btnBackward = new ToolItem(_toolBar, SWT.NONE);
        _btnBackward.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/backward.png"));
        _btnBackward.setToolTipText("往前");
//        _btnBackward.setText("往前");
//        _addSplitLine(toolBar);        
        _btnForward = new ToolItem(_toolBar, SWT.NONE);        
        _btnForward.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/forward.png"));
        _btnForward.setToolTipText("往后");
//        _btnForward.setText("往后");        
//        _addSplitLine(toolBar);        
        //Refresh & Stop Button
        _btnRefreshOrStop = new ToolItem(_toolBar, SWT.NONE);        
//        _addSplitLine(_toolBar1);
        
        //add url input box
        _inputUrl = new Text(_northArea, SWT.BORDER);
        _inputUrl.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));
        _textAda = new TextContentAdapter();
        _proposalPrivider = new SimpleContentProposalProvider(BrowserConfig.URLPROPOSAL);
        _proposalPrivider.setFiltering(true);
            
        //Go button
        _menuBar = new ToolBar(_northArea, SWT.FLAT | SWT.RIGHT);
        _menuBar.setVisible(false);
        _btnGo = new ToolItem(_menuBar, SWT.NONE);
        _btnGo.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/go.png"));
        _btnGo.setToolTipText("访问");
//        _btnGo.setText("GO");
                            
        //Search button
        _btnSearch = new ToolItem(_menuBar, SWT.NONE);
        _btnSearch.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/search.png"));
        _btnSearch.setToolTipText("搜索");

        _addSplitLine(_menuBar);
        //Switch Core Button
        _btnBook = new ToolItem(_menuBar, SWT.NONE);
        _btnBook.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/book.png"));
        _btnBook.setToolTipText("收藏");
        _addSplitLine(_menuBar);
        
        //Switch Core Button
        _btnSwitchCore = new ToolItem(_menuBar, SWT.NONE);
        _btnSwitchCore.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/FireFox.png"));
        _btnSwitchCore.setText("FireFox");
        _btnSwitchCore.setToolTipText("内核切换");

        _menuCore = new Menu(_shell, SWT.POP_UP);        
        _mIE = new MenuItem(_menuCore, SWT.CHECK);
        _mIE.setText("IE");
        
        _mIE6 = new MenuItem(_menuCore, SWT.CHECK);
        _mIE6.setText("IE6");
        
        _mFireFox = new MenuItem(_menuCore, SWT.CHECK);
        _mFireFox.setText("FireFox");        

        _mIE.setSelection(true);
    }
    private    void _initToolBarEvents() {
        //Click Menu Button
        _btnMenu.addSelectionListener(new SelectionAdapter() {  
            @Override
            public void widgetSelected(SelectionEvent e) {     
                    Rectangle rect = _btnMenu.getBounds();
                    Point pt = new Point(rect.x, rect.y + rect.height);
                    pt = _toolBar.toDisplay(pt);
                    _menu.setLocation(pt.x, pt.y);
                	_itemSave.setEnabled(getActivePage() instanceof WebPage);
                	_itemSysMenu.setText(BrowserConfig.EnableSysMenu ? "关闭系统菜单" : "开启系统菜单");
                    _menu.setVisible(true);
                }
        });        
        
        //Click Backward
        _btnBackward.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                Browser browser = getActiveWebPage().getBrowser();
                if (browser.isBackEnabled()) {
                    browser.back();
                    _btnForward.setEnabled(true);
                    if (!browser.isBackEnabled()) {
                        _btnBackward.setEnabled(false);
                    }
                }
            }
        });

        //Click Forward
        _btnForward.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                Browser browser = getActiveWebPage().getBrowser();
                if (browser.isForwardEnabled()) {
                    browser.forward();
                    _btnBackward.setEnabled(true);
                    if (!browser.isForwardEnabled()) {
                        _btnForward.setEnabled(false);
                    }
                }
            }
        });
                
        //Click "Go"
        _btnGo.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event event) {
            	WebPage webPage = getActiveWebPage();
                if (webPage.getType() != WebPage.Type.NormalPage) {
                	getActiveWebPage().setType(WebPage.Type.NormalPage);
                }
                webPage.load(_inputUrl.getText().trim());
            }            
        });
                
        //input address or type enter
        _inputUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 13){
                    _userInputChange = false;
                } else {
                    _userInputChange = true;
                }
            }
        });
        
        _inputAuto = new ContentProposalAdapter(_inputUrl, _textAda, _proposalPrivider, null, null);
        new Label(_northArea, SWT.NONE);
        new Label(_northArea, SWT.NONE);
        //click the selected proposer, or type enter
        _inputAuto.addContentProposalListener(new IContentProposalListener() {
            @Override
            public void proposalAccepted(IContentProposal p) {
                String proposal = p.getContent();
                _inputUrl.setText(proposal);
                loadOrSearch(proposal);
            }            
        });
        _inputAuto.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        _inputAuto.setLabelProvider(new LabelProvider() {
              @Override
              public String getText(Object element) {
                  IContentProposal proposal = (IContentProposal) element;
                  return proposal.getContent();
              }
              @Override
              public Image getImage(Object element) {
                  IContentProposal proposal = (IContentProposal) element;
                  String inputStr = proposal.getContent();
                  if (isKeyWords(inputStr)) {
                      return SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/search.png");
                  } else {
                      return SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/page.png");
                  }
              }
          });
        
        _inputUrl.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent e) {
                if (!_userInputChange) {
                    return;
                }
                String inputString = _inputUrl.getText().trim();
                if (isKeyWords(inputString)) {
                    BrowserConfig.KEYPROPOSAL[0] = inputString;
                    _proposalPrivider.setProposals(BrowserConfig.KEYPROPOSAL);
                } else {
                    BrowserConfig.URLPROPOSAL[BrowserConfig.URLPROPOSAL.length - 1] = inputString;
                    _proposalPrivider.setProposals(BrowserConfig.URLPROPOSAL);
                }
                IContentProposal[] proposals = _proposalPrivider.getProposals(inputString, 0);
                Rectangle rect = _inputUrl.getBounds();
                int newHeight = (proposals.length + 1) * rect.height;
                Point size = new Point(rect.width, newHeight);
                _inputAuto.setPopupSize(size);
            }            
        });
                
        //Click Switch Core --> select IE
        _mIE.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                _switchCore(_mIE, SWT.NONE, "11000");
            }
        });
        
        //Click Switch Core --> select IE6
        _mIE6.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                _switchCore(_mIE6, SWT.NONE, "6000");
            }
        });
        
        //Click Switch Core --> select fireFox
        _mFireFox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                _switchCore(_mFireFox, SWT.MOZILLA, "");
            }
        });
        
        _btnSwitchCore.addSelectionListener(new SelectionAdapter() {  
            @Override
            public void widgetSelected(SelectionEvent e) {     
                    Rectangle rect = _btnSwitchCore.getBounds();
                    Point pt = new Point(rect.x, rect.y + rect.height);
                    pt = _menuBar.toDisplay(pt);
                    _menuCore.setLocation(pt.x-50, pt.y);
                    _menuCore.setVisible(true);
                }
        });

        //Click Refresh or Stop
        _btnRefreshOrStop.addListener(SWT.Selection,new Listener(){
            @Override
            public void handleEvent(Event e) {
            	if (_activePage instanceof WebPage) {
            		_window.getActiveWebPage().refreshOrStop();
            	}
            }
        });

        //Click search button
        _btnSearch.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
            	_window.getActiveWebPage().load("http://www.baidu.com/s?wd=" + _inputUrl.getText().trim());
            }           
        });    
        //Click book button
        _btnBook.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                String sUrl = getActiveWebPage().getUrl();
                String title = getActiveWebPage().getText();
                
                AddBookItemDialog addBookItemDialog = new AddBookItemDialog(_window, title, sUrl);
                Rectangle rect = _btnBook.getBounds();
                Point pt = new Point(rect.x, rect.y + rect.height);
                pt = _menuBar.toDisplay(pt);
                addBookItemDialog.setLocation(pt.x - 300, pt.y);
                
                addBookItemDialog.open();
            }            
        });            
    }
    
    private ToolItem _renderBookMark(BookMark bookmark) {
        final ToolItem book = new ToolItem(_bookBar, SWT.NONE);
        if (bookmark.isFolder()) {
        	_attachFolderMenu(book, _menuFolders.get(bookmark.getName()));
        } else {
            book.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					BookMark bookmark = (BookMark)book.getData();
	            	_window.getActiveWebPage().load(bookmark.getUrl());
				}
            });
        }
        book.setData(bookmark);
        _refreshBookmark(book);
        return book;
    }
    
    private void _initBookEvents() {
        
    }
    
    private void _refreshBookmark(ToolItem bookItem) {
    	BookMark bookmark = (BookMark)bookItem.getData();
    	bookItem.setImage(bookmark.getIcon());
        String title = bookmark.getName();
        if (title.length() > 10) {
            title = title.substring(0,10);
        }
    	bookItem.setText(title);
    	if (bookmark.isUrl()) {
    		bookItem.setToolTipText(bookmark.getName() + "\n" + bookmark.getUrl());
    	}
    }
    
    public void refreshBookmarks() {
    	for (ToolItem bookItem : _bookBar.getItems()) {
    		_refreshBookmark(bookItem);
    	}
    }
    
    private void _mapBookFolderToMenu(BookMark bookmark) {
    	Menu menu = new Menu(_shell, SWT.POP_UP);
    	_addMenuInfo(menu);
        _createFolderMenu(menu, bookmark.getChildren());
		_menuFolders.put(bookmark.getName(), menu);
    }
    
    private void _initCenterArea() {
        _centerArea = new Composite(_shell, SWT.NONE);
        _centerArea.setLayoutData(BorderLayout.CENTER);
        _centerArea.setLayout(new BorderLayout(0, 0));
        for (BookMark bookmark : BookMark.getBooksOnBar()) {
        	if (bookmark.isFolder()) {
        		_mapBookFolderToMenu(bookmark);
        	}
        }
    }
    
    private static void _addMenuInfo(final Menu menu) {
        menu.addMenuListener(new MenuListener() {
			@Override
			public void menuHidden(MenuEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void menuShown(MenuEvent arg0) {
		        for (MenuItem item : menu.getItems()) {
		        	BookMark bookmark = (BookMark)item.getData();
		        	item.setText(bookmark.getName());
		        	item.setImage(bookmark.getIcon());
		        	if (bookmark.isUrl()) {
		        		item.setToolTipText(bookmark.getName() + "\n" + bookmark.getUrl());
		        	}
		        }
			}
        	
        });
    }
    
    private Menu _attachFolderMenu(final ToolItem folderItem, final Menu menu) {
        folderItem.addSelectionListener(new SelectionAdapter() {  
            @Override
            public void widgetSelected(SelectionEvent e) {     
                    Rectangle rect = folderItem.getBounds();
                    Point pt = new Point(rect.x, rect.y + rect.height);
                    pt = _bookBar.toDisplay(pt);
                    menu.setLocation(pt.x, pt.y);
                    menu.setVisible(true);
                }
        });
        return menu;
    }
    
    private void _addBookmarkToMenu(Menu menu, BookMark bookmark) {
    	final MenuItem subItem;
    	if (bookmark.isFolder()) {
    		subItem = new MenuItem(menu, SWT.CASCADE);
    		Menu subMenu = new Menu(subItem);
            _addMenuInfo(subMenu);
    		_menuFolders.put(bookmark.getName(), subMenu);
    		subItem.setMenu(subMenu);
    		List<BookMark> children = bookmark.getChildren();
			_createFolderMenu(subMenu, children);
    	} else {
    		subItem = new MenuItem(menu, SWT.NONE);
            subItem.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					_window.getActiveWebPage().load((String)subItem.getData("url"));
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					BookMark bookmark = (BookMark)subItem.getData();
					_window.getActiveWebPage().load(bookmark.getUrl());
				}                	
            });
    	}
        subItem.setData(bookmark);
    }
    
    private Menu _createFolderMenu(Menu menu, List<BookMark> bookmarks) {
        for (BookMark bookmark : bookmarks) {
        	_addBookmarkToMenu(menu, bookmark);
        }
        return menu;
    }

    private void _addBookmarkToBar(BookMark bookmark) {
        int maxWidth = _shell.getBounds().width - 100;
    	ToolItem moreItem = null;
    	if (!_isBookBarFull) {
            ToolItem bookItem = _renderBookMark(bookmark);
            Rectangle rect = bookItem.getBounds();
            int width = _bookBarWidth + rect.width;
            if(width >= maxWidth) {
            	_isBookBarFull = true;
                bookItem.dispose();
                moreItem = new ToolItem(_bookBar, SWT.NONE);
                moreItem.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/more.png"));
                moreItem.setToolTipText("更多书签");
                _bookBarWidth += moreItem.getBounds().width;
                _moreMenu = new Menu(_shell, SWT.POP_UP);
                _addMenuInfo(_moreMenu);
                _attachFolderMenu(moreItem, _moreMenu);
            } else {
            	_bookBarWidth = width;
            }
    	} 
    	if (_moreMenu != null) {
			_addBookmarkToMenu(_moreMenu, bookmark);
    	}
    }
    
    private void _initBookArea() {
        _bookBar = new ToolBar(_centerArea, SWT.FLAT | SWT.RIGHT);
        _bookBar.setLayoutData(BorderLayout.NORTH);
        ToolItem booksItem = new ToolItem(_bookBar, SWT.NONE);
        booksItem.setText("收藏夹");
        booksItem.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/books.png"));
        booksItem.setToolTipText("收藏夹管理");
        booksItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createBookPage();
			}
        	
        });
        _bookBarWidth = 0;
        _isBookBarFull = false;
        _moreMenu = null;
        for (BookMark bookmark : BookMark.getBooksOnBar()) {
        	_addBookmarkToBar(bookmark);
        }
    }
    
    private void _initPageArea() {
        _pageArea = new Composite(_centerArea, SWT.NONE);
        _pageArea.setLayoutData(BorderLayout.CENTER);
        _pageArea.setLayout(new FillLayout());
        
        _pageFolder = new CTabFolder(_pageArea, SWT.NONE);
    }
        
    private void _initPageEvents() {
        
    }

    private void _initFootArea() {
        _southArea = new Composite(_shell, SWT.NONE);
        _southArea.setLayoutData(BorderLayout.SOUTH);
        _southArea.setLayout(new GridLayout(3, false));

        //add status bar
        Composite statusbar = new Composite(_southArea, SWT.NONE);
        statusbar.setLayoutData(new GridData(GridData.FILL,SWT.LEFT, true, false, 1, 1));
        statusbar.setLayout(new GridLayout(1, false));
        _labelStatus = new Label(statusbar, SWT.NONE);
        String blanks = "          ";
        for (int i=0; i<5; i++) blanks += blanks;
        _labelStatus.setText(blanks); 
        
        //add progress bar
//        _progressBar = new ProgressBar(composite, SWT.NONE);
//        GridData data = new GridData(SWT.FILL,SWT.LEFT, true, false, 1, 1);
//        data.horizontalAlignment = GridData.BEGINNING;
//        _progressBar.setLayoutData(data);
//        _progressBar.setVisible(false);
                
        //add copyright information
        ToolBar toolBar = new ToolBar(_southArea, SWT.FLAT | SWT.RIGHT);
        ToolItem copyright = new ToolItem(toolBar, SWT.NONE);
        copyright.setDisabledImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/logo.png"));
        copyright.setEnabled(false);
        copyright.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/logo.png"));
        copyright.setText("国家电网公司专用版");
        new Label(_southArea, SWT.NONE);
    }
        
    private void _switchCore(MenuItem selectedItem, int coreType, String ieVer) {
        String curCore = _btnSwitchCore.getText().trim();
        String tarCore = selectedItem.getText().trim();
        if (!curCore.equals(tarCore)) {
            _mIE.setSelection(false);
            _mIE6.setSelection(false);
            _mFireFox.setSelection(false);
            selectedItem.setSelection(true);
            _coreType = coreType;
            System.setProperty("org.eclipse.swt.browser.IEVersion", ieVer);
            _btnSwitchCore.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/" + tarCore + ".png"));
            _btnSwitchCore.setText(tarCore);
            if (_activePage instanceof WebPage) {
            	getActiveWebPage().setBrowserCore(_coreType, ieVer);
            	getActiveWebPage().refresh();
            }
        }
    }
    
    private boolean _mouseLeftButtonDown = false;
    protected void _initPageFolderEvent() {
        _pageFolder.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                _mouseLeftButtonDown = false;
            }
        });
        
        _pageFolder.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                _mouseLeftButtonDown = false;
            }

            @Override
            public void mouseDown(MouseEvent e) {
                if(e.button == 1) {
                    _mouseLeftButtonDown = true;
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (e.button == 1 && _mouseLeftButtonDown) {
                    _mouseLeftButtonDown = false;
                    CTabFolder pageFolder = (CTabFolder)e.widget;
                    if (pageFolder.getSelection() == _pageGenerator) {
                        createPage();
                        setUrl("about:blank");
                    }
                }
            }
        });
        
        _pageFolder.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent arg0) {
                // TODO Auto-generated method stub                
            }            
        });
        
        _pageFolder.addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent arg0) {
                // TODO Auto-generated method stub                
            }

            @Override
            public void mouseExit(MouseEvent arg0) {
                // TODO Auto-generated method stub                
            }

            @Override
            public void mouseHover(MouseEvent arg0) {
                // TODO Auto-generated method stub                
            }
        });        
        
        _pageFolder.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            	if (e.item instanceof WebPage) {
                    WebPage webPage = (WebPage)e.item;
                    if (webPage.getType() != WebPage.Type.pageGenerator) {
	                    _activePage = webPage;
	                    webPage.setCurUrl(getActiveWebPage().getUrl());
                    }
            	} else if (e.item instanceof BookPage) {
                    BookPage bookPage = (BookPage)e.item;
                    _activePage = bookPage;
            	} else if (e.item instanceof DownloadPage) {
            		DownloadPage downloadPage = (DownloadPage)e.item;
            		_activePage = downloadPage;
            		downloadPage.refresh();
            	}
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
            	if (e.item instanceof WebPage) {
	                WebPage webPage = (WebPage)e.item;
	                if (webPage.getType() != WebPage.Type.pageGenerator) {
	                	String sUrl = webPage.getUrl().trim();
                		_window.setUrl(sUrl);
	                }
            	} else if (e.item instanceof BookPage) {
                    setUrl("");
	        	} else if (e.item instanceof DownloadPage) {
	        		setUrl("");
	        		DownloadPage downloadPage = (DownloadPage)e.item;
	        		downloadPage.refresh();
	        	}
                _activePage = (CTabItem)e.item;
            }
        });
        
        _pageFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            public void close(CTabFolderEvent e) {
                if (_pageFolder.getItemCount() == 2) {
                    getActiveWebPage().load("about:blank");
                    getActiveWebPage().setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/page.png"));
                    e.doit = false;
                 }
            }
        }); 
    }
    
    public void open() {
        open(null);
    }
    
    public void open(String url) {
        Display display = Display.getCurrent();
        Rectangle area = display.getClientArea();
        _shell.setSize(2 * area.width/3, 2 * area.height/3);
        _shell.setLocation(area.width / 2 - _shell.getSize().x/2, area.height / 2 - _shell.getSize().y/2);  
    	_initWindow();
        _shell.open();
        _shell.layout();
        _pageGenerator = WebPage.createPageGenerator(this);
        _activePage = createPage(url);
        _switchCore(_mIE, SWT.NONE, "default");
        _menuBar.setVisible(true);
        while (!_shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
                        
    protected void _addSplitLine(ToolBar toolBar) {
        ToolItem toolItem_1 = new ToolItem(toolBar, SWT.SEPARATOR);
        toolItem_1.setText("|");
    }
    
    protected void _initUrlInputBox(Composite composite) {
        _inputUrl = new Text(composite, SWT.BORDER);
        GridData gd_txtHttp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtHttp.widthHint = 350;
        _inputUrl.setLayoutData(gd_txtHttp);
    }
            
    public Shell getShell() {
        return _shell;
    }
        
    public void createNewPage() {
        final CTabItem comaTabItem = new CTabItem(_pageFolder, SWT.BORDER|SWT.CLOSE);  
        comaTabItem.setText("新空白页");  
        final Composite composite = new Composite(_pageFolder, SWT.NONE);  
        comaTabItem.setControl(composite);  
        final CTabItem newTabItem = new CTabItem(_pageFolder, SWT.BORDER);  
    }
    
    static String getResourceString(String key, Object[] args) { 
        try { 
            return MessageFormat.format(getResourceString(key), args); 
        } catch (MissingResourceException e) { 
            return key; 
        } catch (NullPointerException e) { 
            return "!" + key + "!"; 
        } 
    }
    
    static String getResourceString(String key) { 
        try { 
            return "key"; 
        } catch (MissingResourceException e) { 
            return key; 
        } catch (NullPointerException e) { 
            return "!" + key + "!"; 
        } 
    }
    
    public void setStopButton() {
        _btnRefreshOrStop.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/cross.png"));
        _btnRefreshOrStop.setToolTipText("停止");
//        _btnRefreshOrStop.setText("停止");
    }
    
    public void setRefreshButton() {
        _btnRefreshOrStop.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/refresh.png"));
        _btnRefreshOrStop.setToolTipText("刷新");
//        _btnRefreshOrStop.setText("刷新");
    }

    public static boolean isKeyWords(String inputStr) {
        return parseUrl(inputStr) == null;
    }
    
    public static URL parseUrl(String inputStr) {
        URL url = null;
        try {
            url = new URL(inputStr);
        } catch (Exception ex) {
            try {
                url = new URL("http://" + inputStr);
                if (!inputStr.contains(".")) {
                    url = null;                    
                } else if(inputStr.endsWith(".")) {
                    url = null;
                }
            } catch (Exception ex1) {
                url = null;                    
            }
        }
        return url;
    }
    
    public void loadOrSearch(String inputStr) {
        inputStr = inputStr.trim();
        if (inputStr.isEmpty()) {
            if (getActiveWebPage().getType() == WebPage.Type.SettingSitePage 
               || getActiveWebPage().getType() == WebPage.Type.SettingUserPage ) {
                setUrl(getActiveWebPage().getType().title());
            }
            return;
        }
        if (getActiveWebPage().getType() != WebPage.Type.NormalPage) {
        	getActiveWebPage().setType(WebPage.Type.NormalPage);
        }
        
        URL url = parseUrl(inputStr);
        String sUrl;
        if (url == null) {
            sUrl = "http://www.baidu.com/s?wd=" + inputStr ;
        } else {
            sUrl = url.toString();
        }
        getActiveWebPage().load(sUrl);
    }
    
    public void setUrl(String sUrl) {
        _inputUrl.setText(sUrl);
    }
    
    public void setStatus(String statusText) {
        _labelStatus.setText(statusText);
    }
    
    public void appendStatus(String addpendText) {
        _labelStatus.setText(_labelStatus.getText() + " " + addpendText);
    }
    
    public WebPage createPage() {
        return createPage(WebPage.Type.NormalPage, null);
    }
    
    public WebPage createPage(WebPage.Type type) {
        return createPage(type, null);
    }
    
    public WebPage createPage(String urlString) {
        return createPage(WebPage.Type.NormalPage, urlString);
    }
    
    public WebPage createPage(WebPage.Type pageType, String urlString) {
        WebPage webPage = null;
        switch(pageType) {
        case SettingUserPage:
            webPage = WebPage.createSettingUserPage(this);
            urlString = webPage.getUrl();
            break;
        case SettingSitePage:
            webPage = WebPage.createSettingSitePage(this);
            urlString = webPage.getUrl();
            break;
        case NormalPage:
            webPage = WebPage.createBlankPage(this);
            break;
        default:
            throw new RuntimeException("unsupported page type !");
        }
        _pageFolder.setSelection(webPage);
        _activePage = webPage;
        if (urlString != null && !urlString.trim().isEmpty()) {
            webPage.load(urlString);
        }
/*
        if (_pageGenerator != null) {
            _pageGenerator.dispose();
        }
        _pageGenerator = WebPage.createPageGenerator(this);
*/
        return webPage;
    }

    public BookPage createBookPage() {
		BookPage bookPage = new BookPage(this, _pageFolder.getItemCount()-1);
		bookPage.setText("书签管理");
		bookPage.setImage(ImageConfig.getBookIcon());
		_pageFolder.setSelection(bookPage);
		_activePage = bookPage;
		return bookPage;
    }
    
    public DownloadPage createDownloadPage() {
    	DownloadPage downloadPage = new DownloadPage(this, _pageFolder.getItemCount()-1);
    	downloadPage.setText("下载管理");
    	downloadPage.setImage(ImageConfig.getBookIcon());
		_pageFolder.setSelection(downloadPage);
		_activePage = downloadPage;
		downloadPage.refresh();
		downloadPage.startMonitor();
		return downloadPage;
    }
    
    public CTabFolder getPageFolder() {
        return _pageFolder;
    }

    public int getCoreType() {
        return _coreType;
    }

    public void setLogin(String user, String role) {
        _userRole = "0".equals(role) ? 0 : 1;
        _user = user;
        if (_user == null) {
            _itemLoginOrLogout.setText("用户登录");
            _itemAdminUser.setEnabled(false);
            _itemAdminSite.setEnabled(false);            
            return;
        } else {
            _itemLoginOrLogout.setText("用户退出");
        }
        if (_userRole == 0) {
            _itemAdminUser.setEnabled(true);            
            _itemAdminSite.setEnabled(true);            
        } else {
            _itemAdminSite.setEnabled(false);
            _itemAdminUser.setEnabled(false);            
        }
    }
    
    public void addNewBookmark(String folderName, BookMark newBookmark) {
    	Menu menu = _menuFolders.get(folderName);
    	if (menu == null) {
    		_addBookmarkToBar(newBookmark);
    	} else {
    		_addBookmarkToMenu(menu, newBookmark);
    	}
    }
    
    public void updateBookBar(BookMark bookmark) {
    	if (bookmark.getParent() == BookMark.getBarBook()) {
    		ToolItem[] items = _bookBar.getItems();
    		for (ToolItem item : items) {
    			if ((BookMark)item.getData() == bookmark) {
    				item.setText(bookmark.getName());
    			}
    		}
    	}
    }

    public void removeBookBar(BookMark bookmark) {
    	if (bookmark.getParent() == BookMark.getBarBook()) {
    		ToolItem[] items = _bookBar.getItems();
    		for (ToolItem item : items) {
    			if ((BookMark)item.getData() == bookmark) {
    				item.dispose();
    			}
    		}
    	}
    }

    public void addBookFolder(BookMark parentFolder, BookMark bookFolder) {
    	if (bookFolder.isFolder()) {
    		_mapBookFolderToMenu(bookFolder);
        	if (parentFolder == BookMark.getBarBook()) {
        		_addBookmarkToBar(bookFolder);
        	} else {
            	Menu menu = _menuFolders.get(parentFolder.getName());
        		_addBookmarkToMenu(menu, bookFolder);
        	}
    	}
    }
    
	public CTabItem getActivePage() {
		return _activePage;
	}
	
	public WebPage getActiveWebPage() {
		if (_activePage instanceof WebPage) {
			return (WebPage)_activePage;
		} else {
			int index = _pageFolder.getSelectionIndex();
			_activePage.dispose();
			_activePage = WebPage.createBlankPage(this, index);
			_pageFolder.setSelection(_activePage);
			return (WebPage)_activePage;
		}
	}
	
	public String saveAs(final String sUrl) {
		try {
			new URL(sUrl);
		} catch (Exception e){
    		return "";
		}
		Download download = new Download(sUrl);
        FileDialog filedlg = new FileDialog(_shell, SWT.SAVE);
        filedlg.setText("文件选择");
        filedlg.setFilterPath(BrowserConfig.DownloadFolder);
        filedlg.setFileName(download.getFilename());
        filedlg.setOverwrite(true);
        final String fileFullPath = filedlg.open();
        if (fileFullPath != null) {
	        download.setFilename(filedlg.getFileName());
	        download.setPath(filedlg.getFilterPath());
	        Downloads.add(download);
        }
        MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
        messageBox.setMessage("已经开始下载，请到下载管理页面查看进度");
        messageBox.open();
        return fileFullPath;
	}
}
