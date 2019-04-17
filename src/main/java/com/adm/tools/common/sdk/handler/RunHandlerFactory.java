package com.adm.tools.common.sdk.handler;

import com.adm.tools.common.SSEException;
import com.adm.tools.common.model.SseModel;
import com.adm.tools.common.sdk.Client;

public class RunHandlerFactory {
    public RunHandler create(Client client, String runType, String entityId) {

        RunHandler ret = null;
        if (SseModel.BVS.equals(runType)) {
            ret = new BvsRunHandler(client, entityId);
        } else if (SseModel.TEST_SET.equals(runType)) {
            ret = new TestSetRunHandler(client, entityId);
        } else if (SseModel.PC.equals(runType)) {
            ret = new PCRunHandler(client, entityId);
        } else {
            throw new SSEException("RunHandlerFactory: Unrecognized run type");
        }

        return ret;
    }
}
