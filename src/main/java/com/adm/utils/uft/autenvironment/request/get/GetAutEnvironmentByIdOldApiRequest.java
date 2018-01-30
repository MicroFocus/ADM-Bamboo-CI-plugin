package com.adm.utils.uft.autenvironment.request.get;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralGetRequest;

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

