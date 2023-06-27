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

package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Response;

import java.util.Map;

public abstract class GeneralRequest{

    protected final Client _client;

    protected GeneralRequest(Client client) {

        _client = client;
    }

    public final Response execute() {

        Response ret = new Response();
        try {
            ret = perform();
        } catch (Throwable cause) {
            ret.setFailure(cause);
        }

        return ret;
    }

    protected abstract Response perform();

    protected String getSuffix() {
        return null;
    }

    protected Map<String, String> getHeaders() {

        return null;
    }

    protected String getBody() {

        return null;
    }

    protected String getUrl() {

        return _client.buildRestRequest(getSuffix());
    }
}
