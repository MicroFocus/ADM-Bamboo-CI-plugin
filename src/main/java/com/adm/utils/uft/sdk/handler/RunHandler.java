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

import com.adm.utils.uft.sdk.request.StartRunEntityRequest;
import com.adm.utils.uft.sdk.request.StopEntityRequest;
import com.adm.utils.uft.model.CdaDetails;
import com.adm.utils.uft.sdk.*;

public abstract class RunHandler extends Handler {
    protected abstract String getStartSuffix();

    public abstract String getNameSuffix();

    public RunHandler(Client client, String entityId) {

        super(client, entityId);
    }

    public Response start(
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {

        return new StartRunEntityRequest(
                _client,
                getStartSuffix(),
                _entityId,
                duration,
                postRunAction,
                environmentConfigurationId,
                cdaDetails).execute();
    }

    public Response stop() {

        return new StopEntityRequest(_client, _runId).execute();
    }

    public String getReportUrl(Args args) {

        return new ALMRunReportUrlBuilder().build(_client, _client.getServerUrl(), args.getDomain(), args.getProject(), _runId);
    }

    public RunResponse getRunResponse(Response response) {

        RunResponse ret = new RunResponse();
        ret.initialize(response);

        return ret;

    }
}
