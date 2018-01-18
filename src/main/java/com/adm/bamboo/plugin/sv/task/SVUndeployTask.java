package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.ServiceInfo;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.adm.bamboo.plugin.sv.model.SvUnDeployModel;
import com.adm.utils.sv.SVConsts;
import com.adm.utils.sv.SVExecutor;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hp.sv.jsvconfigurator.build.ProjectBuilder;
import com.hp.sv.jsvconfigurator.core.IProject;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommandExecutorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommunicatorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.ProjectBuilderException;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.processor.IUndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessorInput;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVUndeployTask implements TaskType {
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
        boolean force = BooleanUtils.toBoolean(map.get(SVConsts.FORCE));
        boolean continueIfNotDeployed = BooleanUtils.toBoolean(map.get(SVConsts.CONTINUE));
        String serviceSelection = map.get(SVConsts.SERVICE_SELECTION);

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);
        SvUnDeployModel svUnDeployModel = new SvUnDeployModel(svServerSettingsModel, svServiceSelectionModel, force, continueIfNotDeployed);
        logConfig(buildLogger, svUnDeployModel, startDate, "    ");

        try {
            unDeployServiceFromProject(svUnDeployModel, buildLogger);
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: UnDeploy Virtual Service in %.3f seconds%n%n", duration));
        }

        return TaskResultBuilder.create(taskContext).success().build();
    }

    private void unDeployServiceFromProject(SvUnDeployModel svUnDeployModel, BuildLogger buildLogger) throws Exception {
        IUndeployProcessor processor = new UndeployProcessor(null);
        ICommandExecutor commandExecutor = svExecutor.createCommandExecutor(new URL(svUnDeployModel.getServerSettingsModel().getUrl()),
                new Credentials(svUnDeployModel.getServerSettingsModel().getUsername(),svUnDeployModel.getServerSettingsModel().getPassword()));
        List<ServiceInfo> serviceInfoList = svExecutor.getServiceList(commandExecutor, svUnDeployModel.getServiceSelectionModel(), svUnDeployModel.isContinueIfNotDeployed(), buildLogger);
        for (ServiceInfo service : serviceInfoList) {
            buildLogger.addBuildLogEntry(String.format("  Undeploying service '%s' [%s] %n", service.getName(), service.getId()));
            UndeployProcessorInput undeployProcessorInput = new UndeployProcessorInput(svUnDeployModel.isForce(), null, service.getId());
            processor.process(undeployProcessorInput, commandExecutor);
        }
    }

    private void logConfig(BuildLogger buildLogger, SvUnDeployModel svUnDeployModel, Date startDate, String prefix) {
        buildLogger.addBuildLogEntry(String.format("%nStarting UnDeploy Virtual Service for SV Server '%s' as %s on %s%n",
                svUnDeployModel.getServerSettingsModel().getUrl(), svUnDeployModel.getServerSettingsModel().getUsername(), startDate));
        svExecutor.logConfig(svUnDeployModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Force: " + svUnDeployModel.isForce());
        buildLogger.addBuildLogEntry(prefix + "Continue if not deployed: " + svUnDeployModel.isContinueIfNotDeployed());
    }
}
