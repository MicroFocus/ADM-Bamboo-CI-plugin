/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
