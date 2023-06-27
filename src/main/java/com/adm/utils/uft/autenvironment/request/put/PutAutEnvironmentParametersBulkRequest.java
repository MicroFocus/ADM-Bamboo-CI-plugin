/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

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

