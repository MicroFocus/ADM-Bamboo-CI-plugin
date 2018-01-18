package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.SvDeployModel;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.utils.sv.SVConsts;
import com.adm.utils.sv.SVExecutor;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.hp.sv.jsvconfigurator.build.ProjectBuilder;
import com.hp.sv.jsvconfigurator.core.IDataModel;
import com.hp.sv.jsvconfigurator.core.IPerfModel;
import com.hp.sv.jsvconfigurator.core.IProject;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.exception.ProjectBuilderException;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.processor.DeployProcessor;
import com.hp.sv.jsvconfigurator.processor.DeployProcessorInput;
import com.hp.sv.jsvconfigurator.processor.IDeployProcessor;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import com.hp.sv.jsvconfigurator.service.ServiceAmendingServiceImpl;
import com.hp.sv.jsvconfigurator.util.ProjectUtils;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVDeployTask implements TaskType {
    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) {
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
        boolean firstSuitableAgentFallback = BooleanUtils.toBoolean(map.get(SVConsts.FIRST_SUITABLE_AGENT_FALLBACK));

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvDeployModel svDeployModel = new SvDeployModel(svServerSettingsModel, serviceName, force, firstSuitableAgentFallback);

        logConfig(buildLogger, svDeployModel, startDate, "    ");
        try {
            IProject project = new ProjectBuilder().buildProject(new File(projectPath), projectPassword);
            printProjectContent(project, buildLogger);
            deployServiceFromProject(svDeployModel, project, buildLogger);
        } catch (ProjectBuilderException e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
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

    private void deployServiceFromProject(SvDeployModel svDeployModel, IProject project, BuildLogger buildLogger) throws Exception {
        SVExecutor svExecutor = new SVExecutor();
        IDeployProcessor processor = new DeployProcessor(null, new ServiceAmendingServiceImpl());
        ICommandExecutor commandExecutor = svExecutor.createCommandExecutor(new URL(svDeployModel.getServerSettingsModel().getUrl()),
                new Credentials(svDeployModel.getServerSettingsModel().getUsername(),svDeployModel.getServerSettingsModel().getPassword()));

        for (IService service : getServiceList(svDeployModel, project)) {
            buildLogger.addBuildLogEntry(String.format("  Deploying service '%s' [%s] %n", service.getName(), service.getId()));
            DeployProcessorInput deployInput = new DeployProcessorInput(svDeployModel.isForce(), false, project, svDeployModel.getServiceName(), null);
            deployInput.setFirstAgentFailover(svDeployModel.isFirstSuitableAgentFallback());
            processor.process(deployInput, commandExecutor);
        }
    }

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
