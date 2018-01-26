package com.adm.tools.common.sdk;

import com.adm.tools.common.StringUtils;

public class PCRunResponse  extends RunResponse {

    @Override
    protected String parseRunId(String runIdResponse) {
        String ret = runIdResponse;
        if (!StringUtils.isNullOrEmpty(ret)) {
            String runIdStr = "qcRunID=";
            if (ret.contains(runIdStr)) {
                ret = ret.substring(ret.indexOf(runIdStr) + runIdStr.length(), ret.length());
            }
        }
        return ret;
    }

}

