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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using HpToolsLauncher.Properties;
using HpToolsLauncher.TestRunners;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class FileSystemTestsRunner : RunnerBase, IDisposable
    {
        #region Members

        Dictionary<string, string> _jenkinsEnvVariables;
        private List<TestInfo> _tests;
        private static string _uftViewerPath;
        private int _errors, _fail;
        private bool _useUFTLicense;
        private McConnectionInfo _mcConnection;
        private string _mobileInfo;
        private RunAsUser _uftRunAsUser;
        private TimeSpan _timeout = TimeSpan.MaxValue;
        private Stopwatch _stopwatch = null;
        private string _abortFilename = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\stop" + Launcher.UniqueTimeStamp + ".txt";

        //LoadRunner Arguments
        private int _pollingInterval;
        private TimeSpan _perScenarioTimeOut;
        private List<string> _ignoreErrorStrings;


        //saves runners for cleaning up at the end.
        private Dictionary<TestType, IFileSysTestRunner> _colRunnersForCleanup = new Dictionary<TestType, IFileSysTestRunner>();

        public const string UftJUnitRportName = "uftRunnerRoot";

        #endregion

        /// <summary>
        /// creates instance of the runner given a source.
        /// </summary>
        /// <param name="sources"></param>
        /// <param name="timeout"></param>
        /// <param name="backgroundWorker"></param>
        /// <param name="useUFTLicense"></param>
        public FileSystemTestsRunner(List<string> sources,
            TimeSpan timeout,
            int ControllerPollingInterval,
            TimeSpan perScenarioTimeOut,
            List<string> ignoreErrorStrings,
            McConnectionInfo mcConnection,
            string mobileInfo,
            Dictionary<string, string> bambooEnvVariables,
            RunAsUser uftRunAsUser,
            bool useUFTLicense = false
            )
        {
            _jenkinsEnvVariables = bambooEnvVariables;
            //search if we have any testing tools installed
            if (!Helper.IsTestingToolsInstalled(TestStorageType.FileSystem))
            {
                ConsoleWriter.WriteErrLine(string.Format(Resources.FileSystemTestsRunner_No_HP_testing_tool_is_installed_on, Environment.MachineName));
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            _timeout = timeout;
            _stopwatch = Stopwatch.StartNew();

            _pollingInterval = ControllerPollingInterval;
            _perScenarioTimeOut = perScenarioTimeOut;
            _ignoreErrorStrings = ignoreErrorStrings;

            _mcConnection = mcConnection;
            _mobileInfo = mobileInfo;

            _uftRunAsUser = uftRunAsUser;
            _useUFTLicense = useUFTLicense;
            _tests = new List<TestInfo>();

            //go over all sources, and create a list of all tests
            foreach (string source in sources)
            {
                List<TestInfo> testGroup = new();
                try
                {
                    //--handle directories which contain test subdirectories (recursively)
                    if (Helper.IsDirectory(source))
                    {
                        var testsLocations = Helper.GetTestsLocations(source);
                        foreach (var loc in testsLocations)
                        {
                            var testName = loc.Substring(loc.LastIndexOf("\\") + 1);
                            var test = new TestInfo(loc, testName, source);
                            testGroup.Add(test);
                        }
                    }
                    //--handle mtb files (which contain links to tests)
                    else
                    //file might be LoadRunner scenario or
                    //mtb file (which contain links to tests)
                    //other files are dropped
                    {
                        testGroup = new List<TestInfo>();
                        FileInfo fi = new(source);
                        if (fi.Extension == Helper.LR_FILE_EXT)
                            testGroup.Add(new TestInfo(source, source, source));
                        else if (fi.Extension == ".mtb")
                        {
                            MtbManager manager = new();

                            var paths = manager.Parse(source);

                            foreach (var p in paths)
                            {
                                testGroup.Add(new TestInfo(p, p, source));
                            }
                        }
                        else if (fi.Extension == ".mtbx")
                        {
                            testGroup = MtbxManager.Parse(source, _jenkinsEnvVariables, source);
                        }
                    }
                }
                catch (Exception)
                {
                    testGroup = new List<TestInfo>();
                }

                //--handle single test dir, add it with no group
                if (testGroup.Count == 1)
                {
                    testGroup[0].TestGroup = "<None>";
                }

                _tests.AddRange(testGroup);
            }

            if (_tests.IsNullOrEmpty())
            {
                ConsoleWriter.WriteLine(Resources.FsRunnerNoValidTests);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            if (_mcConnection != null)
                ConsoleWriter.WriteLine("Digital Lab connection info is - " + _mcConnection.ToString());

            ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestsFound, _tests.Count));
            _tests.ForEach(t => ConsoleWriter.WriteLine(t.TestName));
            ConsoleWriter.WriteLine(Resources.GeneralDoubleSeperator);
        }

        /// <summary>
        /// runs all tests given to this runner and returns a suite of run resutls
        /// </summary>
        /// <returns>The rest run results for each test</returns>
        public override TestSuiteRunResults Run()
        {
            //create a new Run Results object
            TestSuiteRunResults activeRunDesc = new();

            double totalTime = 0;
            try
            {
                var start = DateTime.Now;
                Exception dcomEx = null;
                bool isDcomVerified = false;
                foreach (var test in _tests)
                {
                    if (RunCancelled()) break;

                    var testStart = DateTime.Now;
                    string errorReason = string.Empty;
                    TestRunResults runResult = null;
                    try
                    {
                        var type = Helper.GetTestType(test.TestPath);

                        if (type == TestType.QTP)
                        {
                            if (!isDcomVerified)
                            {
                                try
                                {
                                    Helper.ChangeDCOMSettingToInteractiveUser();
                                }
                                catch (Exception ex)
                                {
                                    dcomEx = ex;
                                }
                                finally
                                {
                                    isDcomVerified = true;
                                }
                            }

                            if (dcomEx != null)
                                throw dcomEx;
                        }

                        runResult = RunHPToolsTest(test, ref errorReason);
                    }
                    catch (Exception ex)
                    {
                        runResult = new TestRunResults
                        {
                            TestState = TestState.Error,
                            ErrorDesc = ex.Message,
                            TestName = test.TestName
                        };
                    }

                    //get the original source for this test, for grouping tests under test classes
                    runResult.TestGroup = test.TestGroup;

                    activeRunDesc.TestRuns.Add(runResult);

                    //if fail was terminated before this step, continue
                    if (runResult.TestState != TestState.Failed)
                    {
                        if (runResult.TestState != TestState.Error)
                        {
                            Helper.GetTestStateFromReport(runResult);
                        }
                        else
                        {
                            if (string.IsNullOrEmpty(runResult.ErrorDesc))
                            {
                                if (RunCancelled())
                                {
                                    runResult.ErrorDesc = Resources.ExceptionUserCancelled;
                                }
                                else
                                {
                                    runResult.ErrorDesc = Resources.ExceptionExternalProcess;
                                }
                            }
                            runResult.ReportLocation = null;
                            runResult.TestState = TestState.Error;
                        }
                    }

                    if (runResult.TestState == TestState.Passed && runResult.HasWarnings)
                    {
                        runResult.TestState = TestState.Warning;
                        ConsoleWriter.WriteLine(Resources.FsRunnerTestDoneWarnings);
                    }
                    else
                    {
                        ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestDone, runResult.TestState));
                    }

                    ConsoleWriter.WriteLine(DateTime.Now.ToString(Launcher.DateFormat) + " Test complete: " + runResult.TestPath + "\n-------------------------------------------------------------------------------------------------------");

                    UpdateCounters(runResult.TestState);
                    var testTotalTime = (DateTime.Now - testStart).TotalSeconds;
                }
                totalTime = (DateTime.Now - start).TotalSeconds;
            }
            finally
            {
                activeRunDesc.NumTests = _tests.Count;
                activeRunDesc.NumErrors = _errors;
                activeRunDesc.TotalRunTime = TimeSpan.FromSeconds(totalTime);
                activeRunDesc.NumFailures = _fail;

                foreach (IFileSysTestRunner cleanupRunner in _colRunnersForCleanup.Values)
                {
                    cleanupRunner.CleanUp();
                }
            }

            return activeRunDesc;
        }

        /// <summary>
        /// creates a correct type of runner and runs a single test.
        /// </summary>
        /// <param name="testPath"></param>
        /// <param name="errorReason"></param>
        /// <returns></returns>
        private TestRunResults RunHPToolsTest(TestInfo testinf, ref string errorReason)
        {
            var testPath = testinf.TestPath;
            var type = Helper.GetTestType(testPath);
            IFileSysTestRunner runner = null;
            switch (type)
            {
                case TestType.ST:
                    runner = new ApiTestRunner(this, _timeout - _stopwatch.Elapsed, _uftRunAsUser);
                    break;
                case TestType.QTP:
                    runner = new GuiTestRunner(this, _useUFTLicense, _timeout - _stopwatch.Elapsed, _mcConnection, _mobileInfo, _uftRunAsUser);
                    break;
                case TestType.LoadRunner:
                    AppDomain.CurrentDomain.AssemblyResolve += Helper.HPToolsAssemblyResolver;
                    runner = new PerformanceTestRunner(this, _timeout, _pollingInterval, _perScenarioTimeOut, _ignoreErrorStrings);
                    break;
            }

            if (runner != null)
            {
                if (!_colRunnersForCleanup.ContainsKey(type))
                    _colRunnersForCleanup.Add(type, runner);

                Stopwatch s = Stopwatch.StartNew();

                TestRunResults results = null;

                results = runner.RunTest(testinf, ref errorReason, RunCancelled);

                results.Runtime = s.Elapsed;
                if (type == TestType.LoadRunner)
                    AppDomain.CurrentDomain.AssemblyResolve -= Helper.HPToolsAssemblyResolver;

                results.TestName = testinf.TestName;

                return results;
            }

            //check for abortion
            if (File.Exists(_abortFilename))
            {
                ConsoleWriter.WriteLine(Resources.GeneralStopAborted);

                //stop working 
                Environment.Exit((int)Launcher.ExitCodeEnum.Aborted);
            }

            return new TestRunResults { ErrorDesc = "Unknown TestType", TestState = TestState.Error };
        }


        /// <summary>
        /// checks if run was cancelled/aborted
        /// </summary>
        /// <returns></returns>
        public bool RunCancelled()
        {
            //if timeout has passed
            if (_stopwatch.Elapsed > _timeout)
            {

                if (!_blnRunCancelled)
                {
                    ConsoleWriter.WriteLine(Resources.GeneralTimedOut);

                    Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                    _blnRunCancelled = true;
                }
            }

            //if (System.IO.File.Exists(_abortFilename))
            //{
            //    if (!_blnRunCancelled)
            //    {
            //        ConsoleWriter.WriteLine(Resources.GeneralAbortedByUser);
            //        Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
            //        _blnRunCancelled = true;
            //    }
            //}
            return _blnRunCancelled;
        }

        /// <summary>
        /// sums errors and failed tests
        /// </summary>
        /// <param name="testState"></param>
        private void UpdateCounters(TestState testState)
        {
            switch (testState)
            {
                case TestState.Error:
                    _errors += 1;
                    break;
                case TestState.Failed:
                    _fail += 1;
                    break;
            }
        }

        /// <summary>
        /// Opens the report viewer for the given report directory
        /// </summary>
        /// <param name="reportDirectory"></param>
        public static void OpenReport(string reportDirectory)
        {
            Helper.OpenReport(reportDirectory, ref _uftViewerPath);
        }
    }
}
