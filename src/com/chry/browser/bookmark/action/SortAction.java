package com.chry.browser.bookmark.action;


import java.util.Map;

import org.eclipse.jface.action.Action;

import com.chry.browser.bookmark.BookPageConstant;
import com.chry.browser.page.BookPage;

public class SortAction extends Action

{
	private String sortType;
	private BookPage _bookPage;
	private Map<String, String> sortTypeMap = BookPageConstant.getSortTypeMap();

	public SortAction( String sortTypeKey, BookPage bookPage )
	{
		_bookPage = bookPage;
		this.sortType = sortTypeMap.get( sortTypeKey );
		setText( sortTypeKey );
		setToolTipText( "Sort" );
		//setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/sort.gif" ) ) );
	}

	
	public void run()
	{
		_bookPage.doSort( sortType );
	}

}
