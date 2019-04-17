package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public class GetPCRunEntityTestSetRunsRequest extends GetRequest {

    public GetPCRunEntityTestSetRunsRequest(Client client, String runId) {

        super(client, runId);
    }

    @Override
    protected String getSuffix() {

        return String.format("runs/%s", _runId);
    }
}
