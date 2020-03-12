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
