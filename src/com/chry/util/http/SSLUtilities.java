package com.chry.util.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class SSLUtilities {
	static Logger logger = LogManager.getLogger(SSLUtilities.class.getName());
    private static final int _DEFAULT_PORT = 443;

    private static StrictHostnameVerifier _hostnameVerifier = new StrictHostnameVerifier();

    private static int CERT_CHECK_CONN_TIMOUT_MS = 5000;    //5 seconds
    private static int CERT_CHECK_READ_TIMOUT_MS = 5000;    //5 seconds


    /**
     * check certificates of a host & port
     */
    public static boolean checkCertificates(String host) throws Exception {
        return checkCertificates(host, _DEFAULT_PORT);
    }

    public static boolean checkCertificates(String host, long connTOInMS, long readTOInMS) throws Exception {
        return checkCertificates(host, _DEFAULT_PORT, connTOInMS, readTOInMS);
    }

    public static boolean checkCertificates(String host, int port) throws Exception {
        return checkCertificates(host, port, CERT_CHECK_CONN_TIMOUT_MS, CERT_CHECK_READ_TIMOUT_MS);
    }

    public static boolean checkCertificates(String host, int port, long connTOInMS, long readTOInMS) throws Exception {
        if(port <= 0) {
            throw new IllegalArgumentException("Invalid port - " + port);
        }

        SSLContext context = null;
        CustomTrustManager trustMgr = null;

        try {
            KeyStore ks = _readKeyStore();

            context = SSLContext.getInstance("TLS");
            String alg = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf;
            tmf = TrustManagerFactory.getInstance(alg);
            tmf.init(ks);

            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];

            trustMgr = new CustomTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{trustMgr}, null);
        }
        catch (Exception e) {
            logger.error("Cannot initialize SSL Context", "SSL may not work", String.format(
                                "host=%s, port=%d, msg=%s", host, port, e.getMessage()
                                ));
            throw e;
        }

        if (testSSLConnection(context, host, port, connTOInMS, readTOInMS)) {
            if(logger.isDebugEnabled()) {
                X509Certificate[] chain = trustMgr.chain;
                if(chain == null || chain.length == 0) {
                    logger.debug("Valid SSL certificates found but cannot retrieve it", "");
                }
                else {
                	logger.debug("Valid SSL Certificates found", String.format(
                                     "info=%s", _getCertificatesInfo(chain)
                                     ));
                }
            }

            return true;
        }

        X509Certificate[] chain = trustMgr.chain;
        if (chain == null || chain.length == 0) {
            logger.debug("Cannot get certificates from server", String.format(
                             "host=%s, port=%d", host, port
                             ));
            return false;
        }

        logger.info("Invalid or wrong SSL Certificates found", String.format(
                        "info=%s", _getCertificatesInfo(chain)
                        ));


        return false;
    }

    public static boolean testSSLConnection(SSLContext context, String host, int port, long connTOInMS, long readTOInMS) throws Exception {
        if (context == null) {
            context = SSLContext.getDefault();
        }

        Socket socketConn = new Socket();
        if(connTOInMS > 0) {
            socketConn.connect(new InetSocketAddress(host, port), (int)connTOInMS);
        }
        else {
        	logger.info("Invalid connect timeout", String.format("timeout=%dms", connTOInMS));
            socketConn.connect(new InetSocketAddress(host, port));
        }

        SSLSocketFactory factory = context.getSocketFactory();
        // SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        SSLSocket socket = (SSLSocket) factory.createSocket(socketConn, host, port, true);

        if(readTOInMS > 0) {
            socket.setSoTimeout((int)readTOInMS);
        }
        else {
        	logger.info("Invalid read timeout", String.format("timeout=%dms", readTOInMS));
        }

        try {
            socket.startHandshake();

            logger.info("Handshake started ... Check host names ...", String.format(
                            "host=%s, port=%d", host, port
                            ));

            _hostnameVerifier.verify(host, socket);

            logger.info("Server is trusted", "");
            return true;
        }
        catch (SSLException ex) {
        	logger.debug("SSLException caught.", String.format(
                             "host=%s, port=%d, msg=%s", host, port, ex.getMessage()
                             ), ex);

            return false;
        }
        catch(Exception e) {
        	logger.error("Unknown exception caught.", String.format(
                            "host=%s, port=%d, msg=%s", host, port, e.getMessage()
                            ));

            throw e;
        }
        finally {
            socket.close();
        }
    }

    // --------------------------------------------------------------------------------
    // helpers
    private static String _getCertificatesInfo(X509Certificate[] chain) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            sb.append(String.format("Found total %d certificates:\n", chain.length));

            for (X509Certificate cert : chain) {
                sha1.update(cert.getEncoded());
                md5.update(cert.getEncoded());
                sb.append("   Subject:    " + cert.getSubjectDN() + "\n");
                sb.append("   Issuer:     " + cert.getIssuerDN() + "\n");
                sb.append("   Type:       " + cert.getType() + "\n");
                sb.append("   SHA1:       " + _toHexString(sha1.digest()) + "\n");
                sb.append("   MD5:        " + _toHexString(md5.digest()) + "\n");
                sb.append("   Valid from: " + _dateToIsoString(cert.getNotBefore()) + "\n");
                sb.append("   Valid to:   " + _dateToIsoString(cert.getNotAfter()) + "\n");
                sb.append("\n");
            }
        }
        catch(Exception e) {
            sb.append("Cannot print certificates:\n");
            sb.append(e.toString());
        }

        return sb.toString();
    }

    private static char[] _passphrase = "changeit".toCharArray();
    private static KeyStore _readKeyStore() {
        // let's not use any key store ...
        return null;
/*
        File file = new File("jssecacerts");

        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }

        LogMsg.warn("Loading KeyStore from file", String.format("file=%s", file.getPath()));
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, _passphrase);
        in.close();

        return ks;
*/
    }

    private static final char[] _HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String _toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(':');
            }
            int b = (bytes[i] & 0xff);
            sb.append(_HEXDIGITS[b >> 4]);
            sb.append(_HEXDIGITS[b & 15]);
        }
        return sb.toString();
    }

    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss Z");
    private static String _dateToIsoString(Date date) {
        return _dateFormat.format(date);
    }

    private static class CustomTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        CustomTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        	logger.debug("Checking client certificates", String.format("authType=%s", authType));
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        	logger.debug("Checking server certificates", String.format("authType=%s", authType));

            this.chain = chain;

            tm.checkServerTrusted(chain, authType);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: SSLUtilities <host> [<port>]");
            System.exit(-1);
        }

        String host = args[0];
        int port = _DEFAULT_PORT;
        if(args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        checkCertificates(host, port);

/*
        try {
            System.setProperty( "http.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "http.proxyPort", "1080" );

            URL url = new URL("http://" + args[0]);
            java.net.HttpURLConnection uc = (java.net.HttpURLConnection)url.openConnection();
            uc.connect();
        
            String line;
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(uc.getInputStream()));
            while ((line = in.readLine()) != null){
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try {
            System.setProperty( "https.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "https.proxyPort", "1080" );

            URL url = new URL("https://" + args[0]);
            javax.net.ssl.HttpsURLConnection uc = (javax.net.ssl.HttpsURLConnection)url.openConnection();
            uc.connect();

            String line;
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(uc.getInputStream()));
            while ((line = in.readLine()) != null){
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
*/
        Properties props = new Properties();
        props.put("server", args[0]);
        props.put("ssl.enable", "true");
        if (args.length == 1) {
            System.setProperty("https.proxyHost", "wwwgate0-ch.mot.com");
            System.setProperty("https.proxyPort", "1080");

//            checkServerCertification("wwwgate0-ch.mot.com");
/*
            System.out.println("################Not using proxy ...");
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("#################Using proxy wwwgate0-ch ...");
            Properties sysProperties = System.getProperties();
            
            sysProperties.put( "http.proxyHost", "wwwgate0-ch.mot.com" );
            sysProperties.put( "http.proxyPort", "1080" );
            // sysProperties.put( "https.proxyHost", proxyHost );
            // sysProperties.put( "https.proxyPort", proxyPort );
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("#################Using proxy wwwgate0-ch again ...");
            System.setProperty( "http.proxyHost", "wwwgate0-ch.mot.com" );
            System.setProperty( "http.proxyPort", "1080" );
            // sysProperties.put( "https.proxyHost", proxyHost );
            // sysProperties.put( "https.proxyPort", proxyPort );
            try {
                checkServerCertification(new Service(props, null));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
*/
        }
        else {
            // initSSL(args[0], Integer.parseInt(args[1]));
        }
    }
}

