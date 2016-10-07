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
import java.util.zip.GZIPOutputStream;

public class HttpPostStream {

    protected final InputStream _inputStream;
    protected final String _contentEncoding;
    protected final String _contentType;
    protected final boolean _hasAlreadyEncoded;

    public HttpPostStream(InputStream is, String contentEncoding, String contentType, boolean hasAlreadyEncoded) {
        _inputStream = is;
        _contentEncoding = contentEncoding;
        _contentType = contentType;
        _hasAlreadyEncoded = hasAlreadyEncoded;
    }

    public String getContentEncoding() {
        return _contentEncoding;
    }

    public String getContentType() {
        return _contentType;
    }

    private boolean _needGzip() {
        return !_hasAlreadyEncoded && "gzip".equalsIgnoreCase(_contentEncoding);
    }

    public long encodeToStream(OutputStream outputStream) throws IOException {
        return encodeToStream(outputStream, null);
    }

    public long encodeToStream(OutputStream outputStream, IHttpLoadProgressListener listener) throws IOException {
        OutputStream os = outputStream;
        if (_needGzip()) {
            os = new GZIPOutputStream(outputStream);
        }
        return StreamUtil.inputStreamToOutputStream(_inputStream, os, listener);
    }
}
