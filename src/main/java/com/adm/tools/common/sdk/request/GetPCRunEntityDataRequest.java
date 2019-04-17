package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public class GetPCRunEntityDataRequest extends GetRequest {

    public GetPCRunEntityDataRequest(Client client, String runId) {

        super(client, runId);
    }

    @Override
    protected String getSuffix() {

        return String.format("runs/%s", _runId);
    }
}