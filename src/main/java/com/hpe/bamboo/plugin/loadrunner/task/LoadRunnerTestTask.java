package com.hpe.bamboo.plugin.loadrunner.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;

/**
 * Created by habash on 15/05/2017.
 */
public class LoadRunnerTestTask implements TaskType {

    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        String tests = taskContext.getConfigurationMap().get("tests");
        int timeout = Integer.valueOf(taskContext.getConfigurationMap().get("timeout"));
        int pollingInterval = Integer.valueOf(taskContext.getConfigurationMap().get("pollingInterval"));
        int execTimeout = Integer.valueOf(taskContext.getConfigurationMap().get("execTimeout"));
        String ignoreErrors = taskContext.getConfigurationMap().get("ignoreErrors");

        buildLogger.addBuildLogEntry("Tests: " + tests);
        buildLogger.addBuildLogEntry("Timeout: " + timeout);
        buildLogger.addBuildLogEntry("Controller Polling Interval: " + pollingInterval);
        buildLogger.addBuildLogEntry("Scenario Execution Timeout: " + execTimeout);
        buildLogger.addBuildLogEntry("Errors to Ignore: " + ignoreErrors);

        return TaskResultBuilder.create(taskContext).success().build();
    }

}
