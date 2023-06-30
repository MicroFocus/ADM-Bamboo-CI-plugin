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
using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class ApiTestRunner : IFileSysTestRunner
    {
        public const string STRunnerName = "ServiceTestExecuter.exe";
        public const string STRunnerTestArg = @"-test";
        public const string STRunnerReportArg = @"-report";
        public const string STRunnerInputParamsArg = @"-inParams";
        private const int PollingTimeMs = 500;
        private bool _stCanRun;
        private string _stExecuterPath = Directory.GetCurrentDirectory();
        private readonly IAssetRunner _runner;
        private TimeSpan _timeout = TimeSpan.MaxValue;
        private Stopwatch _stopwatch = null;
        private RunCancelledDelegate _runCancelled;
        private RunAsUser _uftRunAsUser;

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="runner">parent runner</param>
        /// <param name="timeout">the global timout</param>
        /// <param name="uftRunAsUser"></param>
        public ApiTestRunner(IAssetRunner runner, TimeSpan timeout, RunAsUser uftRunAsUser)
        {
            _stopwatch = Stopwatch.StartNew();
            _timeout = timeout;
            _stCanRun = TrySetSTRunner();
            _runner = runner;
            _uftRunAsUser = uftRunAsUser;
        }

        /// <summary>
        /// Search ServiceTestExecuter.exe in the current running process directory,
        /// and if not found, in the installation folder (taken from registry)
        /// </summary>
        /// <returns></returns>
        public bool TrySetSTRunner()
        {
            if (File.Exists(STRunnerName))
                return true;
            _stExecuterPath = Helper.GetSTInstallPath();
            if (!_stExecuterPath.IsNullOrEmpty())
            {
                _stExecuterPath += "bin";
                return true;
            }
            _stCanRun = false;
            return false;
        }


        /// <summary>
        /// runs the given test
        /// </summary>
        /// <param name="testinf"></param>
        /// <param name="errorReason"></param>
        /// <param name="runCancelled">cancellation delegate, holds the function that checks cancellation</param>
        /// <returns></returns>
        public TestRunResults RunTest(TestInfo testinf, ref string errorReason, RunCancelledDelegate runCancelled)
        {

            TestRunResults runDesc = new TestRunResults();
            ConsoleWriter.ActiveTestRun = runDesc;
            ConsoleWriter.WriteLine(DateTime.Now.ToString(Launcher.DateFormat) + " Running: " + testinf.TestPath);

            runDesc.ReportLocation = testinf.TestPath;
            runDesc.ErrorDesc = errorReason;
            runDesc.TestPath = testinf.TestPath;
            runDesc.TestState = TestState.Unknown;
            if (!Helper.IsServiceTestInstalled())
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = string.Format(Resources.LauncherStNotInstalled, System.Environment.MachineName);
                ConsoleWriter.WriteErrLine(runDesc.ErrorDesc);
                Environment.ExitCode = (int)Launcher.ExitCodeEnum.Failed;
                return runDesc;
            }

            _runCancelled = runCancelled;
            if (!_stCanRun)
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = Resources.STExecuterNotFound;
                return runDesc;
            }
            string fileName = Path.Combine(_stExecuterPath, STRunnerName);

            if (!File.Exists(fileName))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = Resources.STExecuterNotFound;
                ConsoleWriter.WriteErrLine(Resources.STExecuterNotFound);
                return runDesc;
            }

            //write the input parameter xml file for the API test
            string paramFileName = Guid.NewGuid().ToString().Replace("-", string.Empty).Substring(0, 10);
            string tempPath = Path.Combine(Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location), "TestParams");
            Directory.CreateDirectory(tempPath);
            string paramsFilePath = Path.Combine(tempPath, "params" + paramFileName + ".xml");
            string paramFileContent = testinf.GenerateAPITestXmlForTest();

            string argumentString;
            if (!paramFileContent.IsNullOrWhiteSpace())
            {
                File.WriteAllText(paramsFilePath, paramFileContent);
                argumentString = $"{STRunnerTestArg} \"{testinf.TestPath}\" {STRunnerReportArg} \"{runDesc.ReportLocation}\" {STRunnerInputParamsArg} \"{paramsFilePath}\"";
            }
            else
            {
                argumentString = $"{STRunnerTestArg} \"{testinf.TestPath}\" {STRunnerReportArg} \"{runDesc.ReportLocation}\"";
            }

            Stopwatch s = Stopwatch.StartNew();
            runDesc.TestState = TestState.Running;

            if (!ExecuteProcess(fileName, argumentString, ref errorReason))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
            }
            else
            {
                runDesc.ReportLocation = Path.Combine(runDesc.ReportLocation, "Report");
                
                if (!File.Exists(Path.Combine(runDesc.ReportLocation, "Results.xml")) && !File.Exists(Path.Combine(runDesc.ReportLocation, "run_results.html")))
                {
                    runDesc.TestState = TestState.Error;
                    runDesc.ErrorDesc = "No Results.xml or run_results.html file found";
                }
            }

            runDesc.Runtime = s.Elapsed;
            return runDesc;
        }

        /// <summary>
        /// performs global cleanup code for this type of runner
        /// </summary>
        public void CleanUp()
        {
        }

        #region Process

        /// <summary>
        /// executes the run of the test by using the Init and RunProcss routines
        /// </summary>
        /// <param name="proc"></param>
        /// <param name="fileName"></param>
        /// <param name="arguments"></param>
        /// <param name="enableRedirection"></param>
        private bool ExecuteProcess(string fileName, string arguments, ref string failureReason)
        {
            Process proc = null;
            try
            {
                using (proc = new Process())
                {
                    InitProcess(proc, fileName, arguments);
                    RunProcess(proc, true);

                    //it could be that the process already existed
                    //before we could handle the cancel request
                    if (_runCancelled())
                    {
                        failureReason = "Process was stopped since job has timed out!";
                        ConsoleWriter.WriteLine(failureReason);

                        if (!proc.HasExited)
                        {
                            proc.OutputDataReceived -= OnOutputDataReceived;
                            proc.ErrorDataReceived -= OnErrorDataReceived;
                            proc.Kill();
                            return false;
                        }
                    }
                    if (proc.ExitCode != 0)
                    {
                        failureReason = "The Api test runner's exit code was: " + proc.ExitCode;
                        ConsoleWriter.WriteLine(failureReason);
                        return false;
                    }
                }
            }
            catch (Exception e)
            {
                failureReason = e.Message;
                return false;
            }
            finally
            {
                if (proc != null)
                {
                    proc.Close();
                }
            }

            return true;
        }

        /// <summary>
        /// initializes the ServiceTestExecuter process
        /// </summary>
        /// <param name="proc"></param>
        /// <param name="fileName"></param>
        /// <param name="arguments"></param>
        private void InitProcess(Process proc, string fileName, string arguments)
        {
            ProcessStartInfo psi = new()
            {
                FileName = fileName,
                Arguments = arguments,
                WorkingDirectory = Directory.GetCurrentDirectory(),
                ErrorDialog = false,
                UseShellExecute = false,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                CreateNoWindow = true,
            };

            if (_uftRunAsUser != null)
            {
                psi.UserName = _uftRunAsUser.Username;
                psi.Password = _uftRunAsUser.Password;
            }

            proc.StartInfo = psi;
            proc.EnableRaisingEvents = true;

            proc.OutputDataReceived += OnOutputDataReceived;
            proc.ErrorDataReceived += OnErrorDataReceived;
        }

        /// <summary>
        /// runs the ServiceTestExecuter process after initialization
        /// </summary>
        /// <param name="proc"></param>
        /// <param name="enableRedirection"></param>
        private void RunProcess(Process proc, bool enableRedirection)
        {
            proc.Start();
            if (enableRedirection)
            {
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
            }
            proc.WaitForExit(PollingTimeMs);
            while (!_runCancelled() && !proc.HasExited)
            {
                proc.WaitForExit(PollingTimeMs);
            }
        }

        /// <summary>
        /// callback function for spawnd process errors
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnErrorDataReceived(object sender, DataReceivedEventArgs e)
        {
            if (sender is not Process p) return;
            try
            {
                if (!p.HasExited || p.ExitCode == 0) return;
            }
            catch { return; }
            string errorData = e.Data;

            if (errorData.IsNullOrEmpty())
            {
                errorData = $"External process has exited with code {p.ExitCode}";
            }

            ConsoleWriter.WriteErrLine(errorData);
        }

        /// <summary>
        /// callback function for spawnd process output
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnOutputDataReceived(object sender, DataReceivedEventArgs e)
        {
            if (!e.Data.IsNullOrEmpty())
            {
                ConsoleWriter.WriteLine(e.Data);
            }
        }

        #endregion

    }
}