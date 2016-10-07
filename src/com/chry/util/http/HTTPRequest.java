package com.chry.util.http;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPRequest {
	static Logger logger = LogManager.getLogger(HTTPRequest.class.getName());
	
    private String host = "";   // save the host we are requesting
    private String method = "";
    private String uri = "";
    private String version = "1.0";

    private String request = "";     // request line
    private HashMap<String, String> headers = new HashMap<String, String>(); // headers
    private String content = "";     // payload after '\r\n\r\n"

    public HTTPRequest() {
    }

    public HTTPRequest(String allrequest) throws IOException {
        parse(allrequest);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return this.host;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String ver) {
        if (ver.equalsIgnoreCase("1.1")) {
            this.version = ver;
        }
        else {
            this.version = "1.0";
        }
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getRequest() {
        return request;
    }

    public String getPayload() {
        return content;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        header = header.toUpperCase();

        if (headers.containsKey(header)) {
            return headers.get(header);
        }

        return "";
    }

    public void addHeader(String header, String value) {
        headers.put(header.trim().toUpperCase(), value.trim());
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        //buffer.append(request).append("\r\n");
        buffer.append(method).append(' ').append(uri).append(' ').append("HTTP/").append(version).append("\r\n");

        for (String key : headers.keySet()) {
            buffer.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }

        buffer.append("\r\n");
        buffer.append(content);

        return buffer.toString();
    }

    protected void parseRequestLine(String request) throws IOException {
        method = "";
        uri = "";
        version = "";

        try {
            StringTokenizer st = new StringTokenizer(request, " \t");
            method = st.nextToken();
            uri = st.nextToken();
            String protocol = st.nextToken().toUpperCase();
            if (protocol != null && protocol.equalsIgnoreCase("HTTP/1.1")) {
                version = "1.1";
            }
            else {
                version = "1.0";
            }
        }
        catch (Exception e) {
            throw new IOException("Invalid request line: " + request);
        }

        if (method.length() == 0 || uri.length() == 0) {
            throw new IOException("Invalid request line: " + request + ", missing method or URI");
        }
    }

    public void parse(String allrequest) throws IOException {
        request = "";
        content = "";
        headers.clear();

        if (allrequest == null) {
            throw new IOException("HTTPRequest: NULL request ");
        }

        BufferedReader reader = new BufferedReader(new CharArrayReader(allrequest.toCharArray()));
        try {
            // now pass the request to get request line, headers and contents ...
            request = reader.readLine();
            if (request == null || request.length() == 0) {
                throw new IOException("HTTPRequest: empty request line in request - " + allrequest);
            }

            parseRequestLine(request);

            logger.debug("Try parsing http request");

            // now, following this is headers
            String line;
            while ((line = reader.readLine()) != null) {
                // if we got an empty line, it means we have reached end of headers
                if (line.length() == 0) {
                    break;
                }

                int sep = line.indexOf(':');
                if (sep < 0) {
                    throw new IOException("HTTPRequest: invalid header line - " + line);
                }

                String h = line.substring(0, sep).trim();
                String v = line.substring(sep + 1).trim();
                logger.debug("Got header " + h + ":" + v);
                headers.put(h.toUpperCase(), v);
            }

            if (line != null) {
                // here, left are payload ...
                StringBuffer buffer = new StringBuffer();
                char[] buf = new char[1024];
                int nRead = 0;
                while ((nRead = reader.read(buf, 0, 1024)) > 0) {
                    buffer.append(buf, 0, nRead);
                }
                content = buffer.toString();

                logger.debug("Got payload - " + content);
            }
        }
        finally {
            reader.close();
        }
    }
};
