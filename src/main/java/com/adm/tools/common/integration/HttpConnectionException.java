package com.adm.tools.common.integration;

public class HttpConnectionException extends Exception{
    private String errorMsg;

    public HttpConnectionException(){}

    public HttpConnectionException(String msg) {
        super(msg);
        errorMsg = msg;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
