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

import com.hp.sv.jsvconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;

public class SvChangeModeModel {
    private SvServerSettingsModel serverSettingsModel;
    private SvServiceSelectionModel serviceSelectionModel;
    private boolean force;
    private static final SvDataModelSelection NONE_DATA_MODEL = new SvDataModelSelection(SvDataModelSelection.SelectionType.NONE, null);
    private static final SvPerformanceModelSelection NONE_PERFORMANCE_MODEL = new SvPerformanceModelSelection(SvPerformanceModelSelection.SelectionType.NONE, null);
    private final ServiceRuntimeConfiguration.RuntimeMode runtimeMode;
    private final SvDataModelSelection dataModel;
    private final SvPerformanceModelSelection performanceModel;

    public SvChangeModeModel(SvServerSettingsModel serverSettingsModel, SvServiceSelectionModel serviceSelectionModel, ServiceRuntimeConfiguration.RuntimeMode runtimeMode,
                             SvDataModelSelection dataModel, SvPerformanceModelSelection performanceModel, boolean force) {
        this.serverSettingsModel = serverSettingsModel;
        this.serviceSelectionModel = serviceSelectionModel;
        this.dataModel = dataModel;
        this.performanceModel = performanceModel;
        this.runtimeMode = runtimeMode;
        this.force = force;
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

    public ServiceRuntimeConfiguration.RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public SvDataModelSelection getDataModel() {
        return (runtimeMode == ServiceRuntimeConfiguration.RuntimeMode.STAND_BY) ? NONE_DATA_MODEL : dataModel;
    }

    public SvPerformanceModelSelection getPerformanceModel() {
        return (runtimeMode == ServiceRuntimeConfiguration.RuntimeMode.STAND_BY) ? NONE_PERFORMANCE_MODEL : performanceModel;
    }
}
