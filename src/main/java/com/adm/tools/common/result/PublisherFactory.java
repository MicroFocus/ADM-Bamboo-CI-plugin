package com.adm.tools.common.result;

import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.SSEException;
import com.adm.tools.common.model.SseModel;

public class PublisherFactory {
    public Publisher create(Client client, String runType, String entityId, String runId) {

        Publisher ret = null;
        if ((SseModel.BVS.equals(runType)) || (SseModel.TEST_SET.equals(runType))) {
            ret = new LabPublisher(client, entityId, runId);
        } else if (SseModel.PC.equals(runType)) {
            ret = new PCPublisher(client, entityId, runId);
        } else {
            throw new SSEException("PublisherFactory: Unrecognized run type");
        }

        return ret;
    }
}
