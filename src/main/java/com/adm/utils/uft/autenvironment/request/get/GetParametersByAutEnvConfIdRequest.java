package com.adm.utils.uft.autenvironment.request.get;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralGetRequest;

public class GetParametersByAutEnvConfIdRequest extends GeneralGetRequest {

    String configurationId;

    public GetParametersByAutEnvConfIdRequest(Client client, String configurationId) {

        super(client);
        this.configurationId = configurationId;
    }

    @Override
    protected String getSuffix() {

        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_VALUES;
    }

    @Override
    protected String getQueryString() {

        return String.format("query={app-param-value-set-id[%s]}&page-size=2000", configurationId);
    }
}
