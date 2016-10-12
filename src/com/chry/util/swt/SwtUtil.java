package com.chry.util.swt;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;

public class SwtUtil {
	private SwtUtil() {
	}
	private static Clipboard clipboard = null;
	public static Clipboard getClipboard() {
		if( clipboard == null ) {
			clipboard = new Clipboard( Display.getCurrent() );
		}
		return clipboard;
	}
}
