package com.adm.utils.uft.autenvironment.request.put;

import com.adm.utils.uft.autenvironment.request.AUTEnvironmentResources;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.request.GeneralPutBulkRequest;

import java.util.*;

public class PutAutEnvironmentParametersBulkRequest extends GeneralPutBulkRequest {

    private Collection<AUTEnvironmnentParameter> parameters;

    public PutAutEnvironmentParametersBulkRequest(
            Client client,
            Collection<AUTEnvironmnentParameter> parameters) {
        super(client);
        this.parameters = parameters;
    }

    @Override
    protected List<Map<String, String>> getFields() {

        List<Map<String, String>> fieldsToUpdate = new ArrayList<Map<String, String>>();
        for (AUTEnvironmnentParameter autEnvironmnentParameter : parameters) {
            Map<String, String> mapOfValues = new HashMap<String, String>();
            mapOfValues.put(
                    AUTEnvironmnentParameter.ALM_PARAMETER_ID_FIELD,
                    autEnvironmnentParameter.getId());
            mapOfValues.put(
                    AUTEnvironmnentParameter.ALM_PARAMETER_VALUE_FIELD,
                    autEnvironmnentParameter.getValue());
            fieldsToUpdate.add(mapOfValues);
        }

        return fieldsToUpdate;
    }

    @Override
    protected String getSuffix() {
        return AUTEnvironmentResources.AUT_ENVIRONMENT_PARAMETER_VALUES;
    }
}

