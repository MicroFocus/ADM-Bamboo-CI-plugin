/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.sv.model;

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
