/*************************************************************
 * This a Basic class used to handle Http Get/Post operation
 * It support Http Parameter configurations from conf file or properties
 * It support SSL and Proxy for HTTP, this class will not parse any contents
 * in the request or reponse, all the input is full URL (GET method) with a 
 * HttpInputStream (POST method), and it package the HTTP response to a HttpInputStream
 * as a response to applications. application can hande the response according its logic
 *************************************************************/
package com.chry.util.http;

import com.chry.util.DES;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;


public class HttpClient {
	static Logger logger = LogManager.getLogger(HttpClient.class.getName());
    private boolean _proxyEnabled = false;
    private int _connectTimeoutMillis = -1;
    private int _readTimeoutMillis = -1;
    private final HttpConf _httpConf;

    // ------------------------------------------------------------------------
    public HttpClient() {
        this(new HttpConf());
    }

    public HttpClient(Properties props) {
        this(new HttpConf(props));
    }

    public HttpClient(HttpConf httpConf) {
        _httpConf = httpConf;
        // initialize HTTP proxy settings
        try {
            this._proxyEnabled = httpConf.getProxyEnable();

            if (this._proxyEnabled) {
                ProxyHelper.setProxy(httpConf.getHttpProxyHost(),
                        httpConf.getHttpProxyPort(),
                        httpConf.getHttpProxyUser(),
                        DES.decrypt(httpConf.getHttpProxyPass()),
                        httpConf.getHttpProxyExclude());
            }
        }
        catch (Exception e) {
            logger.error("Can not initialize Http Client", e);
            throw e;
        }

        // set timeout
        setReadTimeoutSeconds(httpConf.getHttpReadTimeoutSeconds());
        setConnectTimeoutSeconds(httpConf.getHttpConnectTimeoutSeconds());

        if (_httpConf.isSslEnforceStrongCipher()) {
            try {
                logger.info("Try enforce strong ciphers");
                SSLHelper.enforceStrongCiphers();
            }
            catch (Exception e) {
            	logger.error("Failed to enforce strong Cipher", e);
            }
        }

        if(_httpConf.isSslTrustAll()) {
            try {
            	logger.info("Try trust all certificates");
                SSLHelper.trustAllHttpsCertificates();
            }
            catch (Exception e) {
            	logger.error("Failed to trust all certificates", e);
            }
        }

        logger.debug("Http Service Client is setup",
                String.format("readTimeout=%d, connectTimeout=%d", _readTimeoutMillis, _connectTimeoutMillis));
    }

    public HttpConf getHttpConf() {
        return _httpConf;
    }


    public void setConnectTimeoutMillis(int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        _connectTimeoutMillis = millis;
    }

    public long getConnectTimeoutMillis() {
        return _connectTimeoutMillis;
    }

    public void setConnectTimeoutSeconds(int seconds) {
        setConnectTimeoutMillis(seconds * 1000);
    }

    public void setReadTimeoutMillis(int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        _readTimeoutMillis = millis;
    }

    public long getReadTimeoutMillis() {
        return _readTimeoutMillis;
    }

    public void setReadTimeoutSeconds(int seconds) {
        setReadTimeoutMillis(seconds * 1000);
    }

    /**
     * Get a Http InputStream from a URI.
     *
     * @param sUrl - full Http URL
     * @return HttpInputStream - contain response information
     * @throws IOException
     */
    public HttpResponseStream get(String sUrl) {
        return get(sUrl, new HashMap<String, String>());
    }

    public HttpResponseStream get(String sUrl, boolean redirect) {
        return get(sUrl, new HashMap<String, String>(), redirect);
    }
    
    public HttpResponseStream get(String sUrl, Map<String, String> headers) {
    	return get(sUrl, headers, true);
    }
    
