/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.adm.utils.uft.result;

import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;
import com.adm.utils.uft.sdk.handler.Handler;
import com.adm.utils.uft.sdk.request.GetRequest;
import com.adm.utils.uft.sdk.request.GetRunEntityNameRequest;

import java.util.List;
import java.util.Map;

public abstract class Publisher extends Handler {
    public Publisher(Client client, String entityId, String runId) {

        super(client, entityId, runId);
    }

    public Testsuites publish(
            String nameSuffix,
            String url,
            String domain,
            String project,
            Logger logger) {

        Testsuites ret = null;
        GetRequest testSetRunsRequest = getRunEntityTestSetRunsRequest(_client, _runId);
        Response response = testSetRunsRequest.execute();
        List<Map<String, String>> testInstanceRun = getTestInstanceRun(response, logger);
        String entityName = getEntityName(nameSuffix, logger);
        if (testInstanceRun != null) {
            ret =
                    new JUnitParser().toModel(
                            testInstanceRun,
                            entityName,
                            _runId,
                            url,
                            domain,
                            project);
        }

        return ret;
    }

    protected Response getEntityName(String nameSuffix) {

        return new GetRunEntityNameRequest(_client, nameSuffix, _entityId).execute();
    }

    protected List<Map<String, String>> getTestInstanceRun(Response response, Logger logger) {

        List<Map<String, String>> ret = null;
        try {
            if (!StringUtils.isNullOrEmpty(response.toString())) {
                ret = XPathUtils.toEntities(response.toString());
            }
        } catch (Throwable cause) {
            logger.log(String.format(
                    "Failed to parse TestInstanceRuns response XML. Exception: %s, XML: %s",
                    cause.getMessage(),
                    response.toString()));
        }

        return ret;
    }

    protected abstract GetRequest getRunEntityTestSetRunsRequest(Client client, String runId);

    protected abstract String getEntityName(String nameSuffix, Logger logger);
}
