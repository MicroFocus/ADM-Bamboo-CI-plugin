/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.integration;

import net.minidev.json.JSONObject;

import java.util.List;
import java.util.Map;

public class HttpResponse {

    private Map<String, List<String>> headers;
    private JSONObject jsonObject;
    private String responseCode;

    public HttpResponse() {

    }

    public HttpResponse(Map<String, List<String>> headers, JSONObject jsonObject) {
        this.headers = headers;
        this.jsonObject = jsonObject;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
