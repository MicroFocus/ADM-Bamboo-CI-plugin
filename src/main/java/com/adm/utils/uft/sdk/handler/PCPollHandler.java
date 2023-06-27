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

package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.sdk.request.GetPCRunEntityDataRequest;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;

import java.util.Arrays;
import java.util.List;

public class PCPollHandler extends PollHandler {

    private final static List<String> FINAL_STATES = Arrays.asList("N/A", "Failed", "Passed");

    public PCPollHandler(Client client, String entityId) {

        super(client, entityId);
    }

    public PCPollHandler(Client client, String entityId, int interval) {

        super(client, entityId, interval);
    }

    @Override
    protected Response getResponse() {

        return new GetPCRunEntityDataRequest(_client, _runId).execute();
    }

    @Override
    protected boolean isFinished(Response response, Logger logger) {

        boolean ret = false;
        try {
            String xml = response.toString();
            String pcEndTime = XPathUtils.getAttributeValue(xml, "pc-end-time");
            String status = XPathUtils.getAttributeValue(xml, "status");
            if (!StringUtils.isNullOrEmpty(pcEndTime)) {
                logger.log(String.format("PC test end time: %s", pcEndTime));
                ret = true;
            } else if (!StringUtils.isNullOrEmpty(status)) {
                if (FINAL_STATES.contains(status)) {
                    ret = true;
                }
            }
        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
            ret = true;
        }

        return ret;
    }

    @Override
    protected boolean logRunEntityResults(Response response, Logger logger) {

        boolean ret = false;
        try {
            String xml = response.toString();
            String status = XPathUtils.getAttributeValue(xml, "status");
            String state = XPathUtils.getAttributeValue(xml, "state");
            logger.log(String.format("Run status of %s: %s, State: %s", _runId, status, state));
            ret = true;

        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
        }

        return ret;
    }

    @Override
    protected Response getRunEntityResultsResponse() {

        return new GetPCRunEntityDataRequest(_client, _runId).execute();
    }
}
