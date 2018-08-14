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

package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.*;
import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hp.sv.jsvconfigurator.core.IProjectElement;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.processor.ChmodeProcessor;
import com.hp.sv.jsvconfigurator.processor.ChmodeProcessorInput;
import com.hp.sv.jsvconfigurator.processor.IChmodeProcessor;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import org.apache.commons.lang.BooleanUtils;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SVChangeModeTask implements TaskType {
    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException {
        Date startDate = new Date();
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final ConfigurationMap map = taskContext.getConfigurationMap();

        String serverURL = map.get(SVConstants.URL);
        String userName = map.get(SVConstants.USERNAME);
        String userPassword = map.get(SVConstants.PASSWORD);
        String projectPath = map.get(SVConstants.PROJECT_PATH);
        String projectPassword = map.get(SVConstants.PROJECT_PASSWORD);
        String serviceName = map.get(SVConstants.SERVICE_NAME_OR_ID);
        serviceName = (serviceName == null || serviceName.isEmpty()) ? null : serviceName;
        String dmNameOrId = map.get(SVConstants.DM_NAME_OR_ID);
        String pmNameOrId = map.get(SVConstants.PM_NAME_OR_ID);
        String serviceSelection = map.get(SVConstants.SERVICE_SELECTION);
        String serviceMode = map.get(SVConstants.SERVICE_MODE);
        String dataModel = map.get(SVConstants.DATA_MODEL);
        String pmModel = map.get(SVConstants.PERFORMANCE_MODEL);
        boolean force = BooleanUtils.toBoolean(map.get(SVConstants.FORCE));

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);

        ServiceRuntimeConfiguration.RuntimeMode runtimeMode = ServiceRuntimeConfiguration.RuntimeMode.valueOf(serviceMode);
        SvDataModelSelection svDataModelSelection = new SvDataModelSelection(null, dmNameOrId);
        svDataModelSelection.setSelectionType(dataModel);

        SvPerformanceModelSelection svPerformanceModelSelection = new SvPerformanceModelSelection(null, pmNameOrId);
        svPerformanceModelSelection.setSelectionType(pmModel);

        SvChangeModeModel svChangeModeModel = new SvChangeModeModel(svServerSettingsModel, svServiceSelectionModel, runtimeMode, svDataModelSelection, svPerformanceModelSelection, force);

        logConfig(buildLogger, svChangeModeModel, startDate, SVConstants.PREFIX);
        ICommandExecutor commandExecutor = null;
        try {
            commandExecutor = SVExecutorUtil.createCommandExecutor(new URL(svServerSettingsModel.getUrl()),
                    new Credentials(svServerSettingsModel.getUsername(),svServerSettingsModel.getPassword()));
            List<ServiceInfo> serviceInfoList = SVExecutorUtil.getServiceList(commandExecutor, svServiceSelectionModel, false, buildLogger);
            for (ServiceInfo service : serviceInfoList) {
                changeServiceMode(svChangeModeModel, service, buildLogger, commandExecutor);
            }
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage(), e);
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: Change Mode of Virtual Service in %.3f seconds%n%n", duration));
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    /**
     * change the virtual service model
     *
     * @param svChangeModeModel
     * @param serviceInfo
     * @param buildLogger
     * @return the command executor
     */
    private void changeServiceMode(SvChangeModeModel svChangeModeModel, ServiceInfo serviceInfo, BuildLogger buildLogger, ICommandExecutor commandExecutor) throws Exception {

        String dataModel = svChangeModeModel.getDataModel().getSelectedModelName();
        String performanceModel = svChangeModeModel.getPerformanceModel().getSelectedModelName();
        boolean useDefaultDataModel = svChangeModeModel.getDataModel().isDefaultSelected();
        boolean useDefaultPerformanceModel = svChangeModeModel.getPerformanceModel().isDefaultSelected();
        ServiceRuntimeConfiguration.RuntimeMode targetMode = getTargetMode(svChangeModeModel);

        ChmodeProcessorInput chModeInput = new ChmodeProcessorInput(svChangeModeModel.isForce(), null, serviceInfo.getId(),
                dataModel, performanceModel, targetMode, useDefaultDataModel, useDefaultPerformanceModel);

        buildLogger.addBuildLogEntry(String.format("    Changing mode of service '%s' [%s] to %s mode%n", serviceInfo.getName(), serviceInfo.getId(), svChangeModeModel.getRuntimeMode()));

        IChmodeProcessor processor = new ChmodeProcessor(null);

        try {
            processor.process(chModeInput, commandExecutor);
        } finally {
            printServiceStatus(buildLogger, serviceInfo, commandExecutor);
        }
    }

    /**
     * get the virtual service mode
     *
     * @param svChangeModeModel
     * @return the virtual service mode
     */
    private ServiceRuntimeConfiguration.RuntimeMode getTargetMode(SvChangeModeModel svChangeModeModel) {
        // Set STAND_BY with PM in case of simulation without data model to be in accord with designer & SVM
        if (svChangeModeModel.getRuntimeMode() == ServiceRuntimeConfiguration.RuntimeMode.SIMULATING
                && !svChangeModeModel.getPerformanceModel().isNoneSelected()
                && svChangeModeModel.getDataModel().isNoneSelected()) {
            return ServiceRuntimeConfiguration.RuntimeMode.STAND_BY;
        }

        return svChangeModeModel.getRuntimeMode();
    }

    private void printServiceStatus(BuildLogger buildLogger, ServiceInfo serviceInfo, ICommandExecutor commandExecutor) {
        try {
            IService service = commandExecutor.findService(serviceInfo.getId(), null);
            ServiceRuntimeConfiguration info = commandExecutor.getServiceRuntimeInfo(service);
            ServiceRuntimeConfiguration.RuntimeMode mode = getDisplayRuntimeMode(info);

            buildLogger.addBuildLogEntry(String.format("    Service '%s' [%s] is in %s mode%n", service.getName(), service.getId(), mode));
            if (mode == ServiceRuntimeConfiguration.RuntimeMode.LEARNING || mode == ServiceRuntimeConfiguration.RuntimeMode.SIMULATING) {
                buildLogger.addBuildLogEntry("      Data model: " + getModelName(service.getDataModels(), info.getDataModelId()));
                buildLogger.addBuildLogEntry("      Performance model: " + getModelName(service.getPerfModels(), info.getPerfModelId()));
            }

            if (info.getDeploymentErrorMessage() != null) {
                buildLogger.addBuildLogEntry("      Error message: " + info.getDeploymentErrorMessage());
            }
        } catch (Exception e) {
            String msg = String.format("Failed to get detail of service '%s' [%s]", serviceInfo.getName(), serviceInfo.getId());
            buildLogger.addBuildLogEntry(String.format("      %s: %s%n", msg, e.getMessage()));
        }
    }

    /**
     * get the virtual service mode display name
     *
     * @param info
     * @return the virtual service mode display name
     */
    private ServiceRuntimeConfiguration.RuntimeMode getDisplayRuntimeMode(ServiceRuntimeConfiguration info) {
        // display SIMULATING in case of STAND_BY mode with PM set (as it is done in designer and SVM)
        return (info.getRuntimeMode() == ServiceRuntimeConfiguration.RuntimeMode.STAND_BY && info.getPerfModelId() != null)
                ? ServiceRuntimeConfiguration.RuntimeMode.SIMULATING
                : info.getRuntimeMode();
    }

    /**
     * get the virtual service data/performance model
     *
     * @param models
     * @param modelId
     * @return the virtual service data/performance model
     */
    private String getModelName(Collection<? extends IProjectElement> models, String modelId) {
        for (IProjectElement model : models) {
            if (model.getId().equals(modelId)) {
                return String.format("'%s' [%s]", model.getName(), modelId);
            }
        }
        return null;
    }

    protected void logConfig(BuildLogger buildLogger, SvChangeModeModel svChangeModeModel, Date startDate, String prefix) {
        buildLogger.addBuildLogEntry(String.format("%nStarting Change Mode of Virtual Service for SV Server '%s' as %s on %s%n",
                svChangeModeModel.getServerSettingsModel().getUrl(), svChangeModeModel.getServerSettingsModel().getUsername(), startDate));
        SVExecutorUtil.logConfig(svChangeModeModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Mode: " + svChangeModeModel.getRuntimeMode().toString());
        buildLogger.addBuildLogEntry(prefix + "Data model: " + svChangeModeModel.getDataModel().toString());
        buildLogger.addBuildLogEntry(prefix + "Performance model: " + svChangeModeModel.getPerformanceModel().toString());
        buildLogger.addBuildLogEntry(prefix + "Force: " + svChangeModeModel.isForce());
    }
}
