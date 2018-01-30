package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;

public class GetLabRunEntityTestSetRunsRequest  extends GetRequest {

    public GetLabRunEntityTestSetRunsRequest(Client client, String runId) {

        super(client, runId);
    }

    @Override
    protected String getSuffix() {
        return "procedure-testset-instance-runs";
    }

    @Override
    protected String getQueryString() {

        return String.format("query={procedure-run[%s]}&page-size=2000", _runId);
    }

}

