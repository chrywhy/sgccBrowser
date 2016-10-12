package com.chry.browser.bookmark.action;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import com.chry.browser.page.BookPage;

public class UpAction extends Action
{
	private BookPage _bookPage;

	public UpAction( BookPage bookPage )
	{
		_bookPage = bookPage;
		setText( "&上一级@Alt+U" );
//		setToolTipText( "Return to the previous folder" );
	}

	public void run()
	{
		TreeViewer tv = _bookPage.getTreeViewer();
		TableViewer tbv = _bookPage.getTablebViewer();
		IStructuredSelection selection = ( IStructuredSelection ) tv.getSelection();
		Object selected_file = selection.getFirstElement();
		if( selected_file == null )
		{
			return;
		}
		else
		{
			Object parent = ( ( File ) selected_file ).getParentFile();
			if( parent == null )
			{
				return;
			}
			else if( ( ( File ) parent ).getParent() == null )
			{
				tbv.setInput( parent );
				tv.setInput( parent );
			}
			else
			{
				tbv.setInput( parent );
				selection = ( IStructuredSelection ) tbv.getSelection();
				tv.setExpandedState( parent, false );
			}
		}
	}
}
