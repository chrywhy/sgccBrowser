package com.chry.browser.bookmark.action;

import java.io.File;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import com.chry.browser.bookmark.BookPageConstant;
import com.chry.browser.page.BookPage;
import com.chry.util.swt.SwtUtil;

public class CopyFileNamesToClipboardAction extends Action
{
	private BookPage _bookPage;
	private static final String FILE_COPY_SEPARATOR = BookPageConstant.FILE_COPY_SEPARATOR;

	public CopyFileNamesToClipboardAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText("复制@Ctrl+C" );
	}

	public void run()
	{
		Clipboard clipboard = SwtUtil.getClipboard();
		TextTransfer text_transfer = TextTransfer.getInstance();

		IStructuredSelection selection = _bookPage.getTableSelection();
		if( selection.isEmpty() )
		{
			return;
		}
		
		StringBuffer string_buffer = new StringBuffer();
		for( Iterator i = selection.iterator(); i.hasNext(); )
		{
			File file = ( File ) i.next();
			if( string_buffer.length() == 0 )
			{
				string_buffer.append( file.getAbsolutePath() );
			}
			else
			{
				string_buffer.append( FILE_COPY_SEPARATOR );
				string_buffer.append( file.getAbsolutePath() );
			}
		}
		
		clipboard.setContents( 
				new Object[]{ string_buffer.toString() }, 
				new Transfer[]{ text_transfer } );		
	}
}
