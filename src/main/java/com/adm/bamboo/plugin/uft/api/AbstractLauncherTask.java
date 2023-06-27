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

package com.adm.bamboo.plugin.uft.api;

import com.adm.utils.uft.FilesHandler;
import com.adm.utils.uft.TaskUtils;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface AbstractLauncherTask extends TaskType {
    public static final String BUILD_KEY = "buildTimeStamp";

    Properties getTaskProperties(final TaskContext taskContext) throws Exception;

    TaskResult collateResults(@NotNull final TaskContext taskContext, final Properties mergedProperties);

    default TaskResult execute(@NotNull TaskContext taskContext,  CustomVariableContext customVariableContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        Properties mergedProperties = new Properties();
        try {
            Properties customTaskProperties = getTaskProperties(taskContext);
            if (customTaskProperties != null) {
                mergedProperties.putAll(customTaskProperties);
            }
        } catch (Exception e) {
            TaskUtils.logErrorMessage(e, buildLogger, taskContext);
        }

        //retrieve bamboo buildTimeStamp
        String buildTimeStamp = getBuildTimeStamp(customVariableContext);

        //build props file
        File workingDirectory = taskContext.getWorkingDirectory();
        File paramsFile = FilesHandler.buildPropertiesFile(taskContext, workingDirectory, mergedProperties, buildTimeStamp, buildLogger);

        //extract binary resources
        String launcherPath = null;
        String aborterPath = null;
        try {
            //copy HpToolsLauncher.exe
            launcherPath = FilesHandler.getToolPath(taskContext, workingDirectory, true);

            //copy HpToolsAborter.exe
            aborterPath = FilesHandler.getToolPath(taskContext, workingDirectory, false);
        } catch (IOException ioe) {
            TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
        }

        //run task
        try {
            Integer returnCode = runTask(workingDirectory, launcherPath, paramsFile.getAbsolutePath(), buildLogger);
            if (returnCode.equals(3)) {
                throw new InterruptedException();
            } else if (returnCode.equals(0)) {
                return collateResults(taskContext, mergedProperties);
            }
        } catch (IOException ioe) {
            TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
        } catch (InterruptedException e) {
            buildLogger.addErrorLogEntry("Aborted by user. Aborting process.");
            try {
                runTask(workingDirectory, aborterPath, paramsFile.getAbsolutePath(), buildLogger);
            } catch (IOException ioe) {
                TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
            } catch (InterruptedException ie) {
                TaskUtils.logErrorMessage(ie, buildLogger, taskContext);
            }
        }

        return collateResults(taskContext, mergedProperties);
    }

    default Integer runTask(final File workingDirectory, final String launcherPath, final String paramFile, final BuildLogger logger) throws IOException, InterruptedException {
        try {
            ProcessBuilder builder = new ProcessBuilder(launcherPath, "-paramfile", paramFile);
            builder.directory(workingDirectory);

            logger.addBuildLogEntry(launcherPath + " -paramfile " + paramFile);
            Process process = builder.start();
            BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = output.readLine()) != null) {
                logger.addBuildLogEntry(line);
            }
            output.close();
            return process.waitFor();
        } catch (Throwable t) {
            logger.addBuildLogEntry(t.getMessage());
            return -1;
        }
    }

    default String getBuildTimeStamp(CustomVariableContext customVariableContext){
        Map<String, VariableDefinitionContext> variables = customVariableContext.getVariableContexts();
        String buildTimeStamp = "";
        if(variables.containsKey(BUILD_KEY)) {
           buildTimeStamp = variables.get(BUILD_KEY).getValue();
        }

        return buildTimeStamp;
    }
}



