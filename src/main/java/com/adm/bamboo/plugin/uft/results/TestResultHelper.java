package com.adm.bamboo.plugin.uft.results;

import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskContext;
import org.jetbrains.annotations.NotNull;

public final class TestResultHelper {
    private static final String TEST_REPORT_FILE_PATTERNS = "*.xml";

    private TestResultHelper(){}

    public static void collateTestResults(@NotNull final TestCollationService testCollationService, @NotNull final TaskContext taskContext){
        testCollationService.collateTestResults(taskContext, TEST_REPORT_FILE_PATTERNS, new XmlTestResultsReportCollector());
    }
}
