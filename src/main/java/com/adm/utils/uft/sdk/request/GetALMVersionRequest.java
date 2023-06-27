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

import com.adm.utils.uft.RestXmlUtils;
import com.adm.utils.uft.rest.RESTConstants;
import com.adm.utils.uft.sdk.Client;

import java.util.Map;

public class GetALMVersionRequest extends GetRequest {
    public GetALMVersionRequest(Client client) {

        super(client, null);
    }

    @Override
    protected String getSuffix() {

        return String.format("%s/sa/version", RESTConstants.REST_PROTOCOL);
    }

    @Override
    protected String getUrl() {

        return String.format("%s%s", _client.getServerUrl(), getSuffix());
    }

    @Override
    protected Map<String, String> getHeaders() {

        return RestXmlUtils.getAppXmlHeaders();
    }
}
