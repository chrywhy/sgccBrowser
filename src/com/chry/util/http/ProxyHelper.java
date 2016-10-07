package com.chry.util.http;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyHelper {
	static Logger logger = LogManager.getLogger(ProxyHelper.class.getName());
    private static String _proxyHost = "";
    private static int _proxyPort = 0;
    private static String _proxyUser = "";
    private static String _proxyPass = "";
    private static boolean _enabled = false;

    public static void setProxyEnabled(boolean enabled) {
        logger.info(String.format(
                "Proxy set to %s",
                enabled ? "enabled" : "disabled"
        ));

        _enabled = enabled;
    }

    public static boolean isProxyEnabled() {
        return _enabled;
    }

    public static void setProxy(String proxyHost,
                                int proxyPort,
                                String userName,
                                String password,
                                String exclude) {
        logger.info(String.format(
                "Proxy set to enabled with host=%s, port=%d, user=%s, pass=xxxx, exclude=%s",
                proxyHost, proxyPort, userName, exclude
        ));

        Properties sysProperties = System.getProperties();
        sysProperties.put("http.proxyHost", proxyHost);
        sysProperties.put("http.proxyPort", "" + proxyPort);
        sysProperties.put("https.proxyHost", proxyHost);
        sysProperties.put("https.proxyPort", "" + proxyPort);
        if (exclude != null && exclude.length() > 0) {
            sysProperties.put("http.nonProxyHosts", exclude);
            sysProperties.put("https.nonProxyHosts", exclude);
        }

        _proxyHost = proxyHost;
        _proxyPort = proxyPort;
        _proxyUser = (userName == null) ? "" : userName;
        _proxyPass = (password == null) ? "" : password;

        if (userName != null && userName.length() > 0) {
            Authenticator.setDefault(new DefaultHTTPAuthenticator(userName, password));
        }

        _enabled = true;
    }

    public static boolean detect(URI uri) {
        // detect auto proxy settings
        // http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
        // http://www.rgagnon.com/javadetails/java-0085.html
        System.setProperty("java.net.useSystemProxies", "true");
        try {
            logger.info("Try detecting proxy for - " + uri.toString());

            List<Proxy> l = ProxySelector.getDefault().select(uri);
            if (l != null) {
                logger.info(String.format(
                        "Detected %d proxy for %s",
                        l.size(), uri.toString()
                ));

                for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
                    java.net.Proxy proxy = (java.net.Proxy) iter.next();

                    logger.info(String.format(
                            "Detected proxy %s of type %s",
                            proxy.toString(), proxy.type()
                    ));


                    InetSocketAddress addr = (InetSocketAddress) proxy.address();

                    if (addr == null) {
                        // this is direct connection!
                        continue;   // ignore this
                    }

                    logger.info(String.format(
                            "Detected auto proxy %s:%d (type=%s)",
                            addr.getHostName(), addr.getPort(), proxy.type()
                    ));

                    Properties sysProperties = System.getProperties();
                    sysProperties.put("http.proxyHost", addr.getHostName());
                    sysProperties.put("http.proxyPort", "" + addr.getPort());
                    sysProperties.put("https.proxyHost", addr.getHostName());
                    sysProperties.put("https.proxyPort", "" + addr.getPort());

                    return true;
                }
            }
            else {
                logger.info(String.format(
                        "Cannot get default proxy list for accessing %s",
                        uri.toString()
                ));
            }

            return false;
        }
        finally {
            System.setProperty("java.net.useSystemProxies", "false");
        }
    }


    public static boolean needAuthentication() {
        return (_proxyUser.length() > 0);
    }

    public static String getProxyHost() {
        return _proxyHost;
    }

    public static int getProxyPort() {
        return _proxyPort;
    }

    public static String getProxyAuthUser() {
        return _proxyUser;
    }

    public static String getProxyAuthPass() {
        return _proxyPass;
    }

    public static class DefaultHTTPAuthenticator extends Authenticator {
        private final String username;
        private final String password;

        public DefaultHTTPAuthenticator(String user, String pass) {
            this.username = user;
            this.password = pass;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }


    // 
    // 
    public static void main(String[] args) throws Exception {
        String url = "https://testbed.logicmonitor.com";
        if (args.length > 0) {
            url = args[0];
        }

        logger.info("Try detecting proxy for - " + url);

        if (!detect(new URI(url))) {
        	logger.info("Cannot detect any proxy.");
        }
        else {
        	logger.info(String.format(
                    "Detected proxy %s:%d",
                    System.getProperty("http.proxyHost"),
                    System.getProperty("http.proxyPort")
            ));
        }
    }
}
