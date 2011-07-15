package com.rhymestore.android.utils;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtils
{
    /**
     * Installs a {@link TrustManager} that ignores all SSL certificates in order to allow SSL
     * connections to any host.
     * 
     * @throws Exception If the <code>TrustManager</code> cannot be installed.
     */
    public static void installIgnoreCertTrustManager() throws Exception
    {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] {new IgnoreCertTrustManager()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new AcceptAllHostnameVerifier());
    }

    /**
     * {@link TrustManager} implementation that ignores the certificates.
     * 
     * @author Ignasi Barrera
     */
    private static class IgnoreCertTrustManager implements X509TrustManager
    {
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] certs, final String authType)
        {
            // Do nothing => client trusted
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] certs, final String authType)
        {
            // Do nothing => server trusted
        }
    };

    /**
     * {@link HostnameVerifier} implementation that accepts all hosts.
     * 
     * @author Ignasi Barrera
     */
    private static class AcceptAllHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(final String urlHostName, final SSLSession session)
        {
            // Allow all hosts
            return true;
        }

    }
}
