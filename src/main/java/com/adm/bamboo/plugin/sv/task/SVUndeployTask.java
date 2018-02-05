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

package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.ServiceInfo;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.adm.bamboo.plugin.sv.model.SvUnDeployModel;
import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.processor.IUndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessor;
import com.hp.sv.jsvconfigurator.processor.UndeployProcessorInput;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import org.apache.commons.lang.BooleanUtils;

import java.net.URL;
import java.util.Date;
import java.util.List;

public class SVUndeployTask implements TaskType {
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
        boolean force = BooleanUtils.toBoolean(map.get(SVConstants.FORCE));
        boolean continueIfNotDeployed = BooleanUtils.toBoolean(map.get(SVConstants.CONTINUE));
        String serviceSelection = map.get(SVConstants.SERVICE_SELECTION);

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);
        SvUnDeployModel svUnDeployModel = new SvUnDeployModel(svServerSettingsModel, svServiceSelectionModel, force, continueIfNotDeployed);
        logConfig(buildLogger, svUnDeployModel, startDate, SVConstants.PREFIX);

        try {
            unDeployServiceFromProject(svUnDeployModel, buildLogger);
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage(), e);
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: UnDeploy Virtual Service in %.3f seconds%n%n", duration));
        }

        return TaskResultBuilder.create(taskContext).success().build();
    }

    /**
     * Undeploy virtual service
     *
     * @param svUnDeployModel
     * @param buildLogger
     * @return
     */
    private void unDeployServiceFromProject(SvUnDeployModel svUnDeployModel, BuildLogger buildLogger) throws Exception {
        IUndeployProcessor processor = new UndeployProcessor(null);
        ICommandExecutor commandExecutor = SVExecutorUtil.createCommandExecutor(new URL(svUnDeployModel.getServerSettingsModel().getUrl()),
                new Credentials(svUnDeployModel.getServerSettingsModel().getUsername(),svUnDeployModel.getServerSettingsModel().getPassword()));
        List<ServiceInfo> serviceInfoList = SVExecutorUtil.getServiceList(commandExecutor, svUnDeployModel.getServiceSelectionModel(), svUnDeployModel.isContinueIfNotDeployed(), buildLogger);
        for (ServiceInfo service : serviceInfoList) {
            buildLogger.addBuildLogEntry(String.format("  Undeploying service '%s' [%s] %n", service.getName(), service.getId()));
            UndeployProcessorInput undeployProcessorInput = new UndeployProcessorInput(svUnDeployModel.isForce(), null, service.getId());
            processor.process(undeployProcessorInput, commandExecutor);
        }
    }

    private void logConfig(BuildLogger buildLogger, SvUnDeployModel svUnDeployModel, Date startDate, String prefix) {
        buildLogger.addBuildLogEntry(String.format("%nStarting UnDeploy Virtual Service for SV Server '%s' as %s on %s%n",
                svUnDeployModel.getServerSettingsModel().getUrl(), svUnDeployModel.getServerSettingsModel().getUsername(), startDate));
        SVExecutorUtil.logConfig(svUnDeployModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Force: " + svUnDeployModel.isForce());
        buildLogger.addBuildLogEntry(prefix + "Continue if not deployed: " + svUnDeployModel.isContinueIfNotDeployed());
    }
}
