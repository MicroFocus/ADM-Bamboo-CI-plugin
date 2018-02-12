package com.adm.bamboo.plugin.uft.api;

import com.adm.utils.uft.FilesHandler;
import com.adm.utils.uft.TaskUtils;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskType;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public interface AbstractLauncherTask extends TaskType {

    Properties getTaskProperties(final TaskContext taskContext) throws Exception;

    TaskResult collateResults(@NotNull final TaskContext taskContext, final Properties mergedProperties);

    default TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
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

        //build props file
        File workingDirectory = taskContext.getWorkingDirectory();
        File paramsFile = FilesHandler.buildPropertiesFile(taskContext, workingDirectory, mergedProperties, buildLogger);

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
}



