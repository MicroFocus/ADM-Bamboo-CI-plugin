package com.adm.bamboo.plugin.sv.model;

import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class SvExportModel {
    private SvServerSettingsModel serverSettingsModel;
    private SvServiceSelectionModel serviceSelectionModel;
    private String targetDirectory;
    private boolean cleanTargetDirectory;
    private boolean switchToStandByFirst;
    private boolean force;

    public SvExportModel(SvServerSettingsModel serverSettingsModel, SvServiceSelectionModel serviceSelectionModel, String targetDirectory, boolean cleanTargetDirectory, boolean switchToStandByFirst, boolean force) {
        this.serverSettingsModel = serverSettingsModel;
        this.serviceSelectionModel = serviceSelectionModel;
        this.targetDirectory = targetDirectory;
        this.cleanTargetDirectory = cleanTargetDirectory;
        this.switchToStandByFirst = switchToStandByFirst;
        this.force = force;
    }

    public SvServerSettingsModel getServerSettingsModel() {
        return serverSettingsModel;
    }

    public SvServiceSelectionModel getServiceSelectionModel() {
        return serviceSelectionModel;
    }

    public String getTargetDirectory() {
        return (StringUtils.isNotBlank(targetDirectory)) ? targetDirectory : null;
    }

    public boolean isCleanTargetDirectory() {
        return cleanTargetDirectory;
    }

    public boolean isSwitchToStandByFirst() {
        return switchToStandByFirst;
    }

    public boolean isForce() {
        return force;
    }
}
