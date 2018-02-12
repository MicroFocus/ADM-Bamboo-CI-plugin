package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;

public class PostRequest extends GeneralPostRequest {

    protected final String _runId;

    protected PostRequest(Client client, String runId) {

        super(client);
        _runId = runId;
    }
}
