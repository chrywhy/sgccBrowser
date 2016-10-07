package com.chry.util.http;

import com.chry.util.PropConf;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpConf {
	static Logger logger = LogManager.getLogger(HttpConf.class.getName());

	private static final int _DEFAULT_SERVICE_CONNECT_TIMEOUT_SECONDS = 30;
    private static final int _DEFAULT_SERVICE_READ_TIMEOUT_SECONDS = 5;

    private final String _confFile;
    private PropConf _appConf = null;

    public HttpConf() {
        _confFile = null;
        _appConf = new PropConf(new Properties());
    }

    public HttpConf(String fullpath) {
        _confFile = fullpath;
    }

    public HttpConf(Properties props) {
        _confFile = null;
        _appConf = new PropConf(props);
    }

    private PropConf _getAppConf() throws IOException {
        if (_appConf == null) {
            if (_confFile == null) {
                throw new IOException("No Http Configuration file defined");
            }
            _appConf = new PropConf(_confFile);
            //This is not put in cnstructor, 
            //because I don't want the constructor throw any exception
            //so PropConf instance can be create without exception 
        }
        return _appConf;
    }


    public int getGzipThreshold() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getIntValue("gzip.threshold", 0).intValue();
        }
        catch (Throwable e) {
            logger.error("gzip.threshold", e);
        }
        return 0;
    }

    public boolean getProxyEnable() {
        try {
            PropConf propConf = _getAppConf();
            String val = propConf.getStringValue("proxy.enable", "false");
            return val.equalsIgnoreCase("true");
        }
        catch (Throwable e) {
            logger.error("proxy.enable", e);
        }
        return false;
    }

    public String getHttpProxyHost() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getStringValue("proxy.host", "127.0.0.1");
        }
        catch (Throwable e) {
            logger.error("proxy.host", e);
        }
        return "127.0.0.1";
    }

    public int getHttpProxyPort() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getIntValue("proxy.port", 1080).intValue();
        }
        catch (Throwable e) {
            logger.error("proxy.port", e);
        }
        return 1080;
    }

    public String getHttpProxyUser() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getStringValue("proxy.user", "");
        }
        catch (Throwable e) {
            logger.error("proxy.user", e);
        }
        return "";
    }

    public String getHttpProxyPass() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getStringValue("proxy.pass", "");
        }
        catch (Throwable e) {
            logger.error("proxy.pass", e);
        }
        return "";
    }

    public String getHttpProxyExclude() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getStringValue("proxy.exclude", "");
        }
        catch (Throwable e) {
            logger.error("proxy.exclude", e);
        }
        return "";
    }

    public int getHttpReadTimeoutSeconds() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getIntValue("service.read_timeout", _DEFAULT_SERVICE_READ_TIMEOUT_SECONDS).intValue();
        }
        catch (Throwable e) {
            logger.error("service.read_timeout", e);
        }
        return _DEFAULT_SERVICE_READ_TIMEOUT_SECONDS;
    }

    public int getHttpConnectTimeoutSeconds() {
        try {
            PropConf propConf = _getAppConf();
            return propConf.getIntValue("service.connect_timeout", _DEFAULT_SERVICE_CONNECT_TIMEOUT_SECONDS).intValue();
        }
        catch (Throwable e) {
            logger.error("service.connect_timeout", e);
        }
        return _DEFAULT_SERVICE_CONNECT_TIMEOUT_SECONDS;
    }

    public boolean isSslTrustAll() {
        try {
            PropConf propConf = _getAppConf();
            String trustAll = propConf.getStringValue("service.trustall", "true");
            return "true".equalsIgnoreCase(trustAll);
        }
        catch (Throwable e) {
            logger.error("service.trustall", e);
        }
        return true;
    }

    public boolean isSslEnforceStrongCipher() {
        try {
            PropConf propConf = _getAppConf();
            String trustAll = propConf.getStringValue("ssl.enforceStrongCipher", "true");
            return "true".equalsIgnoreCase(trustAll);
        }
        catch (Throwable e) {
            logger.error("ssl.enforceStrongCipher", e);
        }
        return true;
    }
}
