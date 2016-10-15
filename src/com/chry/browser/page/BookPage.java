package com.chry.browser.page;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;

import com.chry.browser.BrowserWindow;
import com.chry.browser.bookmark.BookMark;
import com.chry.browser.bookmark.BookPageConstant;
import com.chry.browser.bookmark.action.CopyFileNamesToClipboardAction;
import com.chry.browser.bookmark.action.CreateNewFolderAction;
import com.chry.browser.bookmark.action.CutAction;
import com.chry.browser.bookmark.action.DeleteAction;
import com.chry.browser.bookmark.action.FilterAction;
import com.chry.browser.bookmark.action.OpenAction;
import com.chry.browser.bookmark.action.PasteAction;
import com.chry.browser.bookmark.action.RefreshAction;
import com.chry.browser.bookmark.action.RenameAction;
import com.chry.browser.bookmark.action.SelectAction;
import com.chry.browser.bookmark.action.UpAction;
import com.chry.browser.bookmark.logic.filter.AllowOnlyFoldersFilter;
import com.chry.browser.bookmark.logic.filter.FileNameFilter;
import com.chry.browser.bookmark.logic.filter.FileTypeFilter;
import com.chry.browser.bookmark.logic.sorter.BookmarkSorter;
import com.chry.browser.bookmark.provider.BookmarkTableContentProvider;
import com.chry.browser.bookmark.provider.BookmarkTableLabelProvider;
import com.chry.browser.bookmark.provider.FileTreeContentProvider;
import com.chry.browser.bookmark.provider.FileTreeLabelProvider;
import com.chry.browser.bookmark.util.FileUtil;
import com.chry.util.swt.layout.AutoResizeTableLayout;
import com.chry.util.swt.layout.BorderLayout;

public class BookPage  extends CTabItem {
	static Logger logger = LogManager.getLogger(BookPage.class.getName());

	private boolean isCut = false;

	private CutAction cut_action;
	private CopyFileNamesToClipboardAction copy_action;
	private DeleteAction delete_action;
	private OpenAction open_action;
	private PasteAction paste_action;
	private RenameAction rename_action;
	private SelectAction select_all_action;
	private SelectAction select_file_action;
	private SelectAction select_folder_action;
	private UpAction up_action;
	private CreateNewFolderAction create_new_folder_action;
	private RefreshAction refresh_action;

	BrowserWindow _window;

	protected MenuManager createPopMenuManager()
	{
		MenuManager menu_manager = new MenuManager();
		menu_manager.add(open_action);
		menu_manager.add(new Separator());
		menu_manager.add(copy_action);
		menu_manager.add(paste_action);
		menu_manager.add(cut_action);
		menu_manager.add(delete_action);
		menu_manager.add(new Separator());
//		menu_manager.add(select_all_action);
//		menu_manager.add(select_file_action);
//		menu_manager.add(select_folder_action);
//		menu_manager.add(new Separator());
		menu_manager.add(rename_action);
		menu_manager.add(create_new_folder_action);
		menu_manager.add(new Separator());
		menu_manager.add(refresh_action);
		return menu_manager;
	}

	private List< String > sortTypeList = BookPageConstant.getSortTypeList();

	private Tree tree;
	private Table table;

	private TableViewer tbv;
	private TreeViewer tv;

	CTabFolder _parent;

//	private DeleteAction delete_action = new DeleteAction( this );

	private void _initActions() {
		open_action = new OpenAction( this );
		copy_action = new CopyFileNamesToClipboardAction( this );
		delete_action = new DeleteAction( this );
		up_action = new UpAction( this );
		cut_action = new CutAction( this );
		paste_action = new PasteAction( this );
		rename_action = new RenameAction( this );
		select_all_action = new SelectAction( this, SelectAction.SELECT_TYPE_ALL );
		select_file_action = new SelectAction( this, SelectAction.SELECT_TYPE_FILE );
		select_folder_action = new SelectAction( this, SelectAction.SELECT_TYPE_FOLDER );
		create_new_folder_action = new CreateNewFolderAction( this );
		refresh_action = new RefreshAction(this);
	}

	public BookPage(BrowserWindow window) {
		this(window.getPageFolder(), SWT.BORDER | SWT.CLOSE);
		_window = window;
	}
	
	private BookPage(CTabFolder parent, int style) {
		super(parent, style);
		_parent = parent;
		_initActions();
		createContents();
	}
	
	public Shell getShell() {
		return _window.getShell();
	}
	
	public void setStatus(String statusText) {
		_window.setStatus(statusText);
	}
	
	public TreeViewer getTreeViewer() {
		return tv;
	}

	public TableViewer getTablebViewer() {
		return tbv;
	}
	public IStructuredSelection getTableSelection() {
		return ( IStructuredSelection ) ( tbv.getSelection() );
	}

