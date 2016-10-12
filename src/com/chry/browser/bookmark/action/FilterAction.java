package com.chry.browser.bookmark.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.chry.browser.page.BookPage;

public class FilterAction extends Action implements SelectionListener
{
	BookPage _bookPage;

	public FilterAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText( "&Filter@Ctrl+Shift+F" );
		setToolTipText( "Filter current folder" );
	}

	public void run()
	{
		//TODO become can input prarmet in the future
		_bookPage.doFilter();
	}

	public void widgetDefaultSelected( SelectionEvent e )
	{
		//do nothing
	}

	public void widgetSelected( SelectionEvent e )
	{
		_bookPage.doFilter();
		
	}
}
