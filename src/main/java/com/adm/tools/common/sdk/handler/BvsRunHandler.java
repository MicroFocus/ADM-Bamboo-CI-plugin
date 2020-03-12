package com.adm.tools.common.sdk.handler;

import com.adm.tools.common.sdk.Client;

public class BvsRunHandler extends RunHandler{
    public BvsRunHandler(Client client, String entityId) {

        super(client, entityId);
    }

    @Override
    protected String getStartSuffix() {

        return String.format("procedures/%s/startrunprocedure", _entityId);
    }

    @Override
    public String getNameSuffix() {

        return String.format("procedures/%s", getEntityId());
    }
}
