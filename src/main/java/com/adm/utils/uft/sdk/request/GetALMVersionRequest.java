package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.RestXmlUtils;
import com.adm.utils.uft.rest.RESTConstants;
import com.adm.utils.uft.sdk.Client;

import java.util.Map;

public class GetALMVersionRequest extends GetRequest {
    public GetALMVersionRequest(Client client) {

        super(client, null);
    }

    @Override
    protected String getSuffix() {

        return String.format("%s/sa/version", RESTConstants.REST_PROTOCOL);
    }

    @Override
    protected String getUrl() {

        return String.format("%s%s", _client.getServerUrl(), getSuffix());
    }

    @Override
    protected Map<String, String> getHeaders() {

        return RestXmlUtils.getAppXmlHeaders();
    }
}
