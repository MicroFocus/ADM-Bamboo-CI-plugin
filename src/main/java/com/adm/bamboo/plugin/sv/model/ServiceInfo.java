package com.adm.bamboo.plugin.sv.model;

public class ServiceInfo {
    private final String id;
    private final String name;

    public ServiceInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
