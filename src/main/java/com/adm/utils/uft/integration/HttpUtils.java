/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    public static final String POST = "POST";
    public static final String GET = "GET";

    private HttpUtils() {

    }

    public static HttpResponse post(ProxyInfo proxyInfo, String url, Map<String, String> headers, byte[] data) throws HttpConnectionException {

        HttpResponse response = null;
        try {
            response = doHttp(proxyInfo, POST, url, null, headers, data);
        } catch (Exception e) {
            throw new HttpConnectionException();
        }
        return response;
    }

    public static HttpResponse get(ProxyInfo proxyInfo, String url, Map<String, String> headers, String queryString) throws HttpConnectionException {

        HttpResponse response = null;
        try {
            response = doHttp(proxyInfo, GET, url, queryString, headers, null);
        } catch (Exception e) {
            throw new HttpConnectionException();
        }
        return response;
    }

    private static HttpResponse doHttp(ProxyInfo proxyInfo, String requestMethod, String connectionUrl, String queryString, Map<String, String> headers, byte[] data) throws IOException {
        HttpResponse response = new HttpResponse();

        if ((queryString != null) && !queryString.isEmpty()) {
            connectionUrl += "?" + queryString;
        }

        URL url = new URL(connectionUrl);

        HttpURLConnection connection = (HttpURLConnection) openConnection(proxyInfo, url);

        connection.setRequestMethod(requestMethod);

        setConnectionHeaders(connection, headers);

        if (data != null && data.length > 0) {
            connection.setDoOutput(true);
            try {
                OutputStream out = connection.getOutputStream();
                out.write(data);
                out.flush();
                out.close();
            } catch (Throwable cause) {
                cause.printStackTrace();
            }
        }

        connection.connect();

        int resCode = connection.getResponseCode();
        response.setResponseCode(resCode);
        response.setResponseMessage(connection.getResponseMessage());

        if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED || resCode == HttpURLConnection.HTTP_ACCEPTED) {
            InputStream inputStream = connection.getInputStream();
            JSONObject jsonObject = convertStreamToJSONObject(inputStream);
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            response.setHeaders(headerFields);
            response.setJsonObject(jsonObject);
        }

        connection.disconnect();

        return response;
    }

    private static URLConnection openConnection(final ProxyInfo proxyInfo, URL _url) throws IOException {

        Proxy proxy = null;

        if (proxyInfo != null && proxyInfo._host != null && proxyInfo._port != null && !proxyInfo._host.isEmpty() && !proxyInfo._port.isEmpty()) {

            try {
                int port = Integer.parseInt(proxyInfo._port.trim());
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo._host, port));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (proxy != null && proxyInfo._userName != null && proxyInfo._password != null && !proxyInfo._password.isEmpty() && !proxyInfo._password.isEmpty()) {
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyInfo._userName, proxyInfo._password.toCharArray());    //To change body of overridden methods use File | Settings | File Templates.
                }
            };

            Authenticator.setDefault(authenticator);
        }

        if (proxy == null) {
            return _url.openConnection();
        }

        return _url.openConnection(proxy);
    }

    private static void setConnectionHeaders(HttpURLConnection connection, Map<String, String> headers) {

        if (connection != null && headers != null && headers.size() != 0) {
            Iterator<Map.Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Map.Entry<String, String> header = headersIterator.next();
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    private static JSONObject convertStreamToJSONObject(InputStream inputStream) {
        JSONObject obj = null;

        if (inputStream != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer res = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    res.append(line);
                }
                obj = (JSONObject) JSONValue.parseStrict(res.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }


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
        //web-proxy.atl.hp.com:8088
        ProxyInfo proxyInfo = new ProxyInfo();

        if (address != null) {
            if (address.endsWith("/")) {
                int end = address.lastIndexOf("/");
                address = address.substring(0, end);
            }

            int i = address.lastIndexOf(':');
            if (i > 0) {
                proxyInfo._host = address.substring(0, i);
                proxyInfo._port = address.substring(i + 1, address.length());
            } else {
                proxyInfo._host = address;
                proxyInfo._port = "80";
            }

            proxyInfo._userName = userName;
            proxyInfo._password = password;

        }

        return proxyInfo;
    }


    static class ProxyInfo {

        private String _host;
        private String _port;
        private String _userName;
        private String _password;

        public ProxyInfo() {

        }

        public ProxyInfo(String host, String port, String userName, String password) {
            _host = host;
            _port = port;
            _userName = userName;
            _password = password;
        }
    }

}
