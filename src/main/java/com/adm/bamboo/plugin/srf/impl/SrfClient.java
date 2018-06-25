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

package com.adm.bamboo.plugin.srf.impl;

import com.adm.utils.srf.HttpCodeErrorClassifier;
import com.adm.utils.srf.SrfException;
import com.atlassian.bamboo.build.logger.BuildLogger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.wagon.authorization.AuthorizationException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;


public class SrfClient {

    CloseableHttpClient httpclient;
    private String srfServerAddress;
    private SSLSocketFactory sslSocketFactory;
    private HttpHost proxyHost;
    private String accessToken;
    private String workspaceId;
    private String tenantId;
    private BuildLogger buildLogger;

    public SrfClient(BuildLogger buildLogger, String srfServerAddress, String tenantId, SSLSocketFactory sslSocketFactory, URL proxyUrl) {
        this.srfServerAddress = srfServerAddress;
        this.tenantId = tenantId;
        httpclient = HttpClients.createDefault();
        this.sslSocketFactory = sslSocketFactory;
        this.proxyHost = proxyUrl != null ? new HttpHost(proxyUrl.getHost(), proxyUrl.getPort()) : null;
        this.buildLogger = buildLogger;
    }

    public void login(String clientId, String clientSecret) throws AuthorizationException, IOException, SrfException {
        buildLogger.addBuildLogEntry(String.format("Logging with client's id: %s into %s", clientId, srfServerAddress));
        String authorizationsAddress = srfServerAddress.concat("/rest/security/public/v2/authorizations/access-tokens");

        JSONObject loginBody = new JSONObject();
        loginBody.put("loginName", clientId);
        loginBody.put("password", clientSecret);

        String response = sendPostRequest(new URL(authorizationsAddress), loginBody);
        JSONObject accessKeys = JSONObject.fromObject(response);
        accessToken = accessKeys.getString("accessToken");
        workspaceId = accessKeys.getString("workspaceId");

        if (accessToken == null || accessToken.isEmpty() || workspaceId == null || workspaceId.isEmpty()) {
            buildLogger.addErrorLogEntry(String.format("Received invalid access keys: access token %s ,workspace id %s", accessToken, workspaceId));
            throw new SrfException(String.format("Received invalid access keys: access token %s ,workspace id %s", accessToken, workspaceId));
        }
        buildLogger.addBuildLogEntry(String.format("Successfully logged into %s", srfServerAddress));
    }

    public JSONArray executeTestsSet(JSONObject requestBody) throws AuthorizationException, IOException, SrfException {
        String executionAddress = getAuthenticatedSrfApiAddress("/rest/jobmanager/v1/workspaces/%s/execution/jobs");

        String response = sendPostRequest(new URL(executionAddress), requestBody);
        return JSONObject.fromObject(response).getJSONArray("jobs");
    }

    public void cancelJob(String jobId) {
        buildLogger.addBuildLogEntry(String.format("Cancelling job id: %s", jobId));
        String jobCancelAddress = getAuthenticatedSrfApiAddress("/rest/jobmanager/v1/workspaces/%s/execution/jobs/{0}", new String[]{jobId});

        try {
            String response = sendRequest(new URL(jobCancelAddress), HttpMethod.DELETE);
            buildLogger.addBuildLogEntry(response);
        } catch (SrfException | AuthorizationException | IOException e) {
            buildLogger.addBuildLogEntry(String.format("SrfClient.cancelJob: got error:%s1 for job id: %s2", e, jobId));
            e.printStackTrace();
        }

    }

    public JSONArray getTestRuns(JSONArray jobIds) throws AuthorizationException, IOException, SrfException {
        JSONArray testRuns = new JSONArray();
        for (int i = 0; i < jobIds.size(); i++) {
            JSONArray testRun = getTestRun((String) jobIds.get(i));
            testRuns.addAll(testRun);
        }

        return testRuns;
    }

    public JSONArray getTestRun(String jobId) throws IOException, AuthorizationException, SrfException {
        String testRunAddress = getAuthenticatedSrfApiAddress("/rest/test-manager/workspaces/%s/test-runs")
                .concat(String.format("&id=%s&include=resource,script-runs,script-steps", jobId));

        String response = sendRequest(new URL(testRunAddress), HttpMethod.GET);
        return JSONArray.fromObject(response);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    private String sendRequest(URL url, HttpMethod method) throws IOException, SrfException, AuthorizationException {
        URLConnection connection = url.openConnection();

        if (url.getProtocol().startsWith("https")) {
            ((HttpsURLConnection) connection).setRequestMethod(method.text);
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        } else {
            ((HttpURLConnection) connection).setRequestMethod(method.text);
        }

        int statusCode = ((HttpURLConnection) connection).getResponseCode();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

        StringBuilder response = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }

        if (statusCode >= 400) {
            HttpCodeErrorClassifier.throwError(buildLogger, statusCode, response.toString());
        }

        return response.toString();
    }

    private String sendPostRequest(URL url, JSONObject body) throws IOException, SrfException, AuthorizationException {
        OutputStreamWriter writer = null;
        OutputStream out = null;
        BufferedReader bufferedReader = null;
        URLConnection connection = url.openConnection();
        StringBuilder response;

        try {
            if (url.getProtocol().startsWith("https")) {
                ((HttpsURLConnection) connection).setRequestMethod("POST");
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
            } else {
                ((HttpURLConnection) connection).setRequestMethod("POST");
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            out = connection.getOutputStream();
            writer = new OutputStreamWriter(out);
            writer.write(body.toString());
            writer.flush();
            out.flush();

            int statusCode = ((HttpURLConnection) connection).getResponseCode();

            InputStream inputStream = statusCode >= 400 ? ((HttpURLConnection)connection).getErrorStream() : ((HttpURLConnection)connection).getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            response = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }

            if (statusCode >= 400) {
                HttpCodeErrorClassifier.throwError(buildLogger, statusCode, response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            out.close();
            writer.close();
            bufferedReader.close();
        }

        return response.toString();
    }
    private String getAuthenticatedSrfApiAddress(String path) {
        return srfServerAddress
                .concat(String.format(path, workspaceId))
                .concat(String.format("?access-token=%s&TENANTID=%s", accessToken, tenantId));
    }

    private String getAuthenticatedSrfApiAddress(String path, String[] pathParams) {
        String parametizesUrl = MessageFormat.format(path, pathParams);
        return getAuthenticatedSrfApiAddress(parametizesUrl);
    }

    private enum HttpMethod {

        POST("POST"),
        GET("GET"),
        DELETE("DELETE");

        private String text;
        HttpMethod(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

    }

}