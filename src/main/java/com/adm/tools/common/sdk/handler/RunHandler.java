package com.adm.tools.common.sdk.handler;

import com.adm.tools.common.sdk.request.StartRunEntityRequest;
import com.adm.tools.common.sdk.request.StopEntityRequest;
import com.adm.tools.common.model.CdaDetails;
import com.adm.tools.common.sdk.*;

public abstract class RunHandler extends Handler {
    protected abstract String getStartSuffix();

    public abstract String getNameSuffix();

    public RunHandler(Client client, String entityId) {

        super(client, entityId);
    }

    public Response start(
            String duration,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {

        return new StartRunEntityRequest(
                _client,
                getStartSuffix(),
                _entityId,
                duration,
                postRunAction,
                environmentConfigurationId,
                cdaDetails).execute();
    }

    public Response stop() {

        return new StopEntityRequest(_client, _runId).execute();
    }

    public String getReportUrl(Args args) {

        return new ALMRunReportUrlBuilder().build(_client, _client.getServerUrl(), args.getDomain(), args.getProject(), _runId);
    }

    public RunResponse getRunResponse(Response response) {

        RunResponse ret = new RunResponse();
        ret.initialize(response);

        return ret;

    }
}
