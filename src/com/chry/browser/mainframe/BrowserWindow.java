package com.chry.browser.mainframe;

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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.chry.browser.safe.LoginDialog;
import com.chry.browser.safe.SafeGate;
import com.chry.util.FileUtil;

import swing2swt.layout.BorderLayout;

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
    
    private Composite _centerArea;
    private ToolBar _bookBar;
    private Composite _pageArea;
    private CTabFolder _pageFolder;
    private WebPage _activePage = null;
    private WebPage _pageGenerator = null;

    private Composite _southArea;
//    private ProgressBar _progressBar;
    private Label _labelStatus;

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
        _shell.setText("国网自主双核安全浏览器V1.0");
        _shell.setLayout(new BorderLayout(0, 0));
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
    }
    
    private void _initMenuArea() {
    	_menu = new Menu(_shell, SWT.POP_UP);
//        _shell.setMenuBar(_menu);

        MenuItem menuItem_file = new MenuItem(_menu, SWT.CASCADE);
        menuItem_file.setText("文件");
        
        Menu newWindow = new Menu(menuItem_file);
        menuItem_file.setMenu(newWindow);
        
        MenuItem menuItem = new MenuItem(newWindow, SWT.NONE);
        menuItem.setText("新建窗口");
        menuItem.addSelectionListener(new SelectionAdapter() {            
            public void widgetSelected(SelectionEvent arg0) {
                BrowserWindow window = new BrowserWindow();
                window.open();
            }            
        });
        
        MenuItem menuItem_save = new MenuItem(newWindow, SWT.NONE);
        menuItem_save.setText("保存网页");
        menuItem_save.addSelectionListener(new SelectionAdapter() {            
            public void widgetSelected(SelectionEvent e) {
/*                
                String pageText = _activePage.getBrowser().getText();
                FileDialog filedlg=new FileDialog(_shell, SWT.OPEN);
                filedlg.setText("文件选择");
                filedlg.setFilterPath(BrowserConfig.ROOT);
                String selected = filedlg.open();
                FileUtil.WriteStringToFile(pageText, selected);
*/
                _activePage.getBrowser().execute("document.execCommand('SaveAs')");
            }
        });
        
        new MenuItem(newWindow, SWT.SEPARATOR);
        
        MenuItem menuItem_exit = new MenuItem(newWindow, SWT.NONE);
        menuItem_exit.setText("退出");
        menuItem_exit.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                System.exit(0);
            }
        });
        
        MenuItem menuItem_collect = new MenuItem(_menu, SWT.CASCADE);
        menuItem_collect.setText("收藏");
        
        Menu menu_1 = new Menu(menuItem_collect);
        menuItem_collect.setMenu(menu_1);
        
        MenuItem menuItem_1 = new MenuItem(menu_1, SWT.NONE);
        menuItem_1.setText("添加到收藏夹");
        
        MenuItem menuItem_tools = new MenuItem(_menu, SWT.CASCADE);
        menuItem_tools.setText("工具");
        
        Menu menuTools = new Menu(menuItem_tools);
        menuItem_tools.setMenu(menuTools);
        
        MenuItem menuItem_download = new MenuItem(menuTools, SWT.NONE);
        menuItem_download.setText("下载管理");
        
        _itemLoginOrLogout = new MenuItem(menuTools, SWT.NONE);
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
        
        _itemAdminUser = new MenuItem(menuTools, SWT.NONE);
        _itemAdminUser.setText("用户管理");
        _itemAdminUser.setEnabled(false);    
        _itemAdminUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPage(WebPage.Type.SettingUserPage);
            }
        });
        
        _itemAdminSite = new MenuItem(menuTools, SWT.NONE);
        _itemAdminSite.setText("安全管理");
        _itemAdminSite.setEnabled(false);    
        _itemAdminSite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPage(WebPage.Type.SettingSitePage);
            }
        });
        
        MenuItem menuItem_help = new MenuItem(_menu, SWT.CASCADE);
        menuItem_help.setText("帮助");
        
        Menu menu_3 = new Menu(menuItem_help);
        menuItem_help.setMenu(menu_3);
        
        MenuItem menuItem_usehelp = new MenuItem(menu_3, SWT.NONE);
        menuItem_usehelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION|SWT.OK);
                messageBox.setMessage("文件菜单：主要包括新建窗口、保存网页以及退出三部分功能。 \n收藏菜单：用于收藏常用网页。\n工具菜单：包括下载管理、用户管理以及安全策略管理。\n帮助菜单：包括使用帮助、报告错误、升级和软件情况。");
                messageBox.open();
            }
        });
        menuItem_usehelp.setText("使用帮助");
        
        MenuItem menuItem_errorslog = new MenuItem(menu_3, SWT.NONE);
        menuItem_errorslog.setText("报告错误");
        
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
                    _menu.setVisible(true);
                }
        });        
        
        //Click Backward
        _btnBackward.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                Browser browser = _activePage.getBrowser();
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
                Browser browser = _activePage.getBrowser();
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
                if (_activePage.getType() != WebPage.Type.NormalPage) {
                    _activePage.setType(WebPage.Type.NormalPage);
                }
                _activePage.load(_inputUrl.getText().trim());
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
                _activePage.refreshOrStop();
            }
        });

        //Click search button
        _btnSearch.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                _activePage.load("http://www.baidu.com/s?wd=" + _inputUrl.getText().trim());
            }            
        });    
        //Click book button
        _btnBook.addListener(SWT.Selection, new Listener(){
            @Override
            public void handleEvent(Event e) {
                String sUrl = _activePage.getUrl();
                String title = _activePage.getText();
            }            
        });            
    }
    
    private ToolItem _renderBookMark(BookMark bookmark) {
        final ToolItem book = new ToolItem(_bookBar, SWT.NONE);
        String title = bookmark.name;
        if (title.length() > 10) {
            title = title.substring(0,10);
        }
        book.setImage(bookmark.getIcon());
        book.setText(title);
        if (bookmark.type==BookMark.Type.folder) {
        	Menu menu = new Menu(_shell, SWT.POP_UP);
            _createFolderMenu(menu, bookmark.children);
        	_attachFolderMenu(book, menu);
        } else {
            book.setToolTipText(title + "\n" + bookmark.url);
            book.setData("url", bookmark.url);
            book.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
	            	_window._activePage.load((String)book.getData("url"));
				}
            });
        }
        return book;
    }
    
    private void _initBookEvents() {
        
    }
    
    private void _initCenterArea() {
        _centerArea = new Composite(_shell, SWT.NONE);
        _centerArea.setLayoutData(BorderLayout.CENTER);
        _centerArea.setLayout(new BorderLayout(0, 0));
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
    	if (bookmark.type == BookMark.Type.folder) {
    		subItem = new MenuItem(menu, SWT.CASCADE);
    		Menu subMenu = new Menu(subItem);
    		subItem.setMenu(subMenu);
    		List<BookMark> children = bookmark.children;
			_createFolderMenu(subMenu, children);
    	} else {
    		subItem = new MenuItem(menu, SWT.NONE);
            subItem.setToolTipText(bookmark.name + "\n" + bookmark.url);
            subItem.setData("url", bookmark.url);
            subItem.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					_window._activePage.load((String)subItem.getData("url"));
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					_window._activePage.load((String)subItem.getData("url"));
				}                	
            });
    	}
        subItem.setText(bookmark.name);
        subItem.setImage(bookmark.getIcon());
    }
    
    private Menu _createFolderMenu(Menu menu, List<BookMark> bookmarks) {
        for (BookMark bookmark : bookmarks) {
        	_addBookmarkToMenu(menu, bookmark);
        }
        return menu;
    }

    private void _initBookArea() {
        _bookBar = new ToolBar(_centerArea, SWT.FLAT | SWT.RIGHT);
        _bookBar.setLayoutData(BorderLayout.NORTH);
        ToolItem booksItem = new ToolItem(_bookBar, SWT.NONE);
        booksItem.setText("收藏夹");
        booksItem.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/books.png"));
        booksItem.setToolTipText("收藏夹管理");
        _bookBarWidth = 0;
        int maxWidth = _shell.getBounds().width - 100;
        boolean isFull = false;
        Menu moreMenu = null;
    	ToolItem moreItem = null;
        for (BookMark bookmark : BookMark.bookMarks) {
        	if (!isFull) {
	            ToolItem bookItem = _renderBookMark(bookmark);
	            Rectangle rect = bookItem.getBounds();
	            int width = _bookBarWidth + rect.width;
	            if(width >= maxWidth) {
	                isFull = true;
	                bookItem.dispose();
	                moreItem = new ToolItem(_bookBar, SWT.NONE);
	                moreItem.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/more.png"));
	                moreItem.setToolTipText("更多书签");
	                _bookBarWidth += moreItem.getBounds().width;
	                moreMenu = new Menu(_shell, SWT.POP_UP);
	                _addBookmarkToMenu(moreMenu, bookmark);
	                _attachFolderMenu(moreItem, moreMenu);
	                continue;
	            }
	            _bookBarWidth = width;
        	} else if (moreMenu != null) {
    			_addBookmarkToMenu(moreMenu, bookmark);
        	} else {
        		logger.error("Unknown error : more Menu is NULL !");
        	}
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
            _activePage.setBrowserCore(_coreType, ieVer);
            _activePage.refresh();
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
                WebPage webPage = (WebPage)e.item;
                if (webPage.getType() != WebPage.Type.pageGenerator) {
                    _activePage = (WebPage)e.item;
                    _activePage.setCurUrl(_activePage.getUrl());
                }
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                WebPage webPage = (WebPage)e.item;
                if (webPage.getType() != WebPage.Type.pageGenerator) {
                    _activePage = (WebPage)e.item;
                    _activePage.setCurUrl(_activePage.getUrl());
                }
            }
        });
        
        _pageFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            public void close(CTabFolderEvent e) {
                if (_pageFolder.getItemCount() == 2) {
                    _activePage.load("about:blank");
                    _activePage.setImage(SWTResourceManager.getImage(BrowserWindow.class, "/com/chry/browser/resource/images/page.png"));
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
            if (_activePage.getType() == WebPage.Type.SettingSitePage 
               || _activePage.getType() == WebPage.Type.SettingUserPage ) {
                setUrl(_activePage.getType().title());
            }
            return;
        }
        if (_activePage.getType() != WebPage.Type.NormalPage) {
            _activePage.setType(WebPage.Type.NormalPage);
        }
        
        URL url = parseUrl(inputStr);
        String sUrl;
        if (url == null) {
            sUrl = "http://www.baidu.com/s?wd=" + inputStr ;
        } else {
            sUrl = url.toString();
        }
        _activePage.load(sUrl);
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
        if (_pageGenerator != null) {
            _pageGenerator.dispose();
        }
        _pageGenerator = WebPage.createPageGenerator(this);
        return webPage;
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
}
