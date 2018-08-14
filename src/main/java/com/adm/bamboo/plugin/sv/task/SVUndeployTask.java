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
