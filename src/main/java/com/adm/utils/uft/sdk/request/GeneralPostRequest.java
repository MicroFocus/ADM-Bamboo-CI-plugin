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

import com.adm.utils.uft.Pair;
import com.adm.utils.uft.RestXmlUtils;
import com.adm.utils.uft.rest.RESTConstants;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.ResourceAccessLevel;
import com.adm.utils.uft.sdk.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralPostRequest  extends GeneralRequest {

    protected GeneralPostRequest(Client client) {
        super(client);
    }

    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }

    @Override
    public Response perform() {

        return _client.httpPost(
                getUrl(),
                getDataBytes(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }

    private byte[] getDataBytes() {

        StringBuilder builder = new StringBuilder("<Entity><Fields>");
        for (Pair<String, String> currPair : getDataFields()) {
            builder.append(RestXmlUtils.fieldXml(currPair.getFirst(), currPair.getSecond()));
        }

        return builder.append("</Fields></Entity>").toString().getBytes();
    }

    protected List<Pair<String, String>> getDataFields() {

        return new ArrayList<Pair<String, String>>(0);
    }
}

