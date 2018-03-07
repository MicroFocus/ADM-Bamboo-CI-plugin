/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
