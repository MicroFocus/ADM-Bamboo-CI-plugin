package com.adm.utils.uft.model;

public class EnumDescription {
    public EnumDescription(String value, String description) {
        this.value = value;
        this.description = description;
    }

    private String description;
    private String value;

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}
