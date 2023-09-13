/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

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
            logger.addBuildLogEntry(ex.getMessage());
            logger.addBuildLogEntry("Failed to parse test results file (" + results.getName() + ").");
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

