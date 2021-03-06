package com.chry.util.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


// --------------------------------------------------------------------------------
// borrowed from apache AbstractVerifier
public class StrictHostnameVerifier implements HostnameVerifier {
	static Logger logger = LogManager.getLogger(SSLUtilities.class.getName());
    private SSLException _lastException = null;

    /**
     * This contains a list of 2nd-level domains that aren't allowed to
     * have wildcards when combined with country-codes.
     * For example: [*.co.uk].
     * <p/>
     * The [*.co.uk] problem is an interesting one.  Should we just hope
     * that CA's would never foolishly allow such a certificate to happen?
     * Looks like we're the only implementation guarding against this.
     * Firefox, Curl, Sun Java 1.4, 5, 6 don't bother with this check.
     */
    private final static String[] BAD_COUNTRY_2LDS =
            {"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
                    "lg", "ne", "net", "or", "org"};

    static {
        // Just in case developer forgot to manually sort the array.  :-)
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    public StrictHostnameVerifier() {
        super();
    }

    public SSLException getLastException() {
        return _lastException;
    }

    public void clearException() {
        _lastException = null;
    }

    public final void verify(String host, SSLSocket ssl)
            throws IOException {
        if (host == null) {
            throw new NullPointerException("host to verify is null");
        }

        ssl.startHandshake();
        SSLSession session = ssl.getSession();
        if (session == null) {
            // In our experience this only happens under IBM 1.4.x when
            // spurious (unrelated) certificates show up in the server'
            // chain.  Hopefully this will unearth the real problem:
            InputStream in = ssl.getInputStream();
            in.available();
            /*
              If you're looking at the 2 lines of code above because
              you're running into a problem, you probably have two
              options:

                #1.  Clean up the certificate chain that your server
                     is presenting (e.g. edit "/etc/apache2/server.crt"
                     or wherever it is your server's certificate chain
                     is defined).

                                           OR

                #2.   Upgrade to an IBM 1.5.x or greater JVM, or switch
                      to a non-IBM JVM.
            */

            // If ssl.getInputStream().available() didn't cause an
            // exception, maybe at least now the session is available?
            session = ssl.getSession();
            if (session == null) {
                // If it's still null, probably a startHandshake() will
                // unearth the real problem.
                ssl.startHandshake();

                // Okay, if we still haven't managed to cause an exception,
                // might as well go for the NPE.  Or maybe we're okay now?
                session = ssl.getSession();
            }
        }

        Certificate[] certs = session.getPeerCertificates();
        X509Certificate x509 = (X509Certificate) certs[0];
        verify(host, x509);
    }

    public final boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certs = session.getPeerCertificates();
            X509Certificate x509 = (X509Certificate) certs[0];
            verify(host, x509);
            return true;
        }
        catch (SSLException e) {
            return false;
        }
    }

    public final void verify(String host, X509Certificate cert)
            throws SSLException {
        String[] cns = getCNs(cert);
        String[] subjectAlts = getDNSSubjectAlts(cert);
        verify(host, cns, subjectAlts, true); // we want strict match ...
    }

    public final void verify(final String host, final String[] cns,
                             final String[] subjectAlts,
                             final boolean strictWithSubDomains)
            throws SSLException {

        // Build the list of names we're going to check.  Our DEFAULT and
        // STRICT implementations of the HostnameVerifier only use the
        // first CN provided.  All other CNs are ignored.
        // (Firefox, wget, curl, Sun Java 1.4, 5, 6 all work this way).
        LinkedList<String> names = new LinkedList<String>();
        if (cns != null && cns.length > 0 && cns[0] != null) {
            names.add(cns[0]);
        }
        if (subjectAlts != null) {
            for (String subjectAlt : subjectAlts) {
                if (subjectAlt != null) {
                    names.add(subjectAlt);
                }
            }
        }

        if (names.isEmpty()) {
            String msg = "Certificate for <" + host + "> doesn't contain CN or DNS subjectAlt";
            _lastException = new SSLException(msg);
            throw _lastException;
        }

        // StringBuffer for building the error message.
        StringBuffer buf = new StringBuffer();

        // We're can be case-insensitive when comparing the host we used to
        // establish the socket to the hostname in the certificate.
        String hostName = host.trim().toLowerCase(Locale.ENGLISH);
        boolean match = false;
        for (Iterator<String> it = names.iterator(); it.hasNext();) {
            // Don't trim the CN, though!
            String cn = it.next();
            cn = cn.toLowerCase(Locale.ENGLISH);
            // Store CN in StringBuffer in case we need to report an error.
            buf.append(" <");
            buf.append(cn);
            buf.append('>');
            if (it.hasNext()) {
                buf.append(" OR");
            }

            // The CN better have at least two dots if it wants wildcard
            // action.  It also can't be [*.co.uk] or [*.co.jp] or
            // [*.org.uk], etc...
            boolean doWildcard = cn.startsWith("*.") &&
                    cn.lastIndexOf('.') >= 0 &&
                    acceptableCountryWildcard(cn) &&
                    !_isIPv4Address(host);

            if (doWildcard) {
                match = hostName.endsWith(cn.substring(1));
                if (match && strictWithSubDomains) {
                    // If we're in strict mode, then [*.foo.com] is not
                    // allowed to match [a.b.foo.com]
                    match = countDots(hostName) == countDots(cn);
                }
            }
            else {
                match = hostName.equals(cn);
            }
            if (match) {
                break;
            }
        }
        if (!match) {
            _lastException = new SSLException("hostname in certificate didn't match: <" + host + "> !=" + buf);
            throw _lastException;
        }
    }

    public static boolean acceptableCountryWildcard(String cn) {
        int cnLen = cn.length();
        if (cnLen >= 7 && cnLen <= 9) {
            // Look for the '.' in the 3rd-last position:
            if (cn.charAt(cnLen - 3) == '.') {
                // Trim off the [*.] and the [.XX].
                String s = cn.substring(2, cnLen - 3);
                // And test against the sorted array of bad 2lds:
                int x = Arrays.binarySearch(BAD_COUNTRY_2LDS, s);
                return x < 0;
            }
        }
        return true;
    }

    public static String[] getCNs(X509Certificate cert) {
        LinkedList<String> cnList = new LinkedList<String>();
        /*
          Sebastian Hauer's original StrictSSLProtocolSocketFactory used
          getName() and had the following comment:

                Parses a X.500 distinguished name for the value of the
                "Common Name" field.  This is done a bit sloppy right
                 now and should probably be done a bit more according to
                <code>RFC 2253</code>.

           I've noticed that toString() seems to do a better job than
           getName() on these X500Principal objects, so I'm hoping that
           addresses Sebastian's concern.

           For example, getName() gives me this:
           1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d

           whereas toString() gives me this:
           EMAILADDRESS=juliusdavies@cucbc.com

           Looks like toString() even works with non-ascii domain names!
           I tested it with "&#x82b1;&#x5b50;.co.jp" and it worked fine.
        */
        String subjectPrincipal = cert.getSubjectX500Principal().toString();
        StringTokenizer st = new StringTokenizer(subjectPrincipal, ",");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int x = tok.indexOf("CN=");
            if (x >= 0) {
                cnList.add(tok.substring(x + 3));
            }
        }
        if (!cnList.isEmpty()) {
            String[] cns = new String[cnList.size()];
            cnList.toArray(cns);
            return cns;
        }
        else {
            return null;
        }
    }


    /**
     * Extracts the array of SubjectAlt DNS names from an X509Certificate.
     * Returns null if there aren't any.
     * <p/>
     * Note:  Java doesn't appear able to extract international characters
     * from the SubjectAlts.  It can only extract international characters
     * from the CN field.
     * <p/>
     * (Or maybe the version of OpenSSL I'm using to test isn't storing the
     * international characters correctly in the SubjectAlts?).
     *
     * @param cert X509Certificate
     * @return Array of SubjectALT DNS names stored in the certificate.
     */
    public static String[] getDNSSubjectAlts(X509Certificate cert) {
        LinkedList<String> subjectAltList = new LinkedList<String>();
        Collection<List<?>> c = null;
        try {
            c = cert.getSubjectAlternativeNames();
        }
        catch (CertificateParsingException cpe) {
        	logger.error("Error parsing certificate", "", cpe);
        }

        if (c != null) {
            for (List<?> aC : c) {
                List<?> list = aC;
                int type = ((Integer) list.get(0)).intValue();
                // If type is 2, then we've got a dNSName
                if (type == 2) {
                    String s = (String) list.get(1);
                    subjectAltList.add(s);
                }
            }
        }
        if (!subjectAltList.isEmpty()) {
            String[] subjectAlts = new String[subjectAltList.size()];
            subjectAltList.toArray(subjectAlts);
            return subjectAlts;
        }
        else {
            return null;
        }
    }

    /**
     * Counts the number of dots "." in a string.
     *
     * @param s string to count dots from
     * @return number of dots
     */
    public static int countDots(final String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }


    // --------------------------------------------------------------------------------
    // private helpers
    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static boolean _isIPv4Address(final String input) {

        return IPV4_PATTERN.matcher(input).matches();
    }

}
