/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

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
