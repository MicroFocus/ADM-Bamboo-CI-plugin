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

package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.sdk.*;

import java.net.MalformedURLException;
import java.net.URL;

public class PCRunHandler extends RunHandler {

    public PCRunHandler(Client client, String entityId) {

        super(client, entityId);
    }

    @Override
    protected String getStartSuffix() {

        return String.format("test-instances/%s/startrun", _entityId);
    }

    @Override
    public String getNameSuffix() {

        return String.format("runs/%s", _runId);
    }

    @Override
    public String getReportUrl(Args args) {

        String ret = "No report URL available";
        try {
            ret =
                    String.format(
                            "td://%s.%s.%s:8080/qcbin/[TestRuns]?EntityLogicalName=run&EntityID=%s",
                            args.getProject(),
                            args.getDomain(),
                            new URL(args.getUrl()).getHost(),
                            _runId);
        } catch (MalformedURLException ex) {
            throw new SSEException(ex);
        }
        return ret;
    }

    @Override
    public RunResponse getRunResponse(Response response) {

        RunResponse ret = new PCRunResponse();
        ret.initialize(response);

        return ret;

    }
}

