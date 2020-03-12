package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public class StopEntityRequest extends PostRequest {

    public StopEntityRequest(Client client, String runId) {

        super(client, runId);
    }

    @Override
    protected String getSuffix() {

        return String.format("procedure-runs/%s/stop", _runId);
    }

}