    public HttpResponseStream get(String sUrl, Map<String, String> headers, boolean redirect) {
    	logger.debug("Try get URL:" + "url=" + sUrl);
    	try {
	        URL url = new URL(sUrl);
	        HttpURLConnection urlcon = null;
	        String protocol = url.getProtocol();
	        if (protocol.equalsIgnoreCase("https")) {
	            urlcon = (HttpsURLConnection) url.openConnection();
	        }
	        else {
	            urlcon = (HttpURLConnection) url.openConnection();
	        }
	        // allow server to send back gzipped data
	        urlcon.setRequestProperty("Accept-Encoding", "gzip,deflate");
	        if(headers != null && headers.size() > 0) {
	            for(Map.Entry<String, String> e : headers.entrySet()) {
	            	logger.debug("Override header: " + String.format("header=%s, value=%s", e.getKey(), e.getValue()));
	                urlcon.setRequestProperty(e.getKey(), e.getValue());
	            }
	        }
	
	        if (_connectTimeoutMillis >= 0) {
	            urlcon.setConnectTimeout(_connectTimeoutMillis);
	        }
	        if (_readTimeoutMillis >= 0) {
	            urlcon.setReadTimeout(_readTimeoutMillis);
	        }
	        urlcon.setInstanceFollowRedirects(redirect);
	        urlcon.connect();
	
	        return new HttpResponseStream(urlcon);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    public HttpResponseStream post(String sUrl, HttpPostStream postStream) throws IOException {
        return post(sUrl, postStream, new HashMap<String, String>());
    }

    public HttpResponseStream post(String sUrl, HttpPostStream postStream, Map<String, String> headers) throws IOException {
        return post(sUrl, postStream, null, headers);
    }

    /**
     * Get a Http InputStream from a URI.
     *
     * @param sUrl        - full Http URL
     * @param inputStream - contain the contents that need to be posted to server
     * @param listener    - use to indicate the post progress
     * @throws IOException
     */

    public HttpResponseStream post(String sUrl, HttpPostStream postStream, IHttpLoadProgressListener listener) throws IOException {
        return post(sUrl, postStream, listener, new HashMap<String, String>());
    }

    public HttpResponseStream post(String sUrl, HttpPostStream postStream, 
                                   IHttpLoadProgressListener listener, Map<String, String> headers) throws IOException {
    	logger.debug("Try post URL: " + String.format("url=%s", sUrl));

        URL url = new URL(sUrl);
        HttpURLConnection urlcon = null;
        String protocol = url.getProtocol();
        if (protocol.equalsIgnoreCase("https")) {
            urlcon = (HttpsURLConnection) url.openConnection();
        }
        else {
            urlcon = (HttpURLConnection) url.openConnection();
        }

        urlcon.setRequestMethod("POST");
        urlcon.setRequestProperty("Accept-Encoding", "gzip,deflate");
        if(headers != null && headers.size() > 0) {
            for(Map.Entry<String, String> e : headers.entrySet()) {
            	logger.debug("Override header:" + String.format("header=%s, value=%s", e.getKey(), e.getValue()));
                urlcon.setRequestProperty(e.getKey(), e.getValue());
            }
        }

        urlcon.setDoOutput(true);
        if (_connectTimeoutMillis >= 0) {
            urlcon.setConnectTimeout(_connectTimeoutMillis);
        }
        if (_readTimeoutMillis >= 0) {
            urlcon.setReadTimeout(_readTimeoutMillis);
        }

        String encoding = postStream.getContentEncoding();
        if (encoding != null) {
            urlcon.setRequestProperty("Content-Encoding", postStream.getContentEncoding());
        }
        urlcon.setRequestProperty("Content-Type", postStream.getContentType());
        urlcon.connect();
        OutputStream os = urlcon.getOutputStream();
        try {
            postStream.encodeToStream(os, listener);
        }
        finally {
            os.close();
        }

        return new HttpResponseStream(urlcon);
    }

    public HttpResponseStream postFile(String sUrl, String fileName) throws IOException {
        return postFile(sUrl, fileName, new HashMap<String, String>());
    }

    public HttpResponseStream postFile(String sUrl, String fileName, Map<String, String> headers) throws IOException {
        return postFile(sUrl, fileName, null, headers);
    }

    public HttpResponseStream postFile(String sUrl, String fileName, IHttpLoadProgressListener listener) throws IOException {
        return postFile(sUrl, fileName, listener, new HashMap<String, String>());
    }

    public HttpResponseStream postFile(String sUrl, String fileName,
                                       IHttpLoadProgressListener listener, Map<String, String> headers) throws IOException {
        if (sUrl == null || fileName == null) {
            throw new IOException("null Url or null file name");
        }
        FileInputStream fis = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new IOException("No such file - " + fileName);
            }
            long size = file.length();
            String contentEncoding = (size >= _httpConf.getGzipThreshold()) ? "gzip" : null;
            fis = new FileInputStream(file);
            HttpPostStream hps = new HttpPostStream(fis, contentEncoding, "application/octet-stream", false);
            HttpResponseStream rspis = post(sUrl, hps, listener, headers);
            return rspis;
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}
