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

package com.adm.utils.uft.autenvironment.request.post;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralPostRequest;
import com.adm.utils.uft.Pair;

import java.util.ArrayList;
import java.util.List;

public class CreateAutEnvConfRequest extends GeneralPostRequest {

    private String autEnvironmentId;
    private String name;

    public CreateAutEnvConfRequest(Client client, String autEnvironmentId, String name) {

        super(client);
        this.autEnvironmentId = autEnvironmentId;
        this.name = name;
    }

    @Override
    protected String getSuffix() {
        return AUTEnvironmentResources.AUT_ENVIRONMENT_CONFIGURATIONS;
    }

    @Override
    protected List<Pair<String, String>> getDataFields() {

        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        ret.add(new Pair<String, String>("app-param-set-id", autEnvironmentId));
        ret.add(new Pair<String, String>("name", name));

        return ret;
    }
}
