package com.chry.browser.safe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chry.browser.mainframe.BrowserConfig;
import com.chry.util.DES;
import com.chry.util.http.SyncHttpClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SafeGate {	
	private SafeGate() {
	}
	
	static Logger logger = LogManager.getLogger(SafeGate.class.getName());
	private static Site[] _blackSites;
	private static String _encryptToken;
	private static boolean _safeGateEnabled = true;

	public static void loadSafePolicy() {
		try {
			register();
			SyncHttpClient httpClient = new SyncHttpClient();		
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("token", SafeGate.getToken());
			String json = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/sites", headers);
	    	ObjectMapper mapper = new ObjectMapper(); 
			_blackSites = mapper.readValue(json, Site[].class);
	        for (int i = 0; i < _blackSites.length; i++) {
	        	String pattern = _blackSites[i].getPattern();
	        	try {
	        		pattern = pattern.replaceAll("\\.", "\\\\\\.");
	        		pattern = pattern.replaceAll("\\*", "\\.\\*");
	        		_blackSites[i].setPattern(pattern);
	        	} catch (Exception e) {
	        	}
	        	_blackSites[i].setPattern(pattern);
	        }
			_safeGateEnabled = true;
		} catch(Exception e) {
			logger.error("Failed to load safe policy, ingnore!");
			_safeGateEnabled = false;
		}
	}
	
	public static boolean isSafeGateEnabled() {
		return _safeGateEnabled;
	}
	
	public static Site findBlackSite(String sUrl) {
		if (_safeGateEnabled == false) {
			return null;
		}
		String pattern = "\\s*^https?://\\S+/restrict";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(sUrl);
		if (m.find()) {
			return null;
		}
		for (int i=0; i<_blackSites.length; i++) {
			try {
			    pattern = _blackSites[i].getPattern();
		        p = Pattern.compile(".*" + pattern + ".*");
		        m = p.matcher(sUrl);
		        if(m.find()) {
		        	return _blackSites[i];
		        }
			} catch (Exception e) {
				logger.warn(e);
			}
		}
    	return null;
	}
	
	public static void register() {
		try {
			SyncHttpClient httpClient = new SyncHttpClient();
			if (_encryptToken == null || _encryptToken.trim().isEmpty()) {
				_encryptToken = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/register/localhost");
				logger.info("registered to Safe Policy Server, encryptToken: " + _encryptToken );
			} else {
				String tokenOK = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/token/_encryptToken");
				if ("true".equalsIgnoreCase(tokenOK)) {
					logger.info("Already registered to Safe Policy Server encryptToken: " + _encryptToken );
					return;
				} else {
					_encryptToken = httpClient.access("https://" + BrowserConfig.SafePolicyServer + "/register/localhost");
					logger.info("registered to Safe Policy Server encryptToken: " + _encryptToken );
				}
			}
		} catch (Exception e) {
			logger.error("Cannot register to Safe Policy server !", e);
		}
	}
	
	public static String getToken() {
		return DES.decrypt(_encryptToken);
	}
}
