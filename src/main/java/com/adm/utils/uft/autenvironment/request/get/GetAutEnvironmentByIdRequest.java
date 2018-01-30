package com.adm.utils.uft.autenvironment.request.get;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralGetRequest;

public class GetAutEnvironmentByIdRequest extends GeneralGetRequest {

    private String autEnvironmentId;

    public GetAutEnvironmentByIdRequest(Client client, String autEnvironmentId) {

        super(client);
        this.autEnvironmentId = autEnvironmentId;
    }

    @Override
    protected String getSuffix() {

        return AUTEnvironmentResources.AUT_ENVIRONMENTS;

    }

    @Override
    protected String getQueryString() {

        return String.format("query={id[%s]}", autEnvironmentId);
    }
}
