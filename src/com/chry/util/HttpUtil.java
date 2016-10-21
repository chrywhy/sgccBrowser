package com.chry.util;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;

public class HttpUtil {
	public boolean allowMultiThreadDownload(HttpClient httpClient, URI url) throws IOException, ClientProtocolException, Exception {   
		HttpHead httpHead = new HttpHead(url);   
		HttpResponse response = httpClient.execute(httpHead);   
		int statusCode = response.getStatusLine().getStatusCode();   
		if(statusCode != 200) throw new Exception("资源不存在!");   
		
		//Content-Length   
		Header[] headers = response.getHeaders("Content-Length");   
		Long contentLength = 0L;
		if(headers.length > 0) {
		    contentLength = Long.valueOf(headers[0].getValue());   
		}
		httpHead.abort();

		boolean acceptRanges = false;
		httpHead = new HttpHead(url);   
		httpHead.addHeader("Range", "bytes=0-"+(contentLength-1));   
		response = httpClient.execute(httpHead);   
		if(response.getStatusLine().getStatusCode() == 206){   
		    acceptRanges = true;   
		}
		httpHead.abort();
		return acceptRanges;
	}
}
