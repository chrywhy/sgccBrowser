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

public class CutAction extends Action
{
	BookPage _bookPage;

	public CutAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText( "剪切@Ctrl+X" );
//		setImageDescriptor( ImageDescriptor.createFromURL( JExplorerUtil.newURL( "file:icons/cut.gif" ) ) );
	}

	public void run() {
		//TODO implements the function of cut
		Clipboard clipboard = SwtUtil.getClipboard();
		TextTransfer text_transfer = TextTransfer.getInstance();

		IStructuredSelection selection = _bookPage.getTableSelection();
		if(selection.isEmpty()){
			return;
		}
		StringBuffer string_buffer = new StringBuffer();
		for( Iterator i = selection.iterator(); i.hasNext(); ) {
			File file = ( File ) i.next();
			if( string_buffer.length() == 0 ) {
				string_buffer.append( file.getAbsolutePath() );
			} else {
				string_buffer.append( BookPageConstant.FILE_COPY_SEPARATOR );
				string_buffer.append( file.getAbsolutePath() );
			}
		}
		clipboard.setContents( 
				new Object[]{ string_buffer.toString() }, 
				new Transfer[]{ text_transfer } );
		
		_bookPage.setStatus( "cut " + selection.size() + " files" );
		_bookPage.setCut( true );
	}
}
