package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.*;
import com.adm.utils.sv.SVConsts;
import com.adm.utils.sv.SVExecutor;
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
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVChangeModeTask implements TaskType {
    SVExecutor svExecutor = new SVExecutor();
    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        Date startDate = new Date();
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final ConfigurationMap map = taskContext.getConfigurationMap();

        String serverURL = map.get(SVConsts.URL);
        String userName = map.get(SVConsts.USERNAME);
        String userPassword = map.get(SVConsts.PASSWORD);
        String projectPath = map.get(SVConsts.PROJECT_PATH);
        String projectPassword = map.get(SVConsts.PROJECT_PASSWORD);
        String serviceName = map.get(SVConsts.SERVICE_NAME_OR_ID);
        serviceName = (serviceName == null || serviceName.isEmpty()) ? null : serviceName;
        String dmNameOrId = map.get(SVConsts.DM_NAME_OR_ID);
        String pmNameOrId = map.get(SVConsts.PM_NAME_OR_ID);
        String serviceSelection = map.get(SVConsts.SERVICE_SELECTION);
        String serviceMode = map.get(SVConsts.SERVICE_MODE);
        String dataModel = map.get(SVConsts.DATA_MODEL);
        String pmModel = map.get(SVConsts.PERFORMANCE_MODEL);
        boolean force = BooleanUtils.toBoolean(map.get(SVConsts.FORCE));

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);

        ServiceRuntimeConfiguration.RuntimeMode runtimeMode = ServiceRuntimeConfiguration.RuntimeMode.valueOf(serviceMode);
        SvDataModelSelection svDataModelSelection = new SvDataModelSelection(null, dmNameOrId);
        svDataModelSelection.setSelectionType(dataModel);

        SvPerformanceModelSelection svPerformanceModelSelection = new SvPerformanceModelSelection(null, pmNameOrId);
        svPerformanceModelSelection.setSelectionType(pmModel);

        SvChangeModeModel svChangeModeModel = new SvChangeModeModel(svServerSettingsModel, svServiceSelectionModel, runtimeMode, svDataModelSelection, svPerformanceModelSelection, force);

        logConfig(buildLogger, svChangeModeModel, startDate, "    ");
        ICommandExecutor commandExecutor = null;
        try {
            commandExecutor = svExecutor.createCommandExecutor(new URL(svServerSettingsModel.getUrl()),
                    new Credentials(svServerSettingsModel.getUsername(),svServerSettingsModel.getPassword()));
            List<ServiceInfo> serviceInfoList = svExecutor.getServiceList(commandExecutor, svServiceSelectionModel, false, buildLogger);
            for (ServiceInfo service : serviceInfoList) {
                changeServiceMode(svChangeModeModel, service, buildLogger, commandExecutor);
            }
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: Change Mode of Virtual Service in %.3f seconds%n%n", duration));
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

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

    private ServiceRuntimeConfiguration.RuntimeMode getDisplayRuntimeMode(ServiceRuntimeConfiguration info) {
        // display SIMULATING in case of STAND_BY mode with PM set (as it is done in designer and SVM)
        return (info.getRuntimeMode() == ServiceRuntimeConfiguration.RuntimeMode.STAND_BY && info.getPerfModelId() != null)
                ? ServiceRuntimeConfiguration.RuntimeMode.SIMULATING
                : info.getRuntimeMode();
    }

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
        svExecutor.logConfig(svChangeModeModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Mode: " + svChangeModeModel.getRuntimeMode().toString());
        buildLogger.addBuildLogEntry(prefix + "Data model: " + svChangeModeModel.getDataModel().toString());
        buildLogger.addBuildLogEntry(prefix + "Performance model: " + svChangeModeModel.getPerformanceModel().toString());
        buildLogger.addBuildLogEntry(prefix + "Force: " + svChangeModeModel.isForce());
    }
}
