/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.adm.bamboo.plugin.sv.model;

import org.apache.commons.lang.StringUtils;

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
