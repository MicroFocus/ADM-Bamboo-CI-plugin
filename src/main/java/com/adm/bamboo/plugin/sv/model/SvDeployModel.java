package com.adm.bamboo.plugin.sv.model;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class SvDeployModel {
    private boolean force;
    private boolean firstSuitableAgentFallback;
    private String serviceName;
    private SvServerSettingsModel serverSettingsModel;

    public SvDeployModel(SvServerSettingsModel serverSettingsModel, String serviceName, boolean force, boolean firstSuitableAgentFallback) {
        this.serverSettingsModel = serverSettingsModel;
        this.serviceName = serviceName;
        this.force = force;
        this.firstSuitableAgentFallback = firstSuitableAgentFallback;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isForce() {
        return force;
    }

    public SvServerSettingsModel getServerSettingsModel() {
        return serverSettingsModel;
    }

    public boolean isFirstSuitableAgentFallback() {
        return firstSuitableAgentFallback;
    }
}