	protected Control createContents() {
		SashForm sash_form = new SashForm( _parent, SWT.HORIZONTAL | SWT.NULL );
		this.setControl(sash_form);

		final Composite leftComposite = new Composite( sash_form, SWT.NONE );
		leftComposite.setLayout( new BorderLayout( 0, 0 ) );
		
		tv = new TreeViewer( leftComposite );

		tv.setContentProvider( new FileTreeContentProvider() );
		tv.setLabelProvider( new FileTreeLabelProvider() );
		tv.addFilter( new AllowOnlyFoldersFilter() );
		tv.setInput(BookMark.getRootBook());

		tree = tv.getTree();
		tree.setLayoutData( BorderLayout.CENTER );
		
		tv.addSelectionChangedListener( new ISelectionChangedListener()
		{
			public void selectionChanged( SelectionChangedEvent event )
			{
				IStructuredSelection selection = ( IStructuredSelection ) event.getSelection();
				Object selected_file = selection.getFirstElement();
				tv.expandToLevel( selected_file, 1 );
				tbv.setInput( selected_file );

				doFilter();
			}
		} );

		tv.refresh();

		final Composite rightComposite = new Composite( sash_form, SWT.NONE );
		rightComposite.setLayout( new BorderLayout( 0, 0 ) );
		tbv = new TableViewer( rightComposite, SWT.BORDER | SWT.FULL_SELECTION );
		tbv.setContentProvider( new BookmarkTableContentProvider() );
		tbv.setLabelProvider( new BookmarkTableLabelProvider() );
		tbv.setSorter( new BookmarkSorter() );
		tbv.setInput(BookMark.getBarBook());

		tbv.addDoubleClickListener( open_action );
		tbv.addSelectionChangedListener( open_action );
		tbv.addSelectionChangedListener( delete_action );
		
		table = tbv.getTable();
		AutoResizeTableLayout layout = new AutoResizeTableLayout(table);
		table.setLayout(layout);
		table.setHeaderVisible( true );

		TableColumn[] columns = new TableColumn[ sortTypeList.size() ];
		for( int i = 0; i < columns.length; i++ )
		{
			TableColumn column = columns[ i ];
			String columnName = sortTypeList.get( i );
			column = new TableColumn( table, SWT.FILL);
			layout.addColumnData(new ColumnWeightData(100));
			column.setText( sortTypeList.get( i ) );
			final int index = i;
			column.addSelectionListener( new SelectionAdapter()
			{
				public void widgetSelected( SelectionEvent e )
				{
					Map< String, String > sortTypeMap = BookPageConstant.getSortTypeMap();
					String sortTypeKey = sortTypeList.get( index );
					String sortTypeValue = sortTypeMap.get( sortTypeKey );
					doSort( sortTypeValue );
				}
			} );
		}

		tbv.addSelectionChangedListener( new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				// TODO Auto-generated method stub
			}
		} );

		MenuManager pop_menu_manager = createPopMenuManager();
		tbv.getTable().setMenu( pop_menu_manager.createContextMenu( tbv.getTable() ) );
		tbv.refresh();

		sash_form.setWeights( new int[]
		{ 1, 4 } );
		return sash_form;

	}
	
	public void refresh(){
		tbv.refresh();
		tv.refresh();
	}

	public void refresh(BookMark bookmark){
		tbv.refresh();
		tv.refresh();
	}

	public void updateBookBar(BookMark bookmark) {
		_window.updateBookBar(bookmark);
	}
	
	public void removeBookBar(BookMark bookmark) {
		_window.removeBookBar(bookmark);
	}
	
	public void addBookFolder(BookMark parentFolder, BookMark bookfolder) {
		_window.addBookFolder(parentFolder, bookfolder);
	}
	
	public void openFolder(BookMark folder) {
		tv.setExpandedState( folder, true );
		tv.setSelection( new StructuredSelection( folder ), true );
	}

	public void doSort( String sortTypeValue )
	{
		Map< String, Integer > sortMap = BookPageConstant.getSortMap();

		int column = sortMap.get( sortTypeValue );

		BookmarkSorter fileSorter = ( BookmarkSorter ) tbv.getSorter();
		fileSorter.doSort( column );
		tbv.refresh();

	}

	public void doFileter( String fileName, String fileType )
	{
		tbv.resetFilters();
		tbv.addFilter( new FileNameFilter( fileName ) );
		tbv.addFilter( new FileTypeFilter( fileType ) );
		tbv.refresh();
	}

	public void doFilter()
	{
		doFileter( "", "" );
	}

	public boolean isCut()
	{
		return isCut;
	}

	public void setCut( boolean isCut )
	{
		this.isCut = isCut;
	}
}
