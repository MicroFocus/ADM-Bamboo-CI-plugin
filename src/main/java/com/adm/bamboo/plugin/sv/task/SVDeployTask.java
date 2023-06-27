/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.SvDeployModel;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.microfocus.sv.svconfigurator.build.ProjectBuilder;
import com.microfocus.sv.svconfigurator.core.IDataModel;
import com.microfocus.sv.svconfigurator.core.IPerfModel;
import com.microfocus.sv.svconfigurator.core.IProject;
import com.microfocus.sv.svconfigurator.core.IService;
import com.microfocus.sv.svconfigurator.core.impl.exception.ProjectBuilderException;
import com.microfocus.sv.svconfigurator.core.impl.processor.Credentials;
import com.microfocus.sv.svconfigurator.processor.DeployProcessor;
import com.microfocus.sv.svconfigurator.processor.DeployProcessorInput;
import com.microfocus.sv.svconfigurator.processor.IDeployProcessor;
import com.microfocus.sv.svconfigurator.serverclient.ICommandExecutor;
import com.microfocus.sv.svconfigurator.service.ServiceAmendingServiceImpl;
import com.microfocus.sv.svconfigurator.util.ProjectUtils;
import org.apache.commons.lang.BooleanUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class SVDeployTask implements TaskType {
    @Override
    public TaskResult execute(final TaskContext taskContext) {
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
        boolean firstSuitableAgentFallback = BooleanUtils.toBoolean(map.get(SVConstants.FIRST_SUITABLE_AGENT_FALLBACK));

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvDeployModel svDeployModel = new SvDeployModel(svServerSettingsModel, serviceName, force, firstSuitableAgentFallback);

        logConfig(buildLogger, svDeployModel, startDate, SVConstants.PREFIX);
        try {
            IProject project = new ProjectBuilder().buildProject(new File(projectPath), projectPassword);
            printProjectContent(project, buildLogger);
            deployServiceFromProject(svDeployModel, project, buildLogger);
        } catch (ProjectBuilderException e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage(), e);
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: Deploy Virtual Service in %.3f seconds%n%n", duration));
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    private void printProjectContent(IProject project, BuildLogger buildLogger) {
        buildLogger.addBuildLogEntry("  Project content:");
        for (IService service : project.getServices()) {
            buildLogger.addBuildLogEntry("    Service: " + service.getName() + " [" + service.getId() + "]");
            for (IDataModel dataModel : service.getDataModels()) {
                buildLogger.addBuildLogEntry("      DM: " + dataModel.getName() + " [" + dataModel.getId() + "]");
            }
            for (IPerfModel perfModel : service.getPerfModels()) {
                buildLogger.addBuildLogEntry("      PM: " + perfModel.getName() + " [" + perfModel.getId() + "]");
            }
        }
    }

    /**
     * deploy virtual service
     *
     * @param svDeployModel
     * @param project
     * @param buildLogger
     * @return
     */
    private void deployServiceFromProject(SvDeployModel svDeployModel, IProject project, BuildLogger buildLogger) throws Exception {
        IDeployProcessor processor = new DeployProcessor(null);
        ICommandExecutor commandExecutor = SVExecutorUtil.createCommandExecutor(new URL(svDeployModel.getServerSettingsModel().getUrl()),
                new Credentials(svDeployModel.getServerSettingsModel().getUsername(),svDeployModel.getServerSettingsModel().getPassword()));

        for (IService service : getServiceList(svDeployModel, project)) {
            buildLogger.addBuildLogEntry(String.format("  Deploying service '%s' [%s] %n", service.getName(), service.getId()));
            DeployProcessorInput deployInput = new DeployProcessorInput(svDeployModel.isForce(), false, project, svDeployModel.getServiceName(), null, false);
            deployInput.setFirstAgentFailover(svDeployModel.isFirstSuitableAgentFallback());
            processor.process(deployInput, commandExecutor);
        }
    }

    /**
     * get the virtual services list
     *
     * @param svDeployModel
     * @param project
     * @return the virtual services list
     */
    private Iterable<IService> getServiceList(SvDeployModel svDeployModel, IProject project) {
        if (svDeployModel.getServiceName() == null || svDeployModel.getServiceName().isEmpty()) {
            return project.getServices();
        } else {
            ArrayList<IService> list = new ArrayList<>();
            list.add(ProjectUtils.findProjElem(project.getServices(), svDeployModel.getServiceName()));
            return list;
        }
    }

    private void logConfig(BuildLogger buildLogger, SvDeployModel svDeployModel, Date startDate, String prefix) {
        buildLogger.addBuildLogEntry(String.format("%nStarting Deploy Virtual Service for SV Server '%s' as %s on %s%n",
                svDeployModel.getServerSettingsModel().getUrl(), svDeployModel.getServerSettingsModel().getUsername(), startDate));
        buildLogger.addBuildLogEntry(prefix + "Force: " + svDeployModel.isForce());
        buildLogger.addBuildLogEntry(prefix + "First agent fallback: " + svDeployModel.isFirstSuitableAgentFallback());
    }
}
