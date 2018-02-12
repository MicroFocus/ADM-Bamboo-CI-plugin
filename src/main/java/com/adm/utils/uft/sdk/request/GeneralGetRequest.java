package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.ResourceAccessLevel;
import com.adm.utils.uft.sdk.Response;

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
