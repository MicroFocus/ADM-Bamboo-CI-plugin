package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConsts;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
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
