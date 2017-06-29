package com.hpe.bamboo.plugin.loadrunner.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.hpe.utils.loadrunner.LRConsts;
import com.hpe.utils.loadrunner.LoadRunnerExecutor;

import java.io.*;
import java.util.Properties;

/**
 * Created by habash on 15/05/2017.
 */
public class LoadRunnerTestTask implements TaskType {

    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        String workingDirectory = taskContext.getWorkingDirectory().getAbsolutePath();

        String tests = taskContext.getConfigurationMap().get(LRConsts.TESTS);
        String timeout = "".equals(taskContext.getConfigurationMap().get(LRConsts.TIMEOUT)) ?
                LRConsts.DEFAULT_TIMEOUT :
                taskContext.getConfigurationMap().get(LRConsts.TIMEOUT);
        String pollingInterval = "".equals(taskContext.getConfigurationMap().get(LRConsts.POLLING_INTERVAL)) ?
                LRConsts.DEFAULT_POLLING_INTERVAL :
                taskContext.getConfigurationMap().get(LRConsts.POLLING_INTERVAL);
        String execTimeout = "".equals(taskContext.getConfigurationMap().get(LRConsts.EXEC_TIMEOUT)) ?
                LRConsts.DEFAULT_EXEC_TIMEOUT :
                taskContext.getConfigurationMap().get(LRConsts.EXEC_TIMEOUT);
        String ignoreErrors = taskContext.getConfigurationMap().get(LRConsts.IGNORE_ERRORS);

        try {
            buildLogger.addBuildLogEntry("************ Load Runner " +
                    "Test User Input ************");
            buildLogger.addBuildLogEntry(LRConsts.LABEL_TESTS + ": " + tests);
            Thread.sleep(5000);
            buildLogger.addBuildLogEntry(LRConsts.LABEL_TIMEOUT + ": " + timeout);
            Thread.sleep(5000);
            buildLogger.addBuildLogEntry(LRConsts.LABEL_POLLING_INTERVAL + ": " + pollingInterval);
            Thread.sleep(5000);
            buildLogger.addBuildLogEntry(LRConsts.LABEL_EXEC_TIMEOUT + ": " + execTimeout);
            Thread.sleep(5000);
            buildLogger.addBuildLogEntry(LRConsts.LABEL_IGNORE_ERRORS + ": " + ignoreErrors);
            buildLogger.addBuildLogEntry("*****************************************************");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            LoadRunnerExecutor lre = new LoadRunnerExecutor(tests, timeout,
                    pollingInterval, execTimeout, ignoreErrors, workingDirectory);

            buildLogger.addBuildLogEntry("************ Copying the Load Runner executables to the agent ************");
            copyExecutable(workingDirectory, LRConsts.HP_TOOLS_LAUNCHER);
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_EXECUTABLE_COPIED, LRConsts.HP_TOOLS_LAUNCHER));
            copyExecutable(workingDirectory, LRConsts.HP_TOOLS_ABORTER);
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_EXECUTABLE_COPIED, LRConsts.HP_TOOLS_ABORTER));
            buildLogger.addBuildLogEntry("**************************************************************************");

            buildLogger.addBuildLogEntry("************ Creating the param file for the launcher ************");
            lre.execute();
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_PARAM_FILE_CREATION,
                    workingDirectory));
            buildLogger.addBuildLogEntry("******************************************************************");

        }
        catch(Exception e) {
            buildLogger.addBuildLogEntry(e.toString());
            return TaskResultBuilder.create(taskContext).failed().build();
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    private void copyExecutable(String workingDirectory, String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String error = "";
        try {
            String resourcePath = "/" + resourceName;
            stream = this.getClass().getResourceAsStream(resourcePath);

            if(stream == null) {
                throw new Exception(String.format(LRConsts.ERROR_RESOURCE_NOT_FOUND, resourcePath));
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            File resultPath = new File(workingDirectory, resourceName);
            resStreamOut = new FileOutputStream(resultPath);

            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }

        } catch (Exception e) {
            error = e.toString();
        }

        finally {
            if (stream != null) {
                stream.close();
                resStreamOut.close();
            }
            throw new Exception(error);
        }
    }


}
