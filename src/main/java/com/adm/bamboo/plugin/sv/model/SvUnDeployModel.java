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
