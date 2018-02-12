package com.adm.utils.uft.sdk;

import com.adm.utils.uft.StringUtils;

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

