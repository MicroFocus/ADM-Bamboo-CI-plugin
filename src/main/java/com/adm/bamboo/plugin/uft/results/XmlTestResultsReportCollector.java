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
