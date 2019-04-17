package com.adm.tools.common.sdk.request;

import com.adm.tools.common.RestXmlUtils;
import com.adm.tools.common.rest.RESTConstants;
import com.adm.tools.common.sdk.Client;

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
