package com.adm.tools.common.sdk.handler;

import com.adm.tools.common.sdk.Client;

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
