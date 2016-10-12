package com.chry.browser.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.chry.util.FileUtil;
import com.chry.util.swt.SWTResourceManager;

public class ImageConfig {
	private ImageConfig() {		
	}
	
	static Logger logger = LogManager.getLogger(ImageConfig.class.getName());
	
    public static Image getIcon(String sUrl) {
    	String iconPath;
    	if (sUrl == null || sUrl.trim().isEmpty()) {
    		iconPath = null;
    	} else {
    		iconPath = getIconPath(sUrl);
    	}
    	if (iconPath != null) {
	    	try {
		    	ImageLoader loader = new ImageLoader();
		    	ImageData[] imageData = loader.load(iconPath);
		    	Image image = null;
		    	if(imageData.length > 0){
		    		image = new Image(null, imageData[0].scaledTo(16, 16));
		    	}
		    	return image;
	    	} catch (Exception e) {
	    		logger.error("Invalid icon file : " + iconPath, e);
	    		FileUtil.deleteFile(iconPath);
	    	}
    	}
    	return SWTResourceManager.getImage(ImageConfig.class, "/com/chry/browser/resource/images/page.png");
    }
    
    public static Image getFolderIcon() {
    	return SWTResourceManager.getImage(ImageConfig.class, "/com/chry/browser/resource/images/folder.png");
    }
    
    public static Image getFileIcon() {
    	return SWTResourceManager.getImage(ImageConfig.class, "/com/chry/browser/resource/images/page.png");
    }
    
    public static Image getBookIcon() {
    	return SWTResourceManager.getImage(ImageConfig.class, "/com/chry/browser/resource/images/books.png");
    }
    
	public static String getIconPath(String sUrl) {
    	try {
        	URL url;
			url = new URL(sUrl);
			return getIconPath(url);
		} catch (MalformedURLException e) {
			logger.error("failed to set icon", e);
			return null;
		}
	}
	
	public static String getIconPath(URL url) {
		String host = url.getHost();
		String path = BrowserConfig.FaviconPath + File.separator + host + File.separator + "favicon.ico";
		if (FileUtil.exists(path)) {
    		return path;
    	} else {
    		return null;
    	}
	}
}
