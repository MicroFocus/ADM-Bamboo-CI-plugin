package com.adm.utils.uft.sdk;

import com.adm.utils.uft.rest.HttpHeaders;

public enum ResourceAccessLevel{
    PUBLIC(null), PROTECTED(HttpHeaders.PtaL), PRIVATE(HttpHeaders.PvaL);

    private String _headerName;

    private ResourceAccessLevel(String headerName) {
        _headerName = headerName;
    }

    public String getUserHeaderName(){
        return _headerName;
    }

}

