package com.adm.tools.common.autenvironment.request.post;

import com.adm.tools.common.autenvironment.request.AUTEnvironmentResources;
import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.request.GeneralPostRequest;
import com.adm.tools.common.Pair;

import java.util.ArrayList;
import java.util.List;

public class CreateAutEnvConfRequest extends GeneralPostRequest {

    private String autEnvironmentId;
    private String name;

    public CreateAutEnvConfRequest(Client client, String autEnvironmentId, String name) {

        super(client);
        this.autEnvironmentId = autEnvironmentId;
        this.name = name;
    }

    @Override
    protected String getSuffix() {
        return AUTEnvironmentResources.AUT_ENVIRONMENT_CONFIGURATIONS;
    }

    @Override
    protected List<Pair<String, String>> getDataFields() {

        List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
        ret.add(new Pair<String, String>("app-param-set-id", autEnvironmentId));
        ret.add(new Pair<String, String>("name", name));

        return ret;
    }
}
