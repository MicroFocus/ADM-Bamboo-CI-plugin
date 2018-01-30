package com.adm.utils.uft.autenvironment.request.get;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralGetRequest;

public class GetAutEnvironmentConfigurationByIdRequest extends GeneralGetRequest {

    private String autEnvironmentConfigurationId;

    public GetAutEnvironmentConfigurationByIdRequest(
            Client client,
            String autEnvironmentConfigurationId) {

        super(client);
        this.autEnvironmentConfigurationId = autEnvironmentConfigurationId;
    }

    @Override
    protected String getSuffix() {

        return AUTEnvironmentResources.AUT_ENVIRONMENT_CONFIGURATIONS;

    }

    @Override
    protected String getQueryString() {

        return String.format("query={id[%s]}", autEnvironmentConfigurationId);
    }
}
