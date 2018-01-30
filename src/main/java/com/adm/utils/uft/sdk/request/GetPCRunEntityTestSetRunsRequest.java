package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;

public class GetPCRunEntityTestSetRunsRequest extends GetRequest {

    public GetPCRunEntityTestSetRunsRequest(Client client, String runId) {

        super(client, runId);
    }

    @Override
    protected String getSuffix() {

        return String.format("runs/%s", _runId);
    }
}
