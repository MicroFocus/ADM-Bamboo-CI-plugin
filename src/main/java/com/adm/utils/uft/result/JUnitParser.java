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

package com.adm.utils.uft.result;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.result.model.junit.*;
import com.adm.utils.uft.result.model.junit.Error;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JUnitParser {
    public Testsuites toModel(
            List<Map<String, String>> testInstanceRuns,
            String entityName,
            String runEntityId,
            String url,
            String domain,
            String project) {

        Map<String, Testsuite> testSetIdToTestsuite = getTestSets(testInstanceRuns);
        addTestcases(
                testInstanceRuns,
                testSetIdToTestsuite,
                entityName,
                runEntityId,
                url,
                domain,
                project);

        return createTestsuites(testSetIdToTestsuite);
    }

    private Testsuites createTestsuites(Map<String, Testsuite> testSetIdToTestsuite) {

        Testsuites ret = new Testsuites();
        List<Testsuite> testsuites = ret.getTestsuite();
        for (Testsuite currTestsuite : testSetIdToTestsuite.values()) {
            testsuites.add(currTestsuite);
        }

        return ret;
    }

    private void addTestcases(
            List<Map<String, String>> testInstanceRuns,
            Map<String, Testsuite> testSetIdToTestsuite,
            String bvsName,
            String runEntityId,
            String url,
            String domain,
            String project) {

        for (Map<String, String> currEntity : testInstanceRuns) {
            addTestcase(
                    testSetIdToTestsuite,
                    currEntity,
                    bvsName,
                    runEntityId,
                    url,
                    domain,
                    project);
        }
    }

    private void addTestcase(
            Map<String, Testsuite> testSetIdToTestsuite,
            Map<String, String> currEntity,
            String bvsName,
            String runEntityId,
            String url,
            String domain,
            String project) {

        testSetIdToTestsuite.get(getTestSetId(currEntity)).getTestcase().add(
                getTestcase(currEntity, bvsName, runEntityId, url, domain, project));
    }

    private Testcase getTestcase(
            Map<String, String> entity,
            String bvsName,
            String runEntityId,
            String url,
            String domain,
            String project) {

        Testcase ret = new Testcase();
        ret.setClassname(getTestSetName(entity, bvsName, runEntityId));
        ret.setName(getTestName(entity));
        ret.setTime(getTime(entity));
        new TestcaseStatusUpdater().update(ret, entity, url, domain, project);

        return ret;
    }

    private String getTestSetName(Map<String, String> entity, String bvsName, String runEntityId) {

        String ret = String.format("%s.(Unnamed test set)", bvsName);
        String testSetName = entity.get("testset-name");
        if (!StringUtils.isNullOrEmpty(testSetName)) {
            ret = String.format("%s (RunId:%s).%s", bvsName, runEntityId, testSetName);
        }

        return ret;
    }

    private String getTestInstanceRunId(Map<String, String> entity) {

        String ret = "No test instance run ID";
        String runId = entity.get("run-id");
        if (!StringUtils.isNullOrEmpty(runId)) {
            ret = String.format("Test instance run ID: %s", runId);
        }

        return ret;
    }

    private String getTestName(Map<String, String> entity) {

        String testName = entity.get("test-name");
        if (StringUtils.isNullOrEmpty(testName)) {
            testName = "Unnamed test";
        }

        return String.format("%s (%s)", testName, getTestInstanceRunId(entity));
    }

    private String getTime(Map<String, String> entity) {

        String ret = entity.get("duration");
        if (StringUtils.isNullOrEmpty(ret)) {
            ret = "0";
        }

        return ret;
    }

    private Map<String, Testsuite> getTestSets(List<Map<String, String>> testInstanceRuns) {

        Map<String, Testsuite> ret = new HashMap<String, Testsuite>();
        for (Map<String, String> currEntity : testInstanceRuns) {

            String testSetId = getTestSetId(currEntity);
            if (!ret.containsKey(testSetId)) {
                ret.put(testSetId, new Testsuite());
            }
        }

        return ret;
    }

    private String getTestSetId(Map<String, String> entity) {

        return entity.get("testcycl-id");
    }

    private static class TestcaseStatusUpdater {

        public void update(
                Testcase testcase,
                Map<String, String> entity,
                String url,
                String domain,
                String project) {

            String status = entity.get("status");
            testcase.setStatus(getJenkinsStatus(status));
            if (testcase.getStatus().equals(JUnitTestCaseStatus.ERROR)) {
                String errorMessage = status;
                if (errorMessage != null) {
                    Error error = new Error();
                    error.setMessage(String.format(
                            "Error: %s. %s",
                            errorMessage,
                            getTestInstanceRunLink(entity, url, domain, project)));
                    testcase.getError().add(error);
                }
            }
        }

        private String getTestInstanceRunLink(
                Map<String, String> entity,
                String url,
                String domain,
                String project) {

            String ret = StringUtils.EMPTY_STRING;
            String runId = entity.get("run-id");
            if (!StringUtils.isNullOrEmpty(runId)) {
                try {
                    ret =
                            String.format(
                                    "To see the test instance run in ALM, go to: td://%s.%s.%s:8080/qcbin/[TestRuns]?EntityLogicalName=run&EntityID=%s",
                                    project,
                                    domain,
                                    new URL(url).getHost(),
                                    runId);
                } catch (MalformedURLException ex) {
                    throw new SSEException(ex);
                }
            }

            return ret;
        }

        private String getJenkinsStatus(String status) {

            return (!StringUtils.isNullOrEmpty(status) && ("Passed".equals(status) || "Warning".equals(status)))
                    ? JUnitTestCaseStatus.PASS
                    : JUnitTestCaseStatus.ERROR;
        }
    }
}
