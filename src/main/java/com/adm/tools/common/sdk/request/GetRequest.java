package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public abstract class GetRequest extends GeneralGetRequest {

    protected final String _runId;

    protected GetRequest(Client client, String runId) {

        super(client);
        _runId = runId;
    }

}
