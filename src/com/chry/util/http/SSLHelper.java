package com.chry.util.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLHelper {
	static Logger logger = LogManager.getLogger(SSLHelper.class.getName());
    /*
    // 3DES
    public final static String SSL_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    public final static String SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA";
    public final static String SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA = "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA";

    // AES-128
    public final static String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";
    public final static String TLS_DHE_RSA_WITH_AES_128_CBC_SHA = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA";
    public final static String TLS_DHE_DSS_WITH_AES_128_CBC_SHA = "TLS_DHE_DSS_WITH_AES_128_CBC_SHA";

    // AES-256
    public final static String TLS_RSA_WITH_AES_256_CBC_SHA = "TLS_RSA_WITH_AES_256_CBC_SHA";
    public final static String TLS_DHE_RSA_WITH_AES_256_CBC_SHA = "TLS_DHE_RSA_WITH_AES_256_CBC_SHA";
    public final static String TLS_DHE_DSS_WITH_AES_256_CBC_SHA = "TLS_DHE_DSS_WITH_AES_256_CBC_SHA";
    */

    /**
     * Class for certificates handling.
     * Currently, we trust all certificates ...
     */
    public static class SantabaTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }


    public static void trustAllHttpsCertificates() throws Exception {
        //  Create a trust manager that does not validate certificate chains:
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];

        javax.net.ssl.TrustManager tm = new SantabaTM();

        trustAllCerts[0] = tm;

        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");

        sc.init(null, trustAllCerts, null);

        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        });
    }

    private static String[] enabledCiphers = null;

    public static void enforceStrongCiphers() throws Exception {
        // get supported ciphers
        TreeSet<String> ts = new TreeSet<String>(Collections.reverseOrder());

        SSLSocketFactory s = (SSLSocketFactory) SSLSocketFactory.getDefault();
        ts = new TreeSet<String>();
        for (String c : s.getSupportedCipherSuites()) {
            logger.debug("Default supported cipher - " + c);
            ts.add(c);
        }

        HashSet<String> desired = new HashSet<String>();

        /*
        // 3DES
        desired.add(SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA);
        desired.add(SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA);
        desired.add(SSL_RSA_WITH_3DES_EDE_CBC_SHA);

        // AES 128
        desired.add(TLS_DHE_DSS_WITH_AES_128_CBC_SHA);
        desired.add(TLS_DHE_RSA_WITH_AES_128_CBC_SHA);
        desired.add(TLS_RSA_WITH_AES_128_CBC_SHA);

        // AES 256
        desired.add(TLS_DHE_DSS_WITH_AES_256_CBC_SHA);
        desired.add(TLS_DHE_RSA_WITH_AES_256_CBC_SHA);
        desired.add(TLS_RSA_WITH_AES_256_CBC_SHA);

        SortedSet<String> supported = Collections.unmodifiableSortedSet(ts);
        desired.removeAll(supported);
        */

        // parse all algorithm names, make sure we favor AES & 3DES for encryption and SHA for integrity
        Pattern AES = Pattern.compile("AES_.*_CBC_SHA", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Pattern DES = Pattern.compile("3DES_.*_CBC_SHA", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        for (String c : ts) {
            Matcher m = AES.matcher(c);
            if (m.find()) {
                desired.add(c);
                continue;
            }

            m = DES.matcher(c);
            if (m.find()) {
                desired.add(c);
                continue;
            }
        }

        if (desired.isEmpty()) {
            logger.error("No desired cipher supported.");
        }
        else {
            // we have those ciphers enabled ...
            enabledCiphers = desired.toArray(new String[0]);

            for (String o : enabledCiphers) {
                logger.debug("Using desired cipher - " + o);
            }

            // use custom socket factory ...
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(new CustomSSLSocketFactory());
        }
    }

    // --------------------------------------------------------------------------------
    // helper, wrapper over the default SSL context
    static class CustomSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory defaultFactory;

        public CustomSSLSocketFactory() throws Exception {
            SSLContext context = SSLContext.getInstance("TLS");

            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];

            javax.net.ssl.TrustManager tm = new SantabaTM();

            trustAllCerts[0] = tm;

            context.init(null, trustAllCerts, null);

            defaultFactory = context.getSocketFactory();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket s1 = defaultFactory.createSocket(s, host, port, autoClose);
            _preConnect(s1);
            return s1;
        }

        @Override
        public synchronized Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            Socket s = defaultFactory.createSocket(host, port > -1 ? port : 443);
            _preConnect(s);
            return s;
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket s = defaultFactory.createSocket(host, port);
            _preConnect(s);
            return s;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost,
                                   int localPort) throws IOException, UnknownHostException {
            Socket s = defaultFactory.createSocket(host, port, localHost, localPort);
            _preConnect(s);
            return s;
        }

        @Override
        public Socket createSocket(InetAddress address, int port,
                                   InetAddress localAddress, int localPort) throws IOException {
            Socket s = defaultFactory.createSocket(address, port, localAddress, localPort);
            _preConnect(s);
            return s;
        }

        @Override
        public synchronized Socket createSocket() throws IOException {
            Socket s = defaultFactory.createSocket();
            _preConnect(s);
            return s;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return defaultFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return defaultFactory.getSupportedCipherSuites();
        }

        protected void _preConnect(Socket s) throws IOException {
            if (s instanceof SSLSocket) {
                if (enabledCiphers != null) {
                    ((SSLSocket) s).setEnabledCipherSuites(enabledCiphers);
                }
            }
        }
    }
}
