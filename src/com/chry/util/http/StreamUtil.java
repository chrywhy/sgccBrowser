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

import java.io.*;

public class StreamUtil {
    public static String inputStreamToString(InputStream is, IHttpLoadProgressListener listener) throws IOException {
        BufferedReader br = null;
        try {
            if (is == null) {
                return null;
            }
            if (listener != null) {
                listener.loadStart();
            }
            InputStreamReader isr;
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        }
        finally {
            if (is != null) {
                is.close();
            }
            if (br != null) {
                br.close();
            }
            if (listener != null) {
                listener.loadFinished(LoadEvent.OKEvent);
            }
        }
    }
    
    public static long inputStreamToOutputStream(InputStream is, OutputStream os, IHttpLoadProgressListener listener) {
        try {
            if (is == null) {
                return 0;
            }
            byte[] buf = new byte[1024];
            long count = 0;
            int len = 0;
            if (listener != null) {
                listener.loadStart();
            }
            while ((len = is.read(buf)) > 0) {
            	if (listener.isShutdown()) {
            		throw new InterruptedException("write Stopped !");
            	}
            	os.write(buf, 0, len);
                if (listener != null) {
                    listener.progress(len);
                }
                count += len;
            }
            os.flush();
            return count;
        } catch (Exception e) {
        	throw new HttpException(e);
        } finally {
        	try {
	            if (is != null) {
	                is.close();
	            }
	            if (os != null) {
	                os.close();
	            }
	            if (listener != null) {
	                listener.loadFinished(LoadEvent.OKEvent);
	            }
        	} catch (Exception e) {
            	throw new HttpException(e);
        	}
        }
    }
}
