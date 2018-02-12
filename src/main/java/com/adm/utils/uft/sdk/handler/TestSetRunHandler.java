package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.sdk.Client;

public class TestSetRunHandler  extends RunHandler {

    public TestSetRunHandler(Client client, String entityId) {

        super(client, entityId);
    }

    @Override
    protected String getStartSuffix() {

        return String.format("test-sets/%s/startruntestset", _entityId);
    }

    @Override
    public String getNameSuffix() {

        return String.format("test-sets/%s", getEntityId());
    }
}
