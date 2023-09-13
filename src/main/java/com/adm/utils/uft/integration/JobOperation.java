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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class JobOperation {
    public static final String LOGIN_SECRET = "x-hp4msecret";
    public static final String SPLIT_COMMA = ",";
    public static final String JSESSIONID = "JSESSIONID";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COOKIE = "Cookie";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String EQUAL = "=";
    public static final String STATUS = "status";
    public static final String ERROR = "error";

    //about upload
    private final static String CONTENT_TYPE_DOWNLOAD_VALUE = "multipart/form-data; boundary=----";
    private final static String BOUNDARYSTR = "randomstring";

    public static final String LOGIN_URL = "/rest/client/login";
    public static final String CREATE_JOB_URL = "/rest/job/createTempJob";
    public static final String GET_JOB_URL = "/rest/job/";
    public static final String UPLOAD_APP_URL = "/rest/apps/upload";


    //mobile center info
    private String _serverUrl;
    private String _userName;
    private String _password;

    //Proxy Configuration information
    private String proxyHost;
    private String proxyPort;
    private String proxyUserName;
    private String proxyPassword;

    public JobOperation() {

    }

    public JobOperation(String serverUrl, String userName, String password) {
        _userName = userName;
        _password = password;

        _serverUrl = checkUrl(serverUrl);

    }

    public JobOperation(String serverUrl, String userName, String password, String address, String proxyUserName, String proxyPassword) {

        _userName = userName;
        _password = password;

        _serverUrl = checkUrl(serverUrl);

        if (address != null) {

            address = checkUrl(address);

            int i = address.lastIndexOf(':');
            if (i > 0) {
                this.proxyHost = address.substring(0, i);
                this.proxyPort = address.substring(i + 1, address.length());
            } else {
                this.proxyHost = address;
                this.proxyPort = "80";
            }

        }

        this.proxyUserName = proxyUserName;
        this.proxyPassword = proxyPassword;

    }

    public String upload(String appPath) throws HttpConnectionException, IOException {

        String json = null;
        String hp4mSecret = null;
        String jsessionId = null;

        SessionInfo info = loginToMC();

        try {
            if (info != null) {
                hp4mSecret = info.getHp4MSecret();
                jsessionId = info.getJSessionId();
            }
        } catch (Exception e) {
            return null;
        }

        File appFile = new File(appPath);

        String uploadUrl = _serverUrl + UPLOAD_APP_URL;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(LOGIN_SECRET, hp4mSecret);
        headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);
        headers.put(CONTENT_TYPE, CONTENT_TYPE_DOWNLOAD_VALUE + BOUNDARYSTR);
        headers.put("filename", appFile.getName());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StringBuffer content = new StringBuffer();
        content.append("\r\n").append("------").append(BOUNDARYSTR).append("\r\n");
        content.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + appFile.getName() + "\"\r\n");
        content.append("Content-Type: application/octet-stream\r\n\r\n");

        outputStream.write(content.toString().getBytes());

        FileInputStream in = new FileInputStream(appFile);

        byte[] b = new byte[1024];
        int i = 0;
        while ((i = in.read(b)) != -1) {
            outputStream.write(b, 0, i);
        }
        in.close();

        outputStream.write(("\r\n------" + BOUNDARYSTR + "--\r\n").getBytes());

        byte[] bytes = outputStream.toByteArray();

        outputStream.close();

        HttpUtils.ProxyInfo proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);

        HttpResponse response = HttpUtils.post(proxyInfo, uploadUrl, headers, bytes);

        if (response != null && response.getJsonObject() != null) {
            json = response.getJsonObject().toJSONString();
        }

        return json;
    }

    //Login to MC
    public SessionInfo loginToMC() throws HttpConnectionException {

        SessionInfo info = null;
        Map<String, String> headers = new HashMap<>();
        headers.put(ACCEPT, "application/json");
        headers.put(CONTENT_TYPE, "application/json;charset=UTF-8");

        JSONObject sendObject = new JSONObject();
        sendObject.put("name", _userName);
        sendObject.put("password", _password);
        sendObject.put("accountName", "default");

        HttpUtils.ProxyInfo proxyInfo = null;

        if (proxyHost != null && proxyPort != null) {
            proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
        }

        HttpResponse response = HttpUtils.post(proxyInfo, _serverUrl + LOGIN_URL, headers, sendObject.toJSONString().getBytes());

        if (response != null) {
            if (response.getHeaders() != null) {
                Map<String, List<String>> headerFields = response.getHeaders();
                List<String> hp4mSecretList = headerFields.get(LOGIN_SECRET);
                String hp4mSecret = null;
                if (hp4mSecretList != null && hp4mSecretList.size() != 0) {
                    hp4mSecret = hp4mSecretList.get(0);
                }
                List<String> setCookieList = headerFields.get(SET_COOKIE);
                String setCookie = null;
                if (setCookieList != null && setCookieList.size() != 0) {
                    setCookie = setCookieList.toString();
                }

                String jsessionId = getJSESSIONID(setCookie);

                if (hp4mSecret == null || jsessionId == null) {
                    throw new HttpConnectionException();
                }

                info = new SessionInfo(jsessionId, hp4mSecret, response);
            } else {
                info = new SessionInfo(response);
            }
        }

        return info;
    }

    //create one temp job
    public String createTempJob() throws HttpConnectionException {
        String json = null;
        String hp4mSecret = null;
        String jsessionId = null;

        SessionInfo info = loginToMC();
        try {
            if (info == null) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(STATUS, -999);
                return jsonObj.toJSONString();
            } else {
                if (info.getResponse().getResponseCode() == HttpURLConnection.HTTP_OK) {
                    hp4mSecret = info.getHp4MSecret();
                    jsessionId = info.getJSessionId();
                } else {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put(STATUS, info.getResponse().getResponseCode());
                    jsonObj.put(ERROR, info.getResponse().getResponseMessage());
                    return jsonObj.toJSONString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean ok = CommonUtils.doCheck(hp4mSecret, jsessionId);

        if (ok) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(LOGIN_SECRET, hp4mSecret);
            headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);

            HttpUtils.ProxyInfo proxyInfo = null;
            if (proxyHost != null && proxyPort != null) {
                proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
            }

            HttpResponse response = HttpUtils.get(proxyInfo, _serverUrl + CREATE_JOB_URL, headers, null);

            if (response != null) {
                if (response.getJsonObject() == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put(STATUS, response.getResponseCode());
                    jsonObj.put(ERROR, response.getResponseMessage());
                    json = jsonObj.toJSONString();
                } else {
                    json = response.getJsonObject().toJSONString();
                }
            }
        }
        return json;
    }

    //get one job by id
    public JSONObject getJobById(String jobUUID) throws HttpConnectionException {
        JSONObject jobJsonObject = null;
        String hp4mSecret = null;
        String jsessionId = null;

        SessionInfo info = loginToMC();
        try {
            if (info != null) {
                if (info.getResponse().getResponseCode() == HttpURLConnection.HTTP_OK) {
                    hp4mSecret = info.getHp4MSecret();
                    jsessionId = info.getJSessionId();
                } else {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put(STATUS, info.getResponse().getResponseCode());
                    jsonObj.put(ERROR, info.getResponse().getResponseMessage());
                    return jsonObj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean ok = CommonUtils.doCheck(jobUUID, hp4mSecret, jsessionId);

        if (ok) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(LOGIN_SECRET, hp4mSecret);
            headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);

            HttpUtils.ProxyInfo proxyInfo = null;
            if (proxyHost != null && proxyPort != null) {
                proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
            }
            HttpResponse response = HttpUtils.get(proxyInfo, _serverUrl + GET_JOB_URL + jobUUID, headers, null);

            if (response != null && response.getJsonObject() != null) {
                jobJsonObject = response.getJsonObject();
            }
        }

        return jobJsonObject;
    }

    //parse one job.and get the data we want
    public String getJobJSONData(String jobUUID) throws HttpConnectionException {
        JSONObject jobJSON = getJobById(jobUUID);

        JSONObject returnJSON = new JSONObject();

        JSONObject dataJSON = null;
        if (jobJSON != null) {
            dataJSON = (JSONObject) jobJSON.get("data");
        }

        //Device Capabilities

        if (dataJSON != null) {
            JSONObject returnDeviceCapabilityJSON = new JSONObject();

            JSONObject detailJSON = (JSONObject) dataJSON.get("capableDeviceFilterDetails");
            if (detailJSON != null) {
                String osType = (String) detailJSON.get("platformName");
                String osVersion = (String) detailJSON.get("platformVersion");
                String manufacturerAndModel = (String) detailJSON.get("deviceName");
                String targetLab = (String) detailJSON.get("source");

                returnDeviceCapabilityJSON.put("OS", osType + osVersion);
                returnDeviceCapabilityJSON.put("manufacturerAndModel", manufacturerAndModel);
                returnDeviceCapabilityJSON.put("targetLab", targetLab);
            }

            JSONObject returnDeviceJSON = new JSONObject();
            //specific device
            JSONArray devices = (JSONArray) dataJSON.get("devices");

            if (devices != null) {
                JSONObject deviceJSON = (JSONObject) devices.get(0);
                if (deviceJSON != null) {
                    String deviceID = deviceJSON.getAsString("deviceID");
                    String osType = deviceJSON.getAsString("osType");
                    String osVersion = deviceJSON.getAsString("osVersion");
                    String manufacturerAndModel = deviceJSON.getAsString("model");

                    returnDeviceJSON.put("deviceId", deviceID);
                    returnDeviceJSON.put("OS", osType + " " + osVersion);
                    returnDeviceJSON.put("manufacturerAndModel", manufacturerAndModel);
                }
            }

            //Applications under test
            JSONArray returnExtraJSONArray = new JSONArray();
            JSONArray extraAppJSONArray = (JSONArray) dataJSON.get("extraApps");

            if (extraAppJSONArray != null) {
                Iterator<Object> iterator = extraAppJSONArray.iterator();

                while (iterator.hasNext()) {

                    JSONObject extraAPPJSON = new JSONObject();

                    JSONObject nextJSONObject = (JSONObject) iterator.next();
                    String extraAppName = (String) nextJSONObject.get("name");
                    Boolean instrumented = (Boolean) nextJSONObject.get("instrumented");

                    extraAPPJSON.put("extraAppName", extraAppName);
                    extraAPPJSON.put("instrumented", instrumented ? "Packaged" : "Not Packaged");

                    returnExtraJSONArray.add(extraAPPJSON);
                }
            }

            //Test Definitions
            JSONObject returnDefinitionJSON = new JSONObject();

            JSONObject applicationJSON = (JSONObject) dataJSON.get("application");

            if (applicationJSON != null) {
                String launchApplicationName = (String) applicationJSON.get("name");
                Boolean instrumented = (Boolean) applicationJSON.get("instrumented");

                returnDefinitionJSON.put("launchApplicationName", launchApplicationName);
                returnDefinitionJSON.put("instrumented", instrumented ? "Packaged" : "Not Packaged");
            }

            //Device metrics,Install Restart
            String headerStr = (String) dataJSON.get("header");
            JSONObject headerJSON = parseJSONString(headerStr);

            if (headerJSON != null) {
                JSONObject configurationJSONObject = (JSONObject) headerJSON.get("configuration");
                Boolean restart = (Boolean) configurationJSONObject.get("restartApp");
                Boolean install = (Boolean) configurationJSONObject.get("installAppBeforeExecution");
                Boolean uninstall = (Boolean) configurationJSONObject.get("deleteAppAfterExecution");

                StringBuffer sb = new StringBuffer("");

                if (restart) {
                    sb.append("Restart;");
                }

                if (install) {
                    sb.append("Install;");
                }

                if (uninstall) {
                    sb.append("Uninstall;");
                }

                JSONObject collectJSON = (JSONObject) headerJSON.get("collect");
                StringBuffer deviceMetricsSb = new StringBuffer("");

                //device metrics
                if (collectJSON != null) {
                    Boolean useCPU = (Boolean) collectJSON.get("cpu");
                    Boolean useMemory = (Boolean) collectJSON.get("memory");
                    Boolean useLogs = (Boolean) collectJSON.get("logs");
                    Boolean useScreenshot = (Boolean) collectJSON.get("screenshot");
                    Boolean useFreeMemory = (Boolean) collectJSON.get("freeMemory");

                    if (useCPU) {
                        deviceMetricsSb.append("CPU;");
                    }

                    if (useMemory) {
                        deviceMetricsSb.append("Memory;");
                    }

                    if (useLogs) {
                        deviceMetricsSb.append("Log;");
                    }
                    if (useScreenshot) {
                        deviceMetricsSb.append("Screenshot;");
                    }
                    if (useFreeMemory) {
                        deviceMetricsSb.append("FreeMomery;");
                    }
                }

                returnDefinitionJSON.put("autActions", removeLastSemicolon(sb));
                returnDefinitionJSON.put("deviceMetrics", removeLastSemicolon(deviceMetricsSb));

            }

            returnJSON.put("deviceCapability", returnDeviceCapabilityJSON);
            returnJSON.put("extraApps", returnExtraJSONArray);
            returnJSON.put("definitions", returnDefinitionJSON);
            returnJSON.put("jobUUID", jobUUID);
            returnJSON.put("specificDevice", returnDeviceJSON);
        }

        return returnJSON.toJSONString();
    }

    public String removeLastSemicolon(StringBuffer sb) {
        int len = sb.length();
        if (len > 0) {
            sb = sb.delete(len - 1, len);
            return sb.toString();
        }
        return sb.toString();
    }

    public JSONObject parseJSONString(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) JSONValue.parseStrict(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getJSESSIONID(String setCookie) {
        String id = null;
        String[] cookies = setCookie.split(SPLIT_COMMA);
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].contains(JSESSIONID)) {
                int index = cookies[i].indexOf(EQUAL);
                int endIndex = cookies[i].indexOf(";");
                id = cookies[i].substring(index + 1,endIndex);
                break;
            }
        }
        return id;
    }


    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    private String checkUrl(String serverUrl) {
        if (serverUrl != null) {
            if (serverUrl.endsWith("/")) {
                int index = serverUrl.lastIndexOf("/");
                serverUrl = serverUrl.substring(0, index);
                return serverUrl;
            }

        }
        return serverUrl;
    }

    protected class SessionInfo {
        private String jsessionId;
        private String hp4mSecret;

        private HttpResponse response;

        public SessionInfo(String jsessionId, String hp4mSecret, HttpResponse response) {
            this.jsessionId = jsessionId;
            this.hp4mSecret = hp4mSecret;
            this.response = response;
        }

        public SessionInfo(HttpResponse response) {
            this.response = response;
        }

        public String getJSessionId() {
            return jsessionId;
        }
        public String getHp4MSecret() {
            return hp4mSecret;
        }

        public HttpResponse getResponse() {
            return response;
        }
    }

}

