package com.adm.utils.uft.enums;

public enum RunType {
    ALM("Alm"),
    ALM_LAB_MANAGEMENT("AlmLabManagement"),
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
