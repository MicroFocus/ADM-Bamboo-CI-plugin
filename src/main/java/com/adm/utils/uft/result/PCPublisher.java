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

package com.adm.utils.uft.result;

import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;
import com.adm.utils.uft.sdk.request.GetPCRunEntityTestSetRunsRequest;
import com.adm.utils.uft.sdk.request.GetRequest;

public class PCPublisher extends Publisher {

    public PCPublisher(Client client, String entityId, String runId) {

        super(client, entityId, runId);
    }

    protected String getEntityName(String nameSuffix, Logger logger) {

        String ret = "Unnamed Entity";
        try {
            Response response = getEntityName(nameSuffix);
            if (response.isOk() && !response.toString().equals("")) {
                String runId = XPathUtils.getAttributeValue(response.toString(), "id");
                String testId = XPathUtils.getAttributeValue(response.toString(), "testcycl-id");
                String testSetId = XPathUtils.getAttributeValue(response.toString(), "cycle-id");
                ret =
                        String.format(
                                "PC Test ID: %s, Run ID: %s, Test Set ID: %s",
                                testId,
                                runId,
                                testSetId);
            } else {
                Throwable failure = response.getFailure();
                logger.log(String.format(
                        "Failed to get Entity name. Exception: %s",
                        failure == null ? "null" : failure.getMessage()));
            }
        } catch (Throwable e) {
            logger.log("Failed to get Entity name");
        }

        return ret;
    }

    @Override
    protected GetRequest getRunEntityTestSetRunsRequest(Client client, String runId) {

        return new GetPCRunEntityTestSetRunsRequest(client, runId);
    }
}

