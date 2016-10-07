package com.chry.util.http;

import java.util.Map;

public class SyncHttpClient extends HttpClient {
    public String access(String sUrl) {
    	HttpResponseStream rspStream = get(sUrl);
    	if (rspStream.getResponseCode() == 200) {
        	return rspStream.decodeToString();
    	} else {
    		return null;
    	}
    }

    public String access(String sUrl, Map<String, String> headers) {
    	HttpResponseStream rspStream = get(sUrl, headers);
    	if (rspStream.getResponseCode() == 200) {
        	return rspStream.decodeToString();
    	} else {
    		return null;
    	}
    }
}
