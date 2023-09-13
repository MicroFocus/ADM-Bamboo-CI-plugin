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
using System;
using System.Linq;
using System.IO;
using System.Xml;
using QTObjectModelLib;
using Resources = HpToolsLauncher.Properties.Resources;
using System.Threading;
using System.Diagnostics;
using System.Collections.Generic;
using HpToolsLauncher.Utils;
using System.Runtime.InteropServices;
using HpToolsLauncher.Common;

namespace HpToolsLauncher
{
    public class GuiTestRunner : IFileSysTestRunner
    {
        private static HashSet<string> _colLoadedAddinNames = null;
        private readonly Type _qtType = Type.GetTypeFromProgID("Quicktest.Application");
        private readonly Type _runResultsOptionsType = Type.GetTypeFromProgID("QuickTest.RunResultsOptions");
        private readonly IAssetRunner _runNotifier;
        private readonly object _lockObject = new();
        private TimeSpan _timeLeftUntilTimeout = TimeSpan.MaxValue;
        private readonly Stopwatch _stopwatch = null;
        private Application _qtpApplication;
        private ParameterDefinitions _qtpParamDefs;
        private Parameters _qtpParameters;
        private readonly bool _useUFTLicense;
        private RunCancelledDelegate _runCancelled;
        private McConnectionInfo _mcConnection;
        private string _mobileInfo;
        private readonly RunAsUser _uftRunAsUser;
        private const int DISP_E_MEMBERNOTFOUND = -2147352573;
        private const string MOBILE_HOST_ADDRESS = "ALM_MobileHostAddress";
        private const string MOBILE_HOST_PORT = "ALM_MobileHostPort";
        private const string MOBILE_USER = "ALM_MobileUserName";
        private const string MOBILE_PASSWORD = "ALM_MobilePassword";
        private const string MOBILE_CLIENTID = "EXTERNAL_MobileClientID";
        private const string MOBILE_SECRET = "EXTERNAL_MobileSecretKey";
        private const string MOBILE_AUTH_TYPE = "EXTERNAL_MobileAuthType";
        private const string MOBILE_TENANT = "EXTERNAL_MobileTenantId";
        private const string MOBILE_USE_SSL = "ALM_MobileUseSSL";
        private const string MOBILE_USE_PROXY = "EXTERNAL_MobileProxySetting_UseProxy";
        private const string MOBILE_PROXY_SETTING_ADDRESS = "EXTERNAL_MobileProxySetting_Address";
        private const string MOBILE_PROXY_SETTING_PORT = "EXTERNAL_MobileProxySetting_Port";
        private const string MOBILE_PROXY_SETTING_AUTHENTICATION = "EXTERNAL_MobileProxySetting_Authentication";
        private const string MOBILE_PROXY_SETTING_USERNAME = "EXTERNAL_MobileProxySetting_UserName";
        private const string MOBILE_PROXY_SETTING_PASSWORD = "EXTERNAL_MobileProxySetting_Password";
        private const string MOBILE_INFO = "mobileinfo";
        private const string REPORT = "Report";
        private const string READY = "Ready";
        private const string WAITING = "Waiting";
        private const string BUSY = "Busy";
        private const string RUNNING = "Running";
        private const string PASSED = "Passed";
        private const string WARNING = "Warning";

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="runNotifier"></param>
        /// <param name="useUftLicense"></param>
        /// <param name="timeLeftUntilTimeout"></param>
        public GuiTestRunner(IAssetRunner runNotifier, bool useUftLicense, TimeSpan timeLeftUntilTimeout, McConnectionInfo mcConnectionInfo, string mobileInfo, RunAsUser uftRunAsUser)
        {
            _timeLeftUntilTimeout = timeLeftUntilTimeout;
            _stopwatch = Stopwatch.StartNew();
            _runNotifier = runNotifier;
            _useUFTLicense = useUftLicense;
            _mcConnection = mcConnectionInfo;
            _mobileInfo = mobileInfo;
            _uftRunAsUser = uftRunAsUser;
        }

        private void SetMobileInfo()
        {
            if (_mcConnection == null) return;
            #region Mc connection and other mobile info

            // Mc Address, username and password
            if (!string.IsNullOrEmpty(_mcConnection.HostAddress))
            {
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_HOST_ADDRESS, _mcConnection.HostAddress);
                if (!string.IsNullOrEmpty(_mcConnection.HostPort))
                {
                    _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_HOST_PORT, _mcConnection.HostPort);
                }
            }

