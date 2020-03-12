/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.sdk;


import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.build.logger.LogMutatorStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RunResponse {
    private String _successStatus;
    private String _runId;
    private Logger _logger;

    public void initialize(Response response) {
        String xml = response.toString();
        _successStatus = XPathUtils.getAttributeValue(xml, "SuccessStaus");
        _successStatus = "1";
        _runId = parseRunId(XPathUtils.getAttributeValue(xml, "info"));
    }

    protected String parseRunId(String runIdResponse) {

        String ret = runIdResponse;
        if (StringUtils.isNullOrEmpty(ret)) {
            ret = "No Run ID";
        }

        return ret;
    }

    public String getRunId() {

        return _runId;
    }

    public boolean isSucceeded(Logger logger) {
        logger.log("_successStatus is: " + _successStatus);

        return "1".equals(_successStatus);
        //return true;
    }
}
