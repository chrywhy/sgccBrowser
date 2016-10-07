package com.chry.util.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.client.params.AuthPolicy;

public class HttpClientFactory {
	static Logger logger = LogManager.getLogger(HttpClientFactory.class.getName());

    // prepare HttpClient
    public static DefaultHttpClient createHttpClient(int connectTo, int readTo) throws Exception {
	    AbstractVerifier hv = new AbstractVerifier() {
            public void verify(String s, String as[], String as1[]) throws SSLException {
                try {
                verify(s, as, as1, false);
                }
                catch(SSLException sslexception) {
                logger.warn(String.format(
                                          "Invalid SSL certificate for %s: %s",
                                          s, sslexception.getMessage()
                                          ));
                }
            }

            public final String toString() {
                return "DUMMY_VERIFIER";
            }
	    };

        // First create a trust manager that won't care.
        X509TrustManager trustManager = new X509TrustManager()
        {
			@Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
                // Don't do anything.
            }
            
			@Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
                // Don't do anything.
            }
 
			@Override
           public X509Certificate[] getAcceptedIssuers() {
                // Don't do anything.
                return null;
            }
        };
 
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[] { trustManager }, null);
 
        org.apache.http.conn.ssl.SSLSocketFactory sf = new org.apache.http.conn.ssl.SSLSocketFactory(sslcontext);
        // sf.setHostnameVerifier(new org.apache.http.conn.ssl.AllowAllHostnameVerifier());
        sf.setHostnameVerifier(hv);
 
        // If you want a thread safe client, use the ThreadSafeConManager, but
        // otherwise just grab the one from the current client, and get hold of its
        // schema registry. THIS IS THE KEY THING.
        // ClientConnectionManager ccm = httpClient.getConnectionManager();
        // SchemeRegistry schemeRegistry = ccm.getSchemeRegistry();

        // --------------------------------------------------------------------------------
        HttpParams httpParams = new BasicHttpParams();

        // prepare for timeouts
        if(connectTo > 0) {
            logger.debug(String.format("Using Connecting TO=%d", connectTo));
            HttpConnectionParams.setConnectionTimeout(httpParams, connectTo);
        }

        if(readTo > 0) {
            logger.debug(String.format("Using Read TO=%d", readTo));
            HttpConnectionParams.setSoTimeout(httpParams, readTo);
        }

        int maxTo = connectTo + readTo;
        logger.debug(String.format("Using max request TO=%d", maxTo));

        ConnManagerParams.setTimeout(httpParams, maxTo);
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, "LLC Agent");
        HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
        HttpProtocolParams.setHttpElementCharset(httpParams, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(httpParams, true);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https", 443, sf));
        schemeRegistry.register(new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));

        SingleClientConnManager ccm = new SingleClientConnManager(httpParams, schemeRegistry);
        DefaultHttpClient httpclient = new DefaultHttpClient(ccm, httpParams);

        return httpclient;
    }

    public static String getResponseBodyAsString(HttpResponse resp) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream is = resp.getEntity().getContent();
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            is.close();
        }

        return sb.toString();
    }

    public static DefaultHttpClient createHttpClientNTLM(
        String domain,          // NTLM auth domain
        String host,            // NTLM auth host
        String username,        // NTLM auth user
        String password,        // NTLM auth pass
        int connectTo, 
        int readTo
        ) throws Exception {

        DefaultHttpClient httpclient = createHttpClient(connectTo, readTo);

        enableNTLMAuth(httpclient, domain, host, username, password);

        return httpclient;
    }

    public static void enableNTLMAuth(
        DefaultHttpClient httpclient,
        String domain,
        String host, 
        String username,
        String password
        ) throws Exception {
        // NTLM support
        // http://www.devdaily.com/java/jwarehouse/commons-httpclient-4.0.3/NTLM_SUPPORT.txt.shtml
        logger.debug(String.format(
                         "Enable NTLM authenticator with domain=%s, host=%s, username=%s, password=****(%d)",
                         domain, host, username, password.length()));

        httpclient.getAuthSchemes().register(AuthPolicy.NTLM, new NTLMSchemeFactory());
        
        httpclient.getCredentialsProvider().setCredentials(
            new AuthScope(host, -1), 
            new NTCredentials(username, password, host, domain)
            );
    }
}
