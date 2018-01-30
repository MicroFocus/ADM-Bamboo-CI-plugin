package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Response;

import java.util.Map;

public abstract class GeneralRequest{

    protected final Client _client;

    protected GeneralRequest(Client client) {

        _client = client;
    }

    public final Response execute() {

        Response ret = new Response();
        try {
            ret = perform();
        } catch (Throwable cause) {
            ret.setFailure(cause);
        }

        return ret;
    }

    protected abstract Response perform();

    protected String getSuffix() {
        return null;
    }

    protected Map<String, String> getHeaders() {

        return null;
    }

    protected String getBody() {

        return null;
    }

    protected String getUrl() {

        return _client.buildRestRequest(getSuffix());
    }
}
