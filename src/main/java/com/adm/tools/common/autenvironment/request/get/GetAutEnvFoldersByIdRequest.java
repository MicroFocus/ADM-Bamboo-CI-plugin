package com.adm.tools.common.autenvironment.request.get;

import com.adm.tools.common.autenvironment.request.AUTEnvironmentResources;
import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.request.GeneralGetRequest;

public class GetAutEnvFoldersByIdRequest extends GeneralGetRequest {

    private String folderId;

    public GetAutEnvFoldersByIdRequest(Client client, String folderId) {

        super(client);
        this.folderId = folderId;
    }

    @Override
    protected String getSuffix() {

        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_FOLDERS;
    }

    @Override
    protected String getQueryString() {

        return String.format("query={id[%s]}&page-size=2000", folderId);
    }
}

