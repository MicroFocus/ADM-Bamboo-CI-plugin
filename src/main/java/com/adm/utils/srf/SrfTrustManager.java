package com.adm.utils.srf;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

// To prevent man-in-the-middle attacks, hostname checks can be done to verify that the hostname in an end-entity certificate matches the targeted hostname.
// TLS does not require such checks, but some protocols over TLS (such as HTTPS) do.
// In earlier versions of the JDK, the certificate chain checks were done at the SSL/TLS layer, and the hostname verification checks were done at the layer over TLS.
// This class allows for the checking to be done during a single call to this class.
public class SrfTrustManager extends X509ExtendedTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine engine) throws CertificateException {
        // Empty body
    }
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        // Empty body
    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine engine) throws CertificateException {
        // Empty body
    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        // Empty body
    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // Empty body
    }
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // Empty body
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}