package com.adm.utils.uft.enums;

public enum RunType {
    ALM("Alm"),
    FILE_SYSTEM("FileSystem"),
    LOAD_RUNNER("LoadRunner");

    private final String value;

    RunType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
