package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.model.SseModel;
import com.adm.utils.uft.sdk.Client;

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
