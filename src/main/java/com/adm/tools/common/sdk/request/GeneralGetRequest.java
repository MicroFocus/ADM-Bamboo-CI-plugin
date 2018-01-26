package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.ResourceAccessLevel;
import com.adm.tools.common.sdk.Response;

public class GeneralGetRequest extends GeneralRequest {

    protected GeneralGetRequest(Client client) {
        super(client);
    }

    protected String getQueryString() {

        return null;
    }

    @Override
    public Response perform() {

        return _client.httpGet(
                getUrl(),
                getQueryString(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }
}
