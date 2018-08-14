/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.adm.utils.uft.rest;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.HttpRequestDecorator;
import com.adm.utils.uft.sdk.ResourceAccessLevel;
import com.adm.utils.uft.sdk.Response;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RestClient implements Client {
    private final String _serverUrl;
    protected Map<String, String> _cookies = new HashMap<String, String>();
    private final String _restPrefix;
    private final String _webuiPrefix;
    private final String _username;
    private ProxyInfo proxyInfo;

    /**
     * Configure SSL context for the client.
     */
    static {
        // First create a trust manager that won't care.
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Don't do anything.
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Don't do anything.
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // Don't do anything.
                return null;
            }

        };
        // Now put the trust manager into an SSLContext.
        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[] { trustManager }, null);
        } catch (KeyManagementException e) {
            throw new SSEException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new SSEException(e);
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());

        //Ignore hostname verify
        HttpsURLConnection.setDefaultHostnameVerifier(
                new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession sslSession) {
                        return true;
                    }
                }
        );
    }

    public RestClient(String url, String domain, String project, String username) {

        if (!url.endsWith("/")) {
            url = String.format("%s/", url);
        }
        _serverUrl = url;
        _username = username;
        _restPrefix =
                getPrefixUrl(
                        "rest",
                        String.format("domains/%s", domain),
                        String.format("projects/%s", project));
        _webuiPrefix = getPrefixUrl("webui/alm", domain, project);
    }

    /**
     * Constructor for setting rest client properties including proxy info.
     */
    public RestClient(String url, String domain, String project, String username, ProxyInfo proxyInfo) {

        if (!url.endsWith("/")) {
            url = String.format("%s/", url);
        }
        _serverUrl = url;
        _username = username;
        this.proxyInfo = proxyInfo;
        _restPrefix =
                getPrefixUrl(
                        "rest",
                        String.format("domains/%s", domain),
                        String.format("projects/%s", project));
        _webuiPrefix = getPrefixUrl("webui/alm", domain, project);
    }

    @Override
    public String build(String suffix) {

        return String.format("%1$s%2$s", _serverUrl, suffix);
    }

    @Override
    public String buildRestRequest(String suffix) {

        return String.format("%1$s/%2$s", _restPrefix, suffix);
    }

    @Override
    public String buildWebUIRequest(String suffix) {

        return String.format("%1$s/%2$s", _webuiPrefix, suffix);
    }

    @Override
    public Response httpGet(
            String url,
            String queryString,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.GET, url, queryString, null, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    @Override
    public Response httpPost(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.POST, url, null, data, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    @Override
    public Response httpPut(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret = null;
        try {
            ret = doHttp(RESTConstants.PUT, url, null, data, headers, resourceAccessLevel);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    @Override
    public String getServerUrl() {

        return _serverUrl;
    }

    private String getPrefixUrl(String protocol, String domain, String project) {

        return String.format("%s%s/%s/%s", _serverUrl, protocol, domain, project);
    }

    /**
     * @param type
     *            http operation: get post put delete
     * @param url
     *            to work on
     * @param queryString
     * @param data
     *            to write, if a writable operation
     * @param headers
     *            to use in the request
     * @return http response
     */
    private Response doHttp(
            String type,
            String url,
            String queryString,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel) {

        Response ret;
        if ((queryString != null) && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) openConnection(proxyInfo, url);

            connection.setRequestMethod(type);

            Map<String, String> decoratedHeaders = new HashMap<String, String>();
            if (headers != null) {
                decoratedHeaders.putAll(headers);
            }

            HttpRequestDecorator.decorateHeaderWithUserInfo(
                    decoratedHeaders,
                    getUsername(),
                    resourceAccessLevel);

            prepareHttpRequest(connection, decoratedHeaders, data);
            connection.connect();
            ret = retrieveHtmlResponse(connection);
            updateCookies(ret);
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        return ret;
    }

    /**
     * @param connnection
     *            connection to set the headers and bytes in
     * @param headers
     *            to use in the request, such as content-type
     * @param bytes
     *            the actual data to post in the connection.
     */
    private void prepareHttpRequest(
            HttpURLConnection connnection,
            Map<String, String> headers,
            byte[] bytes) {

        // set all cookies for request
        connnection.setRequestProperty(RESTConstants.COOKIE, getCookies());

        setConnectionHeaders(connnection, headers);

        setConnectionData(connnection, bytes);
    }

    private void setConnectionData(HttpURLConnection connnection, byte[] bytes) {

        if (bytes != null && bytes.length > 0) {
            connnection.setDoOutput(true);
            try {
                OutputStream out = connnection.getOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
            } catch (Exception cause) {
                throw new SSEException(cause);
            }
        }
    }

    private void setConnectionHeaders(HttpURLConnection connnection, Map<String, String> headers) {

        if (headers != null) {
            Iterator<Map.Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Map.Entry<String, String> header = headersIterator.next();
                connnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    /**
     * @param connection
     *            that is already connected to its url with an http request, and that should contain
     *            a response for us to retrieve
     * @return a response from the server to the previously submitted http request
     */
    private Response retrieveHtmlResponse(HttpURLConnection connection) {

        Response ret = new Response();

        try {
            ret.setStatusCode(connection.getResponseCode());
            ret.setHeaders(connection.getHeaderFields());
        } catch (Exception cause) {
            throw new SSEException(cause);
        }

        InputStream inputStream;
        // select the source of the input bytes, first try 'regular' input
        try {
            inputStream = connection.getInputStream();
        }
        // if the connection to the server somehow failed, for example 404 or 500,
        // con.getInputStream() will throw an exception, which we'll keep.
        // we'll also store the body of the exception page, in the response data. */
        catch (Exception e) {
            inputStream = connection.getErrorStream();
            ret.setFailure(e);
        }

        // this takes data from the previously set stream (error or input)
        // and stores it in a byte[] inside the response
        ByteArrayOutputStream container = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int read;
        try {
            while ((read = inputStream.read(buf, 0, 1024)) > 0) {
                container.write(buf, 0, read);
            }
            ret.setData(container.toByteArray());
        } catch (Exception ex) {
            throw new SSEException(ex);
        }

        return ret;
    }

    private void updateCookies(Response response) {

        Iterable<String> newCookies = response.getHeaders().get(RESTConstants.SET_COOKIE);
        if (newCookies != null) {
            for (String cookie : newCookies) {
                int equalIndex = cookie.indexOf('=');
                int semicolonIndex = cookie.indexOf(';');
                String cookieKey = cookie.substring(0, equalIndex);
                String cookieValue = cookie.substring(equalIndex + 1, semicolonIndex);
                _cookies.put(cookieKey, cookieValue);
            }
        }
    }

    private String getCookies() {

        StringBuilder ret = new StringBuilder();
        if (!_cookies.isEmpty()) {
            for (Map.Entry<String, String> entry : _cookies.entrySet()) {
                ret.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }

        return ret.toString();
    }

    @Override
    public String getUsername() {

        return _username;
    }

    public static URLConnection openConnection(final ProxyInfo proxyInfo, String urlString) throws IOException {
        Proxy proxy = null;
        URL url = new URL(urlString);

        if (proxyInfo != null && org.apache.commons.lang.StringUtils.isNotBlank(proxyInfo._host) && org.apache.commons.lang.StringUtils.isNotBlank(proxyInfo._port)) {
            int port = Integer.parseInt(proxyInfo._port.trim());
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo._host, port));
        }

        if (proxy != null && org.apache.commons.lang.StringUtils.isNotBlank(proxyInfo._userName) && StringUtils.isNotBlank(proxyInfo._password)) {
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyInfo._userName, proxyInfo._password.toCharArray());    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
            Authenticator.setDefault(authenticator);
        }

        if (proxy == null) {
            return url.openConnection();
        }


        return url.openConnection(proxy);
    }

    /**
     * Set proxy configuration.
     * @param host
     * @param port
     * @param userName
     * @param password
     * @return proxyinfo instance
     */
    public static ProxyInfo setProxyCfg(String host, String port, String userName, String password) {
        return new ProxyInfo(host, port, userName, password);
    }

    public static ProxyInfo setProxyCfg(String host, String port) {

        ProxyInfo proxyInfo = new ProxyInfo();

        proxyInfo._host = host;
        proxyInfo._port = port;

        return proxyInfo;
    }

    public static ProxyInfo setProxyCfg(String address, String userName, String password) {
        ProxyInfo proxyInfo = new ProxyInfo();

        if (address != null) {
            String host = address;

            if (address.endsWith("/")) {
                int end = address.lastIndexOf('/');
                host = address.substring(0, end);
            }

            int index = host.lastIndexOf(':');
            if (index > 0) {
                proxyInfo._host = host.substring(0, index);
                proxyInfo._port = host.substring(index + 1, host.length());
            } else {
                proxyInfo._host = host;
                proxyInfo._port = "80";
            }
        }
        proxyInfo._userName = userName;
        proxyInfo._password = password;

        return proxyInfo;
    }

    static class ProxyInfo {
        String _host;
        String _port;
        String _userName;
        String _password;

        public ProxyInfo() {
            //Keep the non parameter constructor.
        }

        public ProxyInfo(String host, String port, String userName, String password) {
            _host = host;
            _port = port;
            _userName = userName;
            _password = password;
        }

    }
}
