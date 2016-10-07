/***********************************************
 *  When send HTTP request(get/post), server will return response,
 *  The response could be different contend encoding and type, and it could
 *  have upload/download binary file, images. But there is common thing that all
 *  these content could be got from InputStream of the connection. So, HttpInputStream
 *  is a wrapper class for such InputStream, with help of HttpInputStream, HttpService
 *  needn't care what type of content in the get/post request or response, HttpService
 *  become simple to handle any type of get/post request or response
 *************************************************/

package com.chry.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class HttpResponseStream {

    private final String _contentEncoding;
    private final String _contentType;
    private final int _contentLength;
    private final HttpURLConnection _urlConn;

    public HttpResponseStream(HttpURLConnection urlConn) throws HttpException, IOException {
        // _inputStream = urlConn.getInputStream();
        // _errorStream = urlConn.getErrorStream();
        _contentEncoding = urlConn.getContentEncoding();
        _contentType = urlConn.getContentType();
        _contentLength = urlConn.getContentLength();
        _urlConn = urlConn;
    }

    public String getContentEncoding() {
        return _contentEncoding;
    }

    public String getContentType() {
        return _contentType;
    }

    public int getContentLength() {
        return _contentLength;
    }

    public HttpURLConnection getHttpURLConnection() {
        return _urlConn;
    }

    public int getResponseCode() {
        try {
            return _urlConn.getResponseCode();
        }
        catch (Throwable e) {
            return 0;
        }
    }

    public String getResponseMessage() {
        try {
            return _urlConn.getResponseMessage();
        }
        catch(Throwable e) {
            return "EXCEPTION: " + e.getMessage();
        }
    }

    public Map<String, List<String>> getHeaderFields() {
        return _urlConn.getHeaderFields();
    }

    public boolean isGzip() {
        return "gzip".equalsIgnoreCase(_contentEncoding);
    }

    public InputStream getDecodedStream() throws IOException {
        InputStream is = _urlConn.getInputStream();
        if (isGzip()) {
            is = new GZIPInputStream(is);
        }
        return is;
    }

    public String decodeToString() {
    	try {
	        int respCode = _urlConn.getResponseCode();
	        if (respCode != HttpURLConnection.HTTP_OK) {
	            throw new HttpException(respCode, _urlConn.getResponseMessage());
	        }
		
		    InputStream is = getDecodedStream();
		    return StreamUtil.inputStreamToString(is, null);
    	} catch(IOException e) {
    		throw new HttpException(e);
    	}
    }

    public String decodeToString(IHttpLoadProgressListener listener) {
    	try {
	        int respCode = _urlConn.getResponseCode();
	        if (respCode != HttpURLConnection.HTTP_OK) {
	            throw new HttpException(respCode, _urlConn.getResponseMessage());
	        }
		
		    InputStream is = getDecodedStream();
		    return StreamUtil.inputStreamToString(is, null);
    	} catch(IOException e) {
    		throw new HttpException(e);
    	}
    }
    
    public long decodeToStream(OutputStream os) throws IOException {
        return decodeToStream(os, null);
    }

    public long decodeToStream(OutputStream os, IHttpLoadProgressListener listener) throws IOException {
        int respCode = _urlConn.getResponseCode();
        if (respCode != HttpURLConnection.HTTP_OK) {
            throw new HttpException(respCode, _urlConn.getResponseMessage());
        }

        InputStream is = getDecodedStream();
        return StreamUtil.inputStreamToOutputStream(is, os, listener);
    }

    public long writeToStream(OutputStream os) throws IOException {
        return writeToStream(os, null);
    }

    public long writeToStream(OutputStream os, IHttpLoadProgressListener listener) {
    	try {
    		return StreamUtil.inputStreamToOutputStream(_urlConn.getInputStream(), os, listener);
    	} catch (IOException e) {
    		throw new HttpException(e);
    	}
    }

    public String errorToString() throws IOException {
        return StreamUtil.inputStreamToString(_urlConn.getErrorStream(), null);
    }
}
