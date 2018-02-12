package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;

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
