package com.adm.bamboo.plugin.uft.results;

import com.adm.utils.uft.result.ResultSerializer;
import com.adm.utils.uft.result.model.junit.Testcase;
import com.adm.utils.uft.result.model.junit.Testsuite;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.adm.utils.uft.enums.ResultTypeFilter;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.adm.utils.uft.FilesHandler.getOutputFilePath;


public final class TestResultHelperFileSystem {

    /**
     * Retrieve tests results
     *
     * @param results                  the file containing the results of the test run
     * @param filter                   the results filter option(all tests or failed tests)
     * @param resultArtifactNameFormat the format of the results report(archive or html report)
     * @param taskContext              the task context
     * @param logger                   the logger object
     * @return tests results
     */
    public static Collection<ResultInfoItem> getTestResults(@NotNull final File results, final ResultTypeFilter filter,
                                                            @NotNull final String resultArtifactNameFormat,
                                                            @NotNull final TaskContext taskContext,
                                                            @NotNull final BuildLogger logger) {
        logger.addBuildLogEntry("getTestResults method: " + results + " abs path: " + results.getAbsolutePath());
        Collection<ResultInfoItem> resultItems = new ArrayList<>();
        Map<String, Integer> testNames = new HashMap<>();

        if (!results.exists()) {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") was not found.");
            return resultItems;
        }

        try {
            Testsuites testsuites = ResultSerializer.Deserialize(results);
            for (Testsuite testsuite : testsuites.getTestsuite()) {
                for (Testcase testcase : testsuite.getTestcase()) {
                    if (isInFilter(testcase, filter)) {
                        String testName = new File(testcase.getName()).getName();
                        if (!testNames.containsKey(testName)) {
                            testNames.put(testName, 0);
                        }
                        testNames.put(testName, testNames.get(testName) + 1);

                        StringBuilder fileNameSuffix = new StringBuilder();
                        Integer fileNameEntriesAmount = testNames.get(testName);
                        if (fileNameEntriesAmount > 1) {
                            fileNameSuffix.append("_").append(fileNameEntriesAmount);
                        }
                        File reportDir = new File(testcase.getReport());
                        File zipFileFolder = new File(getOutputFilePath(taskContext));
                        zipFileFolder.mkdirs();
                        File reportZipFile = new File(zipFileFolder, testName + fileNameSuffix + ".zip");
                        String resultArtifactName = String.format(resultArtifactNameFormat, testName);

                        ResultInfoItem resultItem = new ResultInfoItem(testName, reportDir, reportZipFile, resultArtifactName);
                        resultItems.add(resultItem);
                    }
                }
            }
        } catch (JAXBException ex) {
            logger.addBuildLogEntry("Test results file (" + results.getName() + ") has invalid format.");
        }

        return resultItems;
    }

    /**
     * Check the result type filter chosen for current test case
     *
     * @param testcase
     * @param filter
     * @return
     */
    private static boolean isInFilter(Testcase testcase, ResultTypeFilter filter) {
        if (filter == ResultTypeFilter.All) {
            return true;
        }
        String status = testcase.getStatus();
        if (filter == ResultTypeFilter.FAILED && status.equals(UFTConstants.TEST_STATUS_FAIL.getValue())) {
            return true;
        }

        return false;
    }
}

