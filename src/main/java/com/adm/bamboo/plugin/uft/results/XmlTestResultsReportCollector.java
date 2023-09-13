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
import com.adm.utils.uft.result.model.junit.JUnitTestCaseStatus;
import com.adm.utils.uft.result.model.junit.Testcase;
import com.adm.utils.uft.result.model.junit.Testsuite;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.atlassian.bamboo.build.test.TestCollectionResult;
import com.atlassian.bamboo.build.test.TestCollectionResultBuilder;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.tests.TestState;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class XmlTestResultsReportCollector implements TestReportCollector {

    @NotNull
    @Override
    public TestCollectionResult collect(File file) throws Exception {
        TestCollectionResultBuilder builder = new TestCollectionResultBuilder();

        Collection<TestResults> successfulTestResults = new ArrayList<TestResults>();
        Collection<TestResults> failingTestResults = new ArrayList<TestResults>();

        Testsuites testsuites = ResultSerializer.Deserialize(file);

        for (Testsuite testsuite : testsuites.getTestsuite()) {
            for (Testcase testcase : testsuite.getTestcase()) {
                TestResults testResult = new TestResults(testcase.getClassname(), testcase.getName(), testcase.getTime());
                if (testcase.getStatus().equals(JUnitTestCaseStatus.PASS) || testcase.getStatus().equals(JUnitTestCaseStatus.WARNING)) {
                    testResult.setState(TestState.SUCCESS);
                    successfulTestResults.add(testResult);
                } else {
                    testResult.setState(TestState.FAILED);
                    failingTestResults.add(testResult);
                }
                testResult.setSystemOut("Test result: " + testcase.getStatus());
            }
        }

        return builder
                .addSuccessfulTestResults(successfulTestResults)
                .addFailedTestResults(failingTestResults)
                .build();
    }

    @NotNull
    @Override
    public Set<String> getSupportedFileExtensions() {
        return Sets.newHashSet("xml");
    }
}
