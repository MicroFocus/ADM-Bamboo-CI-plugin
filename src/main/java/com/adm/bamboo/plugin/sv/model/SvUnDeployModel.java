package com.adm.bamboo.plugin.sv.model;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class SvUnDeployModel {

    private SvServerSettingsModel serverSettingsModel;
    private SvServiceSelectionModel serviceSelectionModel;
    private boolean force;
    private boolean continueIfNotDeployed;

    public SvUnDeployModel(SvServerSettingsModel serverSettingsModel, SvServiceSelectionModel serviceSelectionModel, boolean force, boolean continueIfNotDeployed) {
        this.serverSettingsModel = serverSettingsModel;
        this.serviceSelectionModel = serviceSelectionModel;
        this.force = force;
        this.continueIfNotDeployed = continueIfNotDeployed;
    }

    public SvServerSettingsModel getServerSettingsModel() {
        return serverSettingsModel;
    }

    public SvServiceSelectionModel getServiceSelectionModel() {
        return serviceSelectionModel;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isContinueIfNotDeployed() {
        return continueIfNotDeployed;
    }
}
