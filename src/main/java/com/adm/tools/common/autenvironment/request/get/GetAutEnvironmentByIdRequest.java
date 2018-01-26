package com.adm.tools.common.autenvironment.request.get;

import com.adm.tools.common.autenvironment.request.AUTEnvironmentResources;
import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.request.GeneralGetRequest;

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
