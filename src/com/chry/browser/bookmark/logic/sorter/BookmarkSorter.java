package com.chry.browser.bookmark.logic.sorter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.chry.browser.bookmark.BookMark;

public class BookmarkSorter extends ViewerSorter
{
	private int column;
	
	public void doSort( int column ) {
		this.column = column;
	}
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		//do category first
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2) 
        {
			return cat1 - cat2;
		}
        
		BookMark bookmark1 = (BookMark)e1;
		BookMark bookmark2 = (BookMark)e2;
		String str1 = bookmark1.getName();
		String str2 = bookmark2.getName();
		
		//the value of column determine the method of sort
		switch( column )
		{
			// don't sort
			case 0:
				return 0;
			//sort by name Desc
			case 1:
			{
				int nameDesc = str2.compareToIgnoreCase( str1 );
				return nameDesc;
			}
			//sort by name Asc
			case -1:
			{
				int nameAsc = str1.compareToIgnoreCase( str2 );
				return nameAsc;
			}
		}
		
		return 0;
	}
	
	public int category( Object element ) {
		BookMark bookamrk = (BookMark)element;
		return bookamrk.isFolder() ? 0 : 1;
	}

}
