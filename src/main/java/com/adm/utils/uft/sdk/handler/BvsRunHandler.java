package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.sdk.Client;

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