            var mcAuthType = _mcConnection.MobileAuthType;
            switch (mcAuthType)
            {
                case McConnectionInfo.AuthType.AuthToken:
                    var token = _mcConnection.GetAuthToken();

                    _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_CLIENTID, token.ClientId);
                    _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_SECRET, token.SecretKey);

                    break;
                case McConnectionInfo.AuthType.UsernamePassword:
                    if (!string.IsNullOrEmpty(_mcConnection.UserName))
                    {
                        _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_USER, _mcConnection.UserName);
                    }

                    if (!string.IsNullOrEmpty(_mcConnection.Password))
                    {
                        string encriptedMcPassword = WinUserNativeMethods.ProtectBSTRToBase64(_mcConnection.Password);
                        if (encriptedMcPassword == null)
                        {
                            ConsoleWriter.WriteLine("ProtectBSTRToBase64 fail for mcPassword");
                            throw new Exception("ProtectBSTRToBase64 fail for mcPassword");
                        }
                        _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PASSWORD, encriptedMcPassword);
                    }
                    break;
            }

            // set authentication type
            _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_AUTH_TYPE, mcAuthType);

            // set tenantID
            if (!string.IsNullOrEmpty(_mcConnection.TenantId))
            {
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_TENANT, _mcConnection.TenantId);
            }

            // ssl and proxy info
            _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_USE_SSL, _mcConnection.UseSslAsInt);

            if (_mcConnection.UseProxy)
            {
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_USE_PROXY, _mcConnection.UseProxyAsInt);
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PROXY_SETTING_ADDRESS, _mcConnection.ProxyAddress);
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PROXY_SETTING_PORT, _mcConnection.ProxyPort);
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PROXY_SETTING_AUTHENTICATION, _mcConnection.ProxyAuth);
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PROXY_SETTING_USERNAME, _mcConnection.ProxyUserName);
                string encriptedMcProxyPassword = WinUserNativeMethods.ProtectBSTRToBase64(_mcConnection.ProxyPassword);
                if (encriptedMcProxyPassword == null)
                {
                    ConsoleWriter.WriteLine("ProtectBSTRToBase64 fail for mc proxy Password");
                    throw new Exception("ProtectBSTRToBase64 fail for mc proxy Password");
                }
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_PROXY_SETTING_PASSWORD, encriptedMcProxyPassword);
            }

            // Mc info (device, app, launch and terminate data)
            if (!string.IsNullOrEmpty(_mobileInfo))
            {
                _qtpApplication.TDPierToTulip.SetTestOptionsVal(MOBILE_INFO, _mobileInfo);
            }

            #endregion
        }

        #region QTP

        /// <summary>
        /// runs the given test and returns resutls
        /// </summary>
        /// <param name="testPath"></param>
        /// <param name="errorReason"></param>
        /// <param name="runCanclled"></param>
        /// <returns></returns>
        public TestRunResults RunTest(TestInfo testinf, ref string errorReason, RunCancelledDelegate runCanclled)
        {
            var testPath = testinf.TestPath;
            TestRunResults runDesc = new() { ReportLocation = testPath, TestPath = testPath };
            ConsoleWriter.ActiveTestRun = runDesc;
            ConsoleWriter.WriteLine($"{DateTime.Now.ToString(Launcher.DateFormat)} Running: {testPath}");

            _runCancelled = runCanclled;

            if (!Helper.IsQtpInstalled())
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = string.Format(Resources.GeneralQtpNotInstalled, Environment.MachineName);
                ConsoleWriter.WriteErrLine(runDesc.ErrorDesc);
                Environment.ExitCode = (int)Launcher.ExitCodeEnum.Failed;
                return runDesc;
            }

            try
            {
                lock (_lockObject)
                {
                    _qtpApplication = Activator.CreateInstance(_qtType) as Application;
                    if (_uftRunAsUser != null)
                    {
                        try
                        {
                            _qtpApplication.LaunchAsUser(_uftRunAsUser.Username, _uftRunAsUser.EncodedPassword);
                        }
                        catch (COMException e)
                        {
                            if (e.ErrorCode == DISP_E_MEMBERNOTFOUND)
                            {
                                errorReason = Resources.UftLaunchAsUserNotSupported;
                            }
                            throw;
                        }
                        if (_qtpApplication.Visible)
                        {
                            _qtpApplication.Visible = false;
                        }
                    }

                    Version qtpVersion = Version.Parse(_qtpApplication.Version);
                    if (qtpVersion.Equals(new Version(11, 0)))
                    {
                        runDesc.ReportLocation = GetReportLocation(testPath);
                    }

                    // set Mc connection and other mobile info into rack if neccesary
                    SetMobileInfo();

                    // Check for required Addins
                    LoadNeededAddins(testPath);

                    if (!_qtpApplication.Launched)
                    {
                        if (_runCancelled())
                        {
                            QTPTestCleanup();
                            KillQtp();
                            runDesc.TestState = TestState.Error;
                            return runDesc;
                        }
                        // Launch application after set Addins
                        _qtpApplication.Launch();
                        _qtpApplication.Visible = false;
                    }
                }
            }
            catch (Exception e)
            {
                errorReason = Resources.QtpNotLaunchedError;
                runDesc.TestState = TestState.Error;
                runDesc.ReportLocation = string.Empty;
                runDesc.ErrorDesc = e.Message;
                return runDesc;
            }

            if (_qtpApplication.Test != null && _qtpApplication.Test.Modified)
            {
                var message = Resources.QtpNotLaunchedError;
                errorReason = message;
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            _qtpApplication.UseLicenseOfType(_useUFTLicense
                                                 ? tagUnifiedLicenseType.qtUnifiedFunctionalTesting
                                                 : tagUnifiedLicenseType.qtNonUnified);

            if (!HandleInputParameters(testPath, ref errorReason, testinf.GetParameterDictionaryForQTP(), testinf.DataTablePath))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            GuiTestRunResult guiTestRunResult = ExecuteQTPRun(runDesc);
            runDesc.ReportLocation = guiTestRunResult.ReportPath;

            if (!guiTestRunResult.IsSuccess)
            {
                runDesc.TestState = TestState.Error;
                return runDesc;
            }

            if (!HandleOutputArguments(ref errorReason))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            QTPTestCleanup();

            return runDesc;
        }

        /// <summary>
        /// performs global cleanup code for this type of runner
        /// </summary>
        public void CleanUp()
        {
            try
            {
                //if we don't have a qtp instance, create one
                if (_qtpApplication == null)
                {
                    _qtpApplication = Activator.CreateInstance(_qtType) as Application;
                }

                //if the app is running, close it.
                if (_qtpApplication.Launched)
                    _qtpApplication.Quit();
            }
            catch
            {
                //nothing to do. (cleanup code should not throw exceptions, and there is no need to log this as an error in the test)
            }
        }

        /// <summary>
        /// Set the test Addins 
        /// </summary>
        private void LoadNeededAddins(string fileName)
        {
            bool blnNeedToLoadAddins = false;

            //if not launched, we have no addins.
            if (!_qtpApplication.Launched)
                _colLoadedAddinNames = null;

            try
            {
                HashSet<string> colCurrentTestAddins = new();

                var testAddinsObj = _qtpApplication.GetAssociatedAddinsForTest(fileName);
                object[] testAddins = (object[])testAddinsObj;

                foreach (string addin in testAddins.Cast<string>())
                {
                    colCurrentTestAddins.Add(addin);
                }

                if (_colLoadedAddinNames != null)
                {
                    //check if we have a missing addin (and need to quit Qtp, and reload with new addins)
                    foreach (string addin in testAddins.Cast<string>())
                    {
                        if (!_colLoadedAddinNames.Contains(addin))
                        {
                            blnNeedToLoadAddins = true;
                            break;
                        }
                    }

                    //check if there is no extra addins that need to be removed
                    if (_colLoadedAddinNames.Count != colCurrentTestAddins.Count)
                    {
                        blnNeedToLoadAddins = true;
                    }
                }
                else
                {
                    //first time = load addins.
                    blnNeedToLoadAddins = true;
                }

                _colLoadedAddinNames = colCurrentTestAddins;

                //the addins need to be refreshed, load new addins
                if (blnNeedToLoadAddins)
                {
                    if (_qtpApplication.Launched && _uftRunAsUser == null)
                        _qtpApplication.Quit();
                    _qtpApplication.SetActiveAddins(ref testAddinsObj, out object _);
                }

            }
            catch
            {
                // Try anyway to run the test
            }
        }

        /// <summary>
        /// Activate all Installed Addins 
        /// </summary>
        private void ActivateAllAddins()
        {
            try
            {
                // Get Addins collection
                Addins qtInstalledAddins = _qtpApplication.Addins;

                if (qtInstalledAddins.Count > 0)
                {
                    string[] qtAddins = new string[qtInstalledAddins.Count];

                    // Addins Object is 1 base order
                    for (int idx = 1; idx <= qtInstalledAddins.Count; ++idx)
                    {
                        // Our list is 0 base order
                        qtAddins[idx - 1] = qtInstalledAddins[idx].Name;
                    }

                    object erroDescription;
                    var addinNames = (object)qtAddins;

                    _qtpApplication.SetActiveAddins(ref addinNames, out erroDescription);
                }
            }
            catch (Exception)
            {
                // Try anyway to run the test
            }
        }

        /// <summary>
        /// runs the given test QTP and returns results
        /// </summary>
        /// <param name="testResults">the test results object containing test info and also receiving run results</param>
        /// <returns></returns>
        private GuiTestRunResult ExecuteQTPRun(TestRunResults testResults)
        {
            GuiTestRunResult result = new GuiTestRunResult { IsSuccess = true };
            try
            {
                var options = (RunResultsOptions)Activator.CreateInstance(_runResultsOptionsType);
                options.ResultsLocation = testResults.ReportLocation;
                _qtpApplication.Options.Run.RunMode = "Fast";

                //Check for cancel before executing
                if (_runCancelled())
                {
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = Resources.GeneralTestCanceled;
                    ConsoleWriter.WriteLine(Resources.GeneralTestCanceled);
                    result.IsSuccess = false;
                    return result;
                }
                ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerRunningTest, testResults.TestPath));

                _qtpApplication.Test.Run(options, false, _qtpParameters);

                result.ReportPath = Path.Combine(testResults.ReportLocation, REPORT);
                int slept = 0;
                while (slept < 20000 && _qtpApplication.GetStatus().In(READY, WAITING))
                {
                    Thread.Sleep(50);
                    slept += 50;
                }

                while (!_runCancelled() && _qtpApplication.GetStatus().In(RUNNING, BUSY))
                {
                    Thread.Sleep(200);
                    if (_timeLeftUntilTimeout - _stopwatch.Elapsed <= TimeSpan.Zero)
                    {
                        _qtpApplication.Test.Stop();
                        testResults.TestState = TestState.Error;
                        testResults.ErrorDesc = Resources.GeneralTimeoutExpired;
                        ConsoleWriter.WriteLine(Resources.GeneralTimeoutExpired);

                        result.IsSuccess = false;
                        return result;
                    }
                }

                if (_runCancelled())
                {
                    QTPTestCleanup();
                    KillQtp();
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = Resources.GeneralTestCanceled;
                    ConsoleWriter.WriteLine(Resources.GeneralTestCanceled);
                    Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                    result.IsSuccess = false;
                    return result;
                }
                string lastError = _qtpApplication.Test.LastRunResults.LastError;

                //read the lastError
                if (!lastError.IsNullOrEmpty())
                {
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = lastError;
                }

                // the way to check the logical success of the target QTP test is: app.Test.LastRunResults.Status == "Passed".
                if (_qtpApplication.Test.LastRunResults.Status == PASSED)
                {
                    testResults.TestState = TestState.Passed;
                }
                else if (_qtpApplication.Test.LastRunResults.Status == WARNING)
                {
                    testResults.TestState = TestState.Passed;
                    testResults.HasWarnings = true;

                    if (!Launcher.ExitCode.In(Launcher.ExitCodeEnum.Failed, Launcher.ExitCodeEnum.Aborted))
                        Launcher.ExitCode = Launcher.ExitCodeEnum.Unstable;
                }
                else
                {
                    testResults.TestState = TestState.Failed;
                    testResults.FailureDesc = "Test failed";

                    Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                }
            }
            catch (NullReferenceException e)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.GeneralErrorWithStack, e.Message, e.StackTrace));
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }
            catch (SystemException e)
            {
                KillQtp();
                ConsoleWriter.WriteLine(string.Format(Resources.GeneralErrorWithStack, e.Message, e.StackTrace));
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }
            catch (Exception e2)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.GeneralErrorWithStack, e2.Message, e2.StackTrace));
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }

            return result;
        }

        private void KillQtp()
        {
            //error during run, process may have crashed (need to cleanup, close QTP and qtpRemote for next test to run correctly)
            CleanUp();

            //kill the qtp automation, to make sure it will run correctly next time
            Process[] processes = Process.GetProcessesByName("qtpAutomationAgent");
            Process qtpAuto = processes.Where(p => p.SessionId == Process.GetCurrentProcess().SessionId).FirstOrDefault();
            if (qtpAuto != null)
                qtpAuto.Kill();
        }

        private bool HandleOutputArguments(ref string errorReason)
        {
            try
            {
                var outputArguments = new XmlDocument { PreserveWhitespace = true };
                outputArguments.LoadXml("<Arguments/>");

                for (int i = 1; i <= _qtpParamDefs.Count; ++i)
                {
                    var pd = _qtpParamDefs[i];
                    if (pd.InOut == qtParameterDirection.qtParamDirOut)
                    {
                        var node = outputArguments.CreateElement(pd.Name);
                        var value = _qtpParameters[pd.Name].Value;
                        if (value != null)
                            node.InnerText = value.ToString();

                        outputArguments.DocumentElement.AppendChild(node);
                    }
                }
            }
            catch
            {
                errorReason = Resources.QtpNotLaunchedError;
                return false;
            }
            return true;
        }
        private bool VerifyParameterValueType(object paramValue, qtParameterType type)
        {
            bool legal = false;

            switch (type)
            {
                case qtParameterType.qtParamTypeBoolean:
                    legal = paramValue is bool;
                    break;

                case qtParameterType.qtParamTypeDate:
                    legal = paramValue is DateTime;
                    break;

                case qtParameterType.qtParamTypeNumber:
                    legal = ((paramValue is int) || (paramValue is long) || (paramValue is decimal) || (paramValue is float) || (paramValue is double));
                    break;

                case qtParameterType.qtParamTypePassword:
                    legal = paramValue is string;
                    break;

                case qtParameterType.qtParamTypeString:
                    legal = paramValue is string;
                    break;

                default:
                    legal = true;
                    break;
            }

            return legal;
        }

        private bool HandleInputParameters(string fileName, ref string errorReason, Dictionary<string, object> inputParams, string dataTablePath)
        {
            try
            {
                string path = fileName;

                if (_runCancelled())
                {
                    QTPTestCleanup();
                    KillQtp();
                    return false;
                }

                _qtpApplication.Open(path, true, false);
                _qtpParamDefs = _qtpApplication.Test.ParameterDefinitions;
                _qtpParameters = _qtpParamDefs.GetParameters();

                // handle all parameters (index starts with 1 !!!)
                for (int i = 1; i <= _qtpParamDefs.Count; i++)
                {
                    // input parameters
                    if (_qtpParamDefs[i].InOut == qtParameterDirection.qtParamDirIn)
                    {
                        string paramName = _qtpParamDefs[i].Name;
                        qtParameterType type = _qtpParamDefs[i].Type;

                        // if the caller supplies value for a parameter we set it
                        if (inputParams.ContainsKey(paramName))
                        {
                            // first verify that the type is correct
                            object paramValue = inputParams[paramName];
                            if (!VerifyParameterValueType(paramValue, type))
                            {
                                ConsoleWriter.WriteErrLine(string.Format("Illegal input parameter type (skipped). param: '{0}'. expected type: '{1}'. actual type: '{2}'", paramName, Enum.GetName(typeof(qtParameterType), type), paramValue.GetType()));
                            }
                            else
                            {
                                _qtpParameters[paramName].Value = paramValue;
                            }
                        }
                    }
                }

                // specify data table path
                if (dataTablePath != null)
                {
                    _qtpApplication.Test.Settings.Resources.DataTablePath = dataTablePath;
                    ConsoleWriter.WriteLine("Using external data table: " + dataTablePath);
                }
            }
            catch
            {
                errorReason = Resources.QtpRunError;
                return false;
            }
            return true;

        }

        /// <summary>
        /// stops and closes qtp test, to make sure nothing is left floating after run.
        /// </summary>
        private void QTPTestCleanup()
        {
            try
            {
                lock (_lockObject)
                {
                    if (_qtpApplication == null)
                    {
                        return;
                    }

                    var qtpTest = _qtpApplication.Test;
                    if (qtpTest != null)
                    {
                        if (_qtpApplication.GetStatus().Equals("Running") || _qtpApplication.GetStatus().Equals("Busy"))
                        {
                            try
                            {
                                _qtpApplication.Test.Stop();
                            }
                            catch
                            {
                            }
                            finally
                            {

                            }
                        }
                    }
                }
            }
            catch
            {
            }

            _qtpParameters = null;
            _qtpParamDefs = null;
            _qtpApplication = null;
        }

        private string GetReportLocation(string testPath)
        {
            string path = Path.Combine(testPath, REPORT);
            if (Directory.Exists(path))
            {
                Directory.Delete(path, true);
                Directory.CreateDirectory(path);
            }
            return path;
        }

        #endregion

        /// <summary>
        /// holds the resutls for a GUI test
        /// </summary>
        private class GuiTestRunResult
        {
            public GuiTestRunResult()
            {
                ReportPath = string.Empty;
            }

            public bool IsSuccess { get; set; }
            public string ReportPath { get; set; }
        }
    }
}
