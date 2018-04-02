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
import com.adm.bamboo.plugin.sv.model.SvExportModel;
import com.adm.bamboo.plugin.sv.model.SvServerSettingsModel;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.hp.sv.jsvconfigurator.build.ProjectBuilder;
import com.hp.sv.jsvconfigurator.core.IProject;
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
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class SVExportTask implements TaskType {
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
        boolean archive = BooleanUtils.toBoolean(map.get(SVConstants.ARCHIVE));
        boolean cleanTargetDirectory = BooleanUtils.toBoolean(map.get(SVConstants.CLEAN_TARGET_DIRECTORY));
        boolean switchServiceToStandBy = BooleanUtils.toBoolean(map.get(SVConstants.SWITCH_SERVICE_TO_STANDBY));
        String serviceSelection = map.get(SVConstants.SERVICE_SELECTION);
        String targetDirectory = map.get(SVConstants.TARGET_DIRECTORY);

        SvServerSettingsModel svServerSettingsModel = new SvServerSettingsModel(serverURL, userName, userPassword);
        SvServiceSelectionModel svServiceSelectionModel = new SvServiceSelectionModel(serviceName, projectPath, projectPassword);
        svServiceSelectionModel.setSelectionType(serviceSelection);
        SvExportModel svExportModel = new SvExportModel(svServerSettingsModel, svServiceSelectionModel, targetDirectory, cleanTargetDirectory, switchServiceToStandBy, force, archive);
        logConfig(buildLogger, svExportModel, startDate, SVConstants.PREFIX);

        ExportProcessor exportProcessor = new ExportProcessor(null);
        IChmodeProcessor chmodeProcessor = new ChmodeProcessor(null);

        ICommandExecutor commandExecutor = null;
        IProject project = null;
        try {
            commandExecutor = SVExecutorUtil.createCommandExecutor(new URL(svServerSettingsModel.getUrl()),
                    new Credentials(svServerSettingsModel.getUsername(),svServerSettingsModel.getPassword()));
            if (svExportModel.isCleanTargetDirectory()) {
                cleanTargetDirectory(buildLogger, targetDirectory);
            }
            if (svServiceSelectionModel.getSelectionType().equals(SvServiceSelectionModel.SelectionType.PROJECT)) {
                project = new ProjectBuilder().buildProject(new File(projectPath), projectPassword);
            }
            List<ServiceInfo> serviceInfoList = SVExecutorUtil.getServiceList(commandExecutor, svServiceSelectionModel, false, buildLogger);
            for (ServiceInfo serviceInfo : serviceInfoList) {
                if (svExportModel.isSwitchToStandByFirst()) {
                    switchToStandBy(svExportModel, serviceInfo, chmodeProcessor, commandExecutor, buildLogger);
                }

                buildLogger.addBuildLogEntry(String.format("  Exporting service '%s' [%s] to %s %n", serviceInfo.getName(), serviceInfo.getId(), targetDirectory));
                verifyNotLearningBeforeExport(buildLogger, commandExecutor, serviceInfo);
                exportProcessor.process(commandExecutor, targetDirectory, serviceInfo.getId(), project, false, svExportModel.isArchive());
            }
        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Build failed: " + e.getMessage(), e);
            return TaskResultBuilder.create(taskContext).failedWithError().build();
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            buildLogger.addBuildLogEntry(String.format("Finished: Export Virtual Service in %.3f seconds%n%n", duration));
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    /**
     * check the virtual service learning mode before export
     *
     * @param buildLogger
     * @param exec
     * @param serviceInfo
     * @return
     */
    private void verifyNotLearningBeforeExport(BuildLogger buildLogger, ICommandExecutor exec, ServiceInfo serviceInfo)
            throws CommunicatorException, CommandExecutorException {

        IService service = exec.findService(serviceInfo.getId(), null);
        ServiceRuntimeConfiguration info = exec.getServiceRuntimeInfo(service);
        if (info.getRuntimeMode() == ServiceRuntimeConfiguration.RuntimeMode.LEARNING) {
            buildLogger.addBuildLogEntry(String.format("    WARNING: Service '%s' [%s] is in Learning mode. Exported model need not be complete!",
                    serviceInfo.getName(), serviceInfo.getId()));
        }
    }

    /**
     * switch the virtual service to STAND_BY mode
     *
     * @param svExportModel
     * @param service
     * @param chmodeProcessor
     * @param exec
     * @param buildLogger
     * @return
     */
    private void switchToStandBy(SvExportModel svExportModel, ServiceInfo service, IChmodeProcessor chmodeProcessor, ICommandExecutor exec, BuildLogger buildLogger)
            throws CommandExecutorException, SVCParseException, CommunicatorException {

        buildLogger.addBuildLogEntry(String.format("  Switching service '%s' [%s] to Stand-By mode before export%n", service.getName(), service.getId()));
        ChmodeProcessorInput chmodeInput = new ChmodeProcessorInput(svExportModel.isForce(), null, service.getId(), null, null,
                ServiceRuntimeConfiguration.RuntimeMode.STAND_BY, false, false);
        chmodeProcessor.process(chmodeInput, exec);
    }

    /**
     * Cleans all sub-folders containing *.vproj file.
     *
     * @param buildLogger
     * @param targetDirectory
     * @return
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
        SVExecutorUtil.logConfig(svExportModel.getServiceSelectionModel(), buildLogger, prefix);
        buildLogger.addBuildLogEntry(prefix + "Target Directory: " + svExportModel.getTargetDirectory());
        buildLogger.addBuildLogEntry(prefix + "Clean Target Directory: " + svExportModel.isCleanTargetDirectory());
        buildLogger.addBuildLogEntry(prefix + "Switch to Stand-By: " + svExportModel.isSwitchToStandByFirst());
        buildLogger.addBuildLogEntry(prefix + "Force: " + svExportModel.isForce());
        buildLogger.addBuildLogEntry(prefix + "Archive: " + svExportModel.isArchive());
    }
}
