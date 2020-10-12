/*
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */
using System.IO;
using System.Xml.Serialization;

namespace HpToolsLauncher
{
    public class JunitXmlBuilder : IXmlBuilder
    {
        private string _xmlName = "APIResults.xml";

        public string XmlName
        {
            get { return _xmlName; }
            set { _xmlName = value; }
        }

        public const string ClassName = "MFToolsRunner";
        public const string RootName = "uftRunnerRoot";

        XmlSerializer _serializer = new XmlSerializer(typeof(testsuites));

        testsuites _testSuites = new testsuites();


        public JunitXmlBuilder()
        {
            _testSuites.name = RootName;
        }

        /// <summary>
        /// converts all data from the test resutls in to the Junit xml format and writes the xml file to disk.
        /// </summary>
        /// <param name="results"></param>
        public void CreateXmlFromRunResults(TestSuiteRunResults results)
        {
            _testSuites = new testsuites();

            testsuite ts = new testsuite
            {
                errors = results.NumErrors.ToString(),
                tests = results.NumTests.ToString(),
                failures = results.NumFailures.ToString(),
                name = results.SuiteName,
                package = ClassName
            };
            foreach (TestRunResults testRes in results.TestRuns)
            {
                testcase tc;
                if (testRes.TestType == TestType.LoadRunner.ToString())
                {
                    tc = CreateXmlFromLRRunResults(testRes);
                }
                else
                {
                    tc = CreateXmlFromUFTRunResults(testRes);
                }
                ts.AddTestCase(tc);
            }
            _testSuites.AddTestsuite(ts);

            if (File.Exists(XmlName))
                File.Delete(XmlName);

            using (Stream s = File.OpenWrite(XmlName))
            {
                _serializer.Serialize(s, _testSuites);
            }
        }

        private testcase CreateXmlFromLRRunResults(TestRunResults testRes)
        {
            return CreateXmlFromUFTRunResults(testRes);
            
        }

        private testcase CreateXmlFromUFTRunResults(TestRunResults testRes)
        {
            testcase tc = new testcase
            {
                systemout = testRes.ConsoleOut,
                systemerr = testRes.ConsoleErr,
                report = testRes.ReportLocation,
                classname = "All-Tests." + ((testRes.TestGroup == null) ? "" : testRes.TestGroup.Replace(".", "_")),
                name = testRes.TestPath,
                type = testRes.TestType,
                time = testRes.Runtime.TotalSeconds.ToString()
            };

            if (!string.IsNullOrWhiteSpace(testRes.FailureDesc))
                tc.AddFailure(new failure { message = testRes.FailureDesc });

            switch (testRes.TestState)
            {
                case TestState.Passed:
                    tc.status = "pass";
                    break;
                case TestState.Failed:
                    tc.status = "fail";
                    break;
                case TestState.Error:
                    tc.status = "error";
                    break;
                case TestState.Warning:
                    tc.status = "warning";
                    break;
                default:
                    tc.status = "pass";
                    break;
            }
            if (!string.IsNullOrWhiteSpace(testRes.ErrorDesc))
                tc.AddError(new error { message = testRes.ErrorDesc });
            return tc;
        }




    }
}
