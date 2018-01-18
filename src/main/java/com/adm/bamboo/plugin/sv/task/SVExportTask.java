package com.adm.bamboo.plugin.sv.task;

import com.adm.bamboo.plugin.sv.model.ServiceInfo;
import com.adm.bamboo.plugin.sv.model.SvExportModel;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.adm.utils.sv.SVConsts;
import com.adm.utils.sv.SVExecutor;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommandExecutorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommunicatorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.SVCParseException;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.ServiceRuntimeConfiguration;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.processor.ChmodeProcessor;
import com.hp.sv.jsvconfigurator.processor.ChmodeProcessorInput;
import com.hp.sv.jsvconfigurator.processor.ExportProcessor;
import com.hp.sv.jsvconfigurator.processor.IChmodeProcessor;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVExportTask implements TaskType {
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
        boolean cleanTargetDirectory = BooleanUtils.toBoolean(map.get(SVConsts.CLEAN_TARGET_DIRECTORY));
        boolean switchServiceToStandBy = BooleanUtils.toBoolean(map.get(SVConsts.SWITCH_SERVICE_TO_STANDBY));
        String serviceSelection = map.get(SVConsts.SERVICE_SELECTION);
        String targetDirectory = map.get(SVConsts.TARGET_DIRECTORY);

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);
        SvExportModel svExportModel = new SvExportModel(svServerSettingsModel, svServiceSelectionModel, targetDirectory, cleanTargetDirectory, switchServiceToStandBy, force);
        logConfig(buildLogger, svExportModel, startDate, "    ");

        ExportProcessor exportProcessor = new ExportProcessor(null);
        IChmodeProcessor chmodeProcessor = new ChmodeProcessor(null);

        ICommandExecutor commandExecutor = null;
        try {
            commandExecutor = svExecutor.createCommandExecutor(new URL(svServerSettingsModel.getUrl()),
                    new Credentials(svServerSettingsModel.getUsername(),svServerSettingsModel.getPassword()));
            if (svExportModel.isCleanTargetDirectory()) {
                cleanTargetDirectory(buildLogger, targetDirectory);
            }
            List<ServiceInfo> serviceInfoList = svExecutor.getServiceList(commandExecutor, svServiceSelectionModel, false, buildLogger);
            for (ServiceInfo serviceInfo : serviceInfoList) {
                if (svExportModel.isSwitchToStandByFirst()) {
                    switchToStandBy(svExportModel, serviceInfo, chmodeProcessor, commandExecutor, buildLogger);
                }

                buildLogger.addBuildLogEntry(String.format("  Exporting service '%s' [%s] to %s %n", serviceInfo.getName(), serviceInfo.getId(), targetDirectory));
                verifyNotLearningBeforeExport(buildLogger, commandExecutor, serviceInfo);
                exportProcessor.process(commandExecutor, targetDirectory, serviceInfo.getId(), false);
            }
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage());
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: Export Virtual Service in %.3f seconds%n%n", duration));
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    private void verifyNotLearningBeforeExport(BuildLogger buildLogger, ICommandExecutor exec, ServiceInfo serviceInfo)
            throws CommunicatorException, CommandExecutorException {

        IService service = exec.findService(serviceInfo.getId(), null);
        ServiceRuntimeConfiguration info = exec.getServiceRuntimeInfo(service);
        if (info.getRuntimeMode() == ServiceRuntimeConfiguration.RuntimeMode.LEARNING) {
            buildLogger.addBuildLogEntry(String.format("    WARNING: Service '%s' [%s] is in Learning mode. Exported model need not be complete!",
                    serviceInfo.getName(), serviceInfo.getId()));
        }
    }

    private void switchToStandBy(SvExportModel svExportModel, ServiceInfo service, IChmodeProcessor chmodeProcessor, ICommandExecutor exec, BuildLogger buildLogger)
            throws CommandExecutorException, SVCParseException, CommunicatorException {

        buildLogger.addBuildLogEntry(String.format("  Switching service '%s' [%s] to Stand-By mode before export%n", service.getName(), service.getId()));
        ChmodeProcessorInput chmodeInput = new ChmodeProcessorInput(svExportModel.isForce(), null, service.getId(), null, null,
                ServiceRuntimeConfiguration.RuntimeMode.STAND_BY, false, false);
        chmodeProcessor.process(chmodeInput, exec);
    }

    /**
     * Cleans all sub-folders containing *.vproj file.
     */
    private void cleanTargetDirectory(BuildLogger buildLogger, String targetDirectory) throws IOException {
        File target = new File(targetDirectory);
        if (target.exists()) {
            File[] subfolders = target.listFiles((FilenameFilter) DirectoryFileFilter.INSTANCE);
            if (subfolders.length > 0) {
                buildLogger.addBuildLogEntry("  Cleaning target directory...");
            }
            for (File subfolder : subfolders) {
                if (subfolder.listFiles((FilenameFilter) new SuffixFileFilter(".vproj")).length > 0) {
                    buildLogger.addBuildLogEntry("    Deleting subfolder of target directory: " + subfolder.getAbsolutePath());
                    FileUtils.deleteDirectory(subfolder);
                } else {
                    buildLogger.addBuildLogEntry("    Skipping delete of directory '" + subfolder.getAbsolutePath() + "' because it does not contain any *.vproj file.");
                }
            }
        }
    }

    private void logConfig(BuildLogger buildLogger, SvExportModel svExportModel, Date startDate, String prefix) {
        buildLogger.addBuildLogEntry(String.format("%nStarting UnDeploy Virtual Service for SV Server '%s' as %s on %s%n",
                svExportModel.getServerSettingsModel().getUrl(), svExportModel.getServerSettingsModel().getUsername(), startDate));
        svExecutor.logConfig(svExportModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Target Directory: " + svExportModel.getTargetDirectory());
        buildLogger.addBuildLogEntry(prefix + "Clean Target Directory: " + svExportModel.isCleanTargetDirectory());
        buildLogger.addBuildLogEntry(prefix + "Switch to Stand-By: " + svExportModel.isSwitchToStandByFirst());
        buildLogger.addBuildLogEntry(prefix + "Force: " + svExportModel.isForce());
    }
}
