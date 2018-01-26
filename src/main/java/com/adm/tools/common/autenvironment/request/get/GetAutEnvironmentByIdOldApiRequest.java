package com.adm.tools.common.autenvironment.request.get;

import com.adm.tools.common.autenvironment.request.AUTEnvironmentResources;
import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.request.GeneralGetRequest;

public class GetAutEnvironmentByIdOldApiRequest extends GeneralGetRequest {

    private String autEnvironmentId;

    public GetAutEnvironmentByIdOldApiRequest(Client client, String autEnvironmentId) {

        super(client);
        this.autEnvironmentId = autEnvironmentId;
    }

    @Override
    protected String getSuffix() {

        return AUTEnvironmentResources.AUT_ENVIRONMENTS_OLD;

    }

    @Override
    protected String getQueryString() {

        return String.format("query={id[%s]}", autEnvironmentId);
    }
}

