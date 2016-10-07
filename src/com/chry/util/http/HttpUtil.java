/*
*****************************************************************************************************************
* Module Introduction:
* Access HTPP/HTTPS site via URL, and return response in a String
* for HTTPS, use trust all strategy
*
* Wang Huiyu   2012/05/27	Initial Version			
*****************************************************************************************************************
*/

package com.chry.util.http;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientParamBean;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpUtil {
    private static class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            return true;
        }
    }

    static public class Proxy {
        public String host;
        public int port;
        public String user;
        public String passwd;

        public Proxy(String host, int port, String user, String passwd) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.passwd = passwd;
        }
    }

    public static String accessUrl(String url)
            throws KeyManagementException,
                   UnrecoverableKeyException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   ClientProtocolException,
                   IOException {
        return accessUrl(url, true, null, 10000, 20000);
    }

    public static String accessUrl(String url, boolean handleRedirect, Proxy proxy, int connTimeout, int readTimeout)
            throws KeyManagementException,
                   UnrecoverableKeyException,
                   NoSuchAlgorithmException,
                   KeyStoreException,
                   ClientProtocolException,
                   IOException {
        String response = null;

        HttpParams params = new SyncBasicHttpParams();
        ClientParamBean bean = new ClientParamBean(params);
        bean.setHandleRedirects(handleRedirect);
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), connTimeout);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), readTimeout);
        if (proxy != null) {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(proxy.host, proxy.port),
                    new UsernamePasswordCredentials(proxy.user, proxy.passwd));
            HttpHost proxyHost = new HttpHost(proxy.host, proxy.port);
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
        }
        try {
            TrustAllStrategy trustAll;
            trustAll = new TrustAllStrategy();
            SSLSocketFactory socketFactory = new SSLSocketFactory(trustAll, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme("https", 443, socketFactory);
            httpclient.getConnectionManager().getSchemeRegistry().register(sch);

            HttpGet httpget = new HttpGet(url);
            httpget.getRequestLine();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            response = httpclient.execute(httpget, responseHandler);
        }
        finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }

    public static void main(String[] args) throws Exception {
        try {
            String responseBody;
            HttpUtil.Proxy proxy;
//            proxy = new HttpUtil.Proxy("203.66.187.251", 80, "", "");
//            responseBody = HttpUtil.accessUrl("http://www.baidu.com", false, proxy);
          proxy = null;
          responseBody = HttpUtil.accessUrl("https://why.chry.com/test/timeout", false, proxy, 5000, 5000);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
        }
        catch (UnknownHostException e) {
            System.out.println("HTTP: Fail - Unkown Host\n");
            e.printStackTrace();
        }
        catch (HttpHostConnectException e) {
            System.out.println("HTTP: Fail - Connection Refused\n");
            e.printStackTrace();
        }
        catch (HttpResponseException e) {
            System.out.println("HTTP: Fail - Status " + e.getStatusCode() + "\n");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("HTTP: Fail - Unknown Error\n");
            e.printStackTrace();
        }
    }
}
