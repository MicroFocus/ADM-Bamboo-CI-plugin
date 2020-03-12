package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public class GetRunEntityNameRequest extends GetRequest {

    private final String _nameSuffix;

    public GetRunEntityNameRequest(Client client, String suffix, String entityId) {

        super(client, entityId);
        _nameSuffix = suffix;

    }

    @Override
    protected String getSuffix() {

        return _nameSuffix;
    }
}
