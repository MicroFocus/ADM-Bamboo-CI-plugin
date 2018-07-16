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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.Pair;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.model.CdaDetails;
import com.adm.utils.uft.sdk.Client;

import java.util.ArrayList;
import java.util.List;

public class StartRunEntityRequest extends PostRequest {

    private final String _duration;
    private final String _suffix;
    private final String _environmentConfigurationId;
    private final CdaDetails _cdaDetails;

    public StartRunEntityRequest(
            Client client,
            String suffix,
            String runId,
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {

        super(client, runId);
        _duration = duration;
        _suffix = suffix;
        _environmentConfigurationId = environmentConfigurationId;
        _cdaDetails = cdaDetails;
    }

    @Override
    protected List<Pair<String, String>> getDataFields() {

        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        ret.add(new Pair<String, String>("duration", _duration));
        ret.add(new Pair<String, String>("vudsMode", "false"));
        ret.add(new Pair<String, String>("reservationId", "-1"));
        if (!StringUtils.isNullOrEmpty(_environmentConfigurationId)) {
            ret.add(new Pair<String, String>("valueSetId", _environmentConfigurationId));
            if (_cdaDetails != null) {
                ret.add(new Pair<String, String>("topologyAction", _cdaDetails.getTopologyAction()));
                ret.add(new Pair<String, String>(
                        "realizedTopologyName",
                        _cdaDetails.getDeployedEnvironmentName()));

            }
        }

        return ret;
    }

    @Override
    protected String getSuffix() {

        return _suffix;
    }

}

