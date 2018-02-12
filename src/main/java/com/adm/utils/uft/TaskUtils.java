package com.adm.utils.uft;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;

public final class TaskUtils {
    private TaskUtils() {
    }

    /**
     * Add the error message in the logs when an exception occurs
     *
     * @param exception
     * @param buildLogger
     * @param taskContext
     * @return TaskResult object
     */
    public static TaskResult logErrorMessage(final Exception exception, final BuildLogger buildLogger, final TaskContext taskContext) {
        buildLogger.addErrorLogEntry(exception.getMessage(), exception);
        return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
    }

    public static TaskResult logErrorMessage(final String errorMessage, final BuildLogger buildLogger, final TaskContext taskContext) {
        buildLogger.addErrorLogEntry(errorMessage);
        return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
    }

}
