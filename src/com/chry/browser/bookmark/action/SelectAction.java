package com.chry.browser.bookmark.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.chry.browser.page.BookPage;

public class SelectAction extends Action
{
	public static final int SELECT_TYPE_ALL = 1;
	public static final int SELECT_TYPE_FILE = 2;
	public static final int SELECT_TYPE_FOLDER = 3;
	
	private BookPage _bookPage;
	private int type;

	/**
	 * 
	 * @param bookPage
	 * @param type all = 1, file = 2, folder = 3
	 */
	public SelectAction( BookPage bookPage, int type )
	{
		
		_bookPage = bookPage;
		this.type = type;
		String text = "";
		String toolTipText = "";
		switch( type )
		{
			case SELECT_TYPE_ALL:
				text = "全选@Ctrl+A";
//				toolTipText = "Select all";
				break;
			case SELECT_TYPE_FILE:
				text = "选所有书签@Ctrl+F";
//				toolTipText = "Select all files";
				break;
			case SELECT_TYPE_FOLDER:
				text = "选所有文件夹@Ctrl+D";
//				toolTipText = "Select all folders";
				break;
		}
		
		setText( text );
//		setToolTipText( toolTipText );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/selectall.gif" ) ) );
	}

	public void run()
	{
		_bookPage.getTablebViewer().getTable().selectAll();
		IStructuredSelection selection = _bookPage.getTableSelection();
		List< Object > selectionList = new ArrayList< Object >();
		if( type == SELECT_TYPE_ALL )
		{
			return;
		}
		else if ( type == SELECT_TYPE_FILE )
		{	
			for( Iterator i = selection.iterator(); i.hasNext(); )
			{
				File file = ( File ) i.next();
				if( file.isFile() )
				{
					selectionList.add( file );
				}
			}
			_bookPage.getTablebViewer().setSelection( new StructuredSelection(selectionList.toArray()) );
			return;
		}
		else if( type == SELECT_TYPE_FOLDER )
		{
			for( Iterator i = selection.iterator(); i.hasNext(); )
			{
				File file = ( File ) i.next();
				if( file.isDirectory() )
				{
					selectionList.add( file );
				}
			}
			_bookPage.getTablebViewer().setSelection( new StructuredSelection(selectionList.toArray()) );
			return;
		}
		else
		{
			//throw new Exception("Wrong Pramater!");
		}

	}
}
