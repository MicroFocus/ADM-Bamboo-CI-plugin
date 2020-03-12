package com.adm.tools.common.sdk.request;

import com.adm.tools.common.sdk.Client;

public class PostRequest extends GeneralPostRequest {

    protected final String _runId;

    protected PostRequest(Client client, String runId) {

        super(client);
        _runId = runId;
    }
}
