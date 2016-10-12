package com.chry.browser;

import org.eclipse.swt.browser.Browser;  
import org.eclipse.swt.browser.BrowserFunction;  

public class DomControl extends BrowserFunction {  
	private BrowserWindow _window;
    public DomControl(BrowserWindow window, Browser browser, String name) {  
        super(browser, name); 
        _window = window;
    }  
  
    @Override  
    public Object function(Object[] arguments) {  
        _window.setStatus("click Link : " + arguments[0]);
        return super.function(arguments);  
    }  
  
}  