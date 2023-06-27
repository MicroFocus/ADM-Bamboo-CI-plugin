/*
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors (“OpenText”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using HpToolsLauncher.Properties;
using HpToolsLauncher.TestRunners;

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
        private TimeSpan _timeout = TimeSpan.MaxValue;
        private Stopwatch _stopwatch = null;
        private string _abortFilename = System.IO.Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\stop" + Launcher.UniqueTimeStamp + ".txt";

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
            Dictionary<string, string> jenkinsEnvVariables,
            string fsAppParamName,
            string appIdentifier,
            bool useUFTLicense = false
            )
        {
            _jenkinsEnvVariables = jenkinsEnvVariables;
            //search if we have any testing tools installed
            if (!Helper.IsTestingToolsInstalled(TestStorageType.FileSystem))
            {
                ConsoleWriter.WriteErrLine(string.Format(Resources.FileSystemTestsRunner_No_HP_testing_tool_is_installed_on, System.Environment.MachineName));
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            _timeout = timeout;
            _stopwatch = Stopwatch.StartNew();

            _pollingInterval = ControllerPollingInterval;
            _perScenarioTimeOut = perScenarioTimeOut;
            _ignoreErrorStrings = ignoreErrorStrings;


            _useUFTLicense = useUFTLicense;
            _tests = new List<TestInfo>();

            //go over all sources, and create a list of all tests
            foreach (string source in sources)
            {
                List<TestInfo> testGroup = new List<TestInfo>();
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
                        FileInfo fi = new FileInfo(source);
                        if (fi.Extension == Helper.LoadRunnerFileExtention)
                            testGroup.Add(new TestInfo(source, source, source));
                        else if (fi.Extension == ".mtb")
                        //if (source.TrimEnd().EndsWith(".mtb", StringComparison.CurrentCultureIgnoreCase))
                        {
                            MtbManager manager = new MtbManager();

                            var paths = manager.Parse(source);
                     
                            foreach (var p in paths)
                            {
                                testGroup.Add(new TestInfo(p, p, source));
                            }
                        } else if (fi.Extension == ".mtbx")
                            //if (source.TrimEnd().EndsWith(".mtb", StringComparison.CurrentCultureIgnoreCase))
                            {
                                testGroup = MtbxManager.Parse(source, _jenkinsEnvVariables, source);

                            // set the test Id for each test from the group
                            // this is important for parallel runner
                            
                            //testGroup = MtbxManager.Parse(source, _jenkinsEnvVariables, source);
                           // if (!string.IsNullOrEmpty(fsAppParamName) && !string.IsNullOrEmpty(appIdentifier))
                          //  {
                              //  var testParam = new TestParameterInfo() { Name = fsAppParamName, Type = "string", Value = appIdentifier };
                                //foreach(TestInfo testInfo in testGroup)
                                //{
                                //testInfo.ParameterList.Add(testParam);
                                //testInfo.TestId = source.Id;
                                //}
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

            if (_tests == null || _tests.Count == 0)
            {
                ConsoleWriter.WriteLine(Resources.FsRunnerNoValidTests);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestsFound, _tests.Count));
            _tests.ForEach(t => ConsoleWriter.WriteLine("" + t.TestName));
            ConsoleWriter.WriteLine(Resources.GeneralDoubleSeperator);
        }




        /// <summary>
        /// runs all tests given to this runner and returns a suite of run resutls
        /// </summary>
        /// <returns>The rest run results for each test</returns>
        public override TestSuiteRunResults Run()
        {
            //create a new Run Results object
            TestSuiteRunResults activeRunDesc = new TestSuiteRunResults();

            double totalTime = 0;
            try
            {
                var start = DateTime.Now;
                foreach (var test in _tests)
                {
                    if (RunCancelled()) break;

                    var testStart = DateTime.Now;

                    string errorReason = string.Empty;
                    TestRunResults runResult = null;
                    try
                    {
                        runResult = RunHPToolsTest(test, ref errorReason);
                    }
                    catch (Exception ex)
                    {
                        runResult = new TestRunResults();
                        runResult.TestState = TestState.Error;
                        runResult.ErrorDesc = ex.Message;
                        runResult.TestName = test.TestName;
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
                                    runResult.ErrorDesc = HpToolsLauncher.Properties.Resources.ExceptionUserCancelled;
                                }
                                else
                                {
                                    runResult.ErrorDesc = HpToolsLauncher.Properties.Resources.ExceptionExternalProcess;
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
        /// checks if timeout has expired
        /// </summary>
        /// <returns></returns>
        private bool CheckTimeout()
        {
            TimeSpan timeleft = _timeout - _stopwatch.Elapsed;
            return (timeleft > TimeSpan.Zero);
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
                    runner = new ApiTestRunner(this, _timeout - _stopwatch.Elapsed);
                    break;
                case TestType.QTP:
                    runner = new GuiTestRunner(this, _useUFTLicense, _timeout - _stopwatch.Elapsed);
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
            if (System.IO.File.Exists(_abortFilename))
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
