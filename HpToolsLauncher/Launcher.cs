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
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public enum CIName
    {
        Hudson,
        Jenkins,
        TFS,
        CCNET,
    }

    public class Launcher
    {
        private IXmlBuilder _xmlBuilder;
        private bool _ciRun = false;
        private JavaProperties _ciParams = new();
        private TestStorageType _runtype = TestStorageType.Unknown;
        private readonly string _failOnUftTestFailed;
        private static readonly char[] _commaAndSemiColon = new char[] { ',' , ';'};
        private readonly string _secretKey;
        private readonly string _initVector;

        public static string DateFormat { get; set; } = "dd/MM/yyyy HH:mm:ss";

        /// <summary>
        /// if running an alm job theses strings are mandatory:
        /// </summary>
        private readonly string[] requiredParamsForQcRun = { "almServerUrl",
                                 "almUserName",
                                 "almPassword",
                                 "almDomain",
                                 "almProject",
                                 "almRunMode",
                                 "almTimeout",
                                 "almRunHost"};

        /// <summary>
        /// a place to save the unique timestamp which shows up in properties/results/abort file names
        /// this timestamp per job run.
        /// </summary>
        public static string UniqueTimeStamp { get; set; }

        public enum ExitCodeEnum
        {
            Passed = 0,
            Failed = 1,
            Unstable = 2,
            Aborted = 3
        }
        /// <summary>
        /// saves the exit code in case we want to run all tests but fail at the end since a file wasn't found
        /// </summary>
        public static ExitCodeEnum ExitCode { get; set; } = ExitCodeEnum.Passed;

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="failOnTestFailed"></param>
        /// <param name="paramFileName"></param>
        /// <param name="runtype"></param>
        public Launcher(string failOnTestFailed, string paramFileName, TestStorageType runtype)
        {
            _runtype = runtype;
            if (paramFileName != null)
                _ciParams.Load(paramFileName);

            _failOnUftTestFailed = string.IsNullOrEmpty(failOnTestFailed) ? "N" : failOnTestFailed;
            _secretKey = Environment.GetEnvironmentVariable("AES_256_SECRET_KEY");
            _initVector = Environment.GetEnvironmentVariable("AES_256_SECRET_INIT_VECTOR");
        }

        /// <summary>
        /// decrypts strings which were encrypted by Encrypt (in the c# or java code, mainly for qc passwords)
        /// </summary>
        /// <param name="textToDecrypt"></param>
        /// <returns></returns>
        private string Decrypt(string textToDecrypt)
        {
            RijndaelManaged rijndaelCipher = new()
            {
                Mode = CipherMode.CBC,
                Padding = PaddingMode.PKCS7,
                KeySize = 256,
                BlockSize = 128,
                Key = Encoding.UTF8.GetBytes(_secretKey), // 32 bytes
                IV = Encoding.UTF8.GetBytes(_initVector) // 16 bytes
            };
            byte[] encryptedData = Convert.FromBase64String(textToDecrypt); 
            byte[] plainText = rijndaelCipher.CreateDecryptor().TransformFinalBlock(encryptedData, 0, encryptedData.Length);
            return Encoding.UTF8.GetString(plainText);
        }

        /// <summary>
        /// writes to console using the ConsolWriter class
        /// </summary>
        /// <param name="message"></param>
        private static void WriteToConsole(string message)
        {
            ConsoleWriter.WriteLine(message);
        }

        /// <summary>
        /// analyzes and runs the tests given in the param file.
        /// </summary>
        public void Run()
        {
            _ciRun = true;
            if (_runtype == TestStorageType.Unknown)
                Enum.TryParse(_ciParams["runType"], true, out _runtype);
            if (_runtype == TestStorageType.Unknown)
            {
                WriteToConsole(Resources.LauncherNoRuntype);
                return;
            }

            if (!_ciParams.ContainsKey("resultsFilename"))
            {
                WriteToConsole(Resources.LauncherNoResFilenameFound);
                return;
            }
            string resultsFilename = _ciParams["resultsFilename"];

            if (_ciParams.ContainsKey("uniqueTimeStamp"))
            {
                UniqueTimeStamp = _ciParams["uniqueTimeStamp"];
            }
            else
            {
                UniqueTimeStamp = resultsFilename.ToLower().Replace("results", string.Empty).Replace(".xml", string.Empty);
            }

            //create the runner according to type
            IAssetRunner runner = CreateRunner(_runtype);

            //runner instantiation failed (no tests to run or other problem)
            if (runner == null)
            {
                Environment.Exit((int)ExitCodeEnum.Failed);
            }

            //run the tests!
            RunTests(runner, resultsFilename);

            if (ExitCode != ExitCodeEnum.Passed)
                Environment.Exit((int)ExitCode);
        }

        /// <summary>
        /// creates the correct runner according to the given type
        /// </summary>
        /// <param name="runType"></param>
        /// <param name="ciParams"></param>
        IAssetRunner CreateRunner(TestStorageType runType)
        {
            IAssetRunner runner;
            switch (runType)
            {
                case TestStorageType.Alm:
                    //check that all required parameters exist
                    foreach (string param1 in requiredParamsForQcRun)
                    {
                        if (!_ciParams.ContainsKey(param1))
                        {
                            ConsoleWriter.WriteLine(string.Format(Resources.LauncherParamRequired, param1));
                            return null;
                        }
                    }

                    //parse params that need parsing
                    if (!double.TryParse(_ciParams["almTimeout"], out double dblQcTimeout))
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherTimeoutNotNumeric);
                        dblQcTimeout = int.MaxValue;
                    }

                    ConsoleWriter.WriteLine(string.Format(Resources.LauncherDisplayTimeout, dblQcTimeout));

                    if (!Enum.TryParse(_ciParams["almRunMode"], true, out QcRunMode enmQcRunMode))
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherIncorrectRunmode);
                        enmQcRunMode = QcRunMode.RUN_LOCAL;
                    }
                    ConsoleWriter.WriteLine(string.Format(Resources.LauncherDisplayRunmode, enmQcRunMode.ToString()));

                    //go over testsets in the parameters, and collect them
                    List<string> sets = GetParamsWithPrefix("TestSet", true);
                   
                    if (sets.Count == 0)
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherNoTests);
                        return null;
                    }
                   
                    bool isSSOEnabled = _ciParams.ContainsKey("almSSO") && Convert.ToBoolean(_ciParams["almSSO"]);
                    string clientID = _ciParams.GetOrDefault("clientID");
                    string apiKey = _ciParams.ContainsKey("apiKeySecret") ? Decrypt(_ciParams["apiKeySecret"]) : string.Empty;
                    string almUserName = _ciParams.GetOrDefault("almUserName");
                    string almPassword = _ciParams.ContainsKey("almPassword") ? Decrypt(_ciParams["almPassword"]) : string.Empty;

                    //create an Alm runner
                    runner = new AlmTestSetsRunner(_ciParams["almServerUrl"],
                                      almUserName,
                                      almPassword,
                                     _ciParams["almDomain"],
                                     _ciParams["almProject"],
                                     dblQcTimeout,
                                     enmQcRunMode,
                                     _ciParams["almRunHost"],
                                     sets,
                                     runType,
                                     isSSOEnabled,
                                     clientID,
                                     apiKey);
                    break;
                case TestStorageType.FileSystem:

                    //get the tests
                    IEnumerable<string> tests = GetParamsWithPrefix("Test");

                    IEnumerable<string> bambooEnvVarsWithCommas = GetParamsWithPrefix("JenkinsEnv");
                    Dictionary<string, string> bambooEnvVars = new();
                    foreach (string var in bambooEnvVarsWithCommas)
                    { 
                        string[] nameVal = var.Split(_commaAndSemiColon);
                        bambooEnvVars.Add(nameVal[0], nameVal[1]);
                    }
                    //parse the timeout into a TimeSpan
                    TimeSpan timeout = TimeSpan.MaxValue;
                    if (_ciParams.ContainsKey("fsTimeout"))
                    {
                        string strTimoutInSeconds = _ciParams["fsTimeout"];
                        if (strTimoutInSeconds.Trim() != "-1")
                        {
                            int.TryParse(strTimoutInSeconds, out int intTimoutInSeconds);
                            timeout = TimeSpan.FromSeconds(intTimoutInSeconds);
                        }
                    }

                    //LR specific values:
                    //default values are set by JAVA code, in com.hp.application.automation.tools.model.RunFromFileSystemModel.java

                    int pollingInterval = 30;
                    if (_ciParams.ContainsKey("controllerPollingInterval"))
                        pollingInterval = int.Parse(_ciParams["controllerPollingInterval"]);

                    TimeSpan perScenarioTimeOut = TimeSpan.MaxValue;
                    if (_ciParams.ContainsKey("PerScenarioTimeOut"))
                    {
                        string strTimoutInSeconds = _ciParams["PerScenarioTimeOut"];
                        if (strTimoutInSeconds.Trim() != "-1")
                        {
                            if (int.TryParse(strTimoutInSeconds, out int intTimoutInSeconds))
                                perScenarioTimeOut = TimeSpan.FromMinutes(intTimoutInSeconds);
                        }
                    }

                    char[] delim = { '\n' };
                    List<string> ignoreErrorStrings = new();
                    if (_ciParams.ContainsKey("ignoreErrorStrings"))
                    {
                        ignoreErrorStrings.AddRange(_ciParams["ignoreErrorStrings"].Split(delim, StringSplitOptions.RemoveEmptyEntries));
                    }

                    //--MC connection info
                    McConnectionInfo mcConnectionInfo = null;
                    try
                    {
                        mcConnectionInfo = new McConnectionInfo(_ciParams);
                    }
                    catch (Exception ex)
                    {
                        ConsoleWriter.WriteErrLine(ex.Message);
                        Environment.Exit((int)ExitCodeEnum.Failed);
                    }

                    // other mobile info
                    string mobileinfo = string.Empty;
                    if (_ciParams.ContainsKey("mobileinfo"))
                    {
                        mobileinfo = _ciParams["mobileinfo"];
                    }

                    if (tests.IsNullOrEmpty())
                    {
                        WriteToConsole(Resources.LauncherNoTestsFound);
                    }

                    List<string> validTests = Helper.ValidateFiles(tests);
                    if (tests.HasAny() && validTests.Count == 0)
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherNoValidTests);
                        return null;
                    }
                    RunAsUser uftRunAsUser = null;
                    string username = _ciParams.GetOrDefault("uftRunAsUserName");
                    if (!username.IsNullOrEmpty())
                    {
                        string encryptedAndEncodedPwd = _ciParams.GetOrDefault("uftRunAsUserEncodedPassword");
                        string encryptedPwd = _ciParams.GetOrDefault("uftRunAsUserPassword");
                        if (!encryptedAndEncodedPwd.IsNullOrEmpty())
                        {
                            string encodedPwd = Decrypt(encryptedAndEncodedPwd);
                            uftRunAsUser = new RunAsUser(username, encodedPwd);
                        }
                        else if (!encryptedPwd.IsNullOrEmpty())
                        {
                            string plainTextPwd = Decrypt(encryptedPwd);
                            uftRunAsUser = new RunAsUser(username, plainTextPwd.ToSecureString());
                        }
                    }
                    runner = new FileSystemTestsRunner(validTests, timeout, pollingInterval, perScenarioTimeOut, ignoreErrorStrings, mcConnectionInfo, mobileinfo, bambooEnvVars, uftRunAsUser);

                    break;

                default:
                    runner = null;
                    break;
            }
            return runner;
        }

        private List<string> GetParamsWithPrefix(string prefix, bool skipEmptyEntries = false)
        {
            int idx = 1;
            List<string> parameters = new();
            while (_ciParams.ContainsKey(prefix + idx))
            {
                string set = _ciParams[prefix + idx];
                if (set.StartsWith("Root\\"))
                    set = set.Substring(5);

                set = set.TrimEnd(" \\".ToCharArray());
                if (!(skipEmptyEntries && set.IsNullOrWhiteSpace()))
                {
                    parameters.Add(set);
                }

                ++idx;
            }
            return parameters;
        }

        /// <summary>
        /// used by the run fuction to run the tests
        /// </summary>
        /// <param name="runner"></param>
        /// <param name="resultsFile"></param>
        private void RunTests(IAssetRunner runner, string resultsFile)
        {
            try
            {
                if (_ciRun)
                {
                    _xmlBuilder = new JunitXmlBuilder { XmlName = resultsFile };
                }

                TestSuiteRunResults results = runner.Run();

                if (results == null)
                    Environment.Exit((int)ExitCodeEnum.Failed);

                _xmlBuilder.CreateXmlFromRunResults(results);

                //if there is an error
                if (results.TestRuns.Any(tr => tr.TestState.In(TestState.Failed, TestState.Error)))
                {
                    ExitCode = ExitCodeEnum.Failed;
                }

                //this is the total run summary
                ConsoleWriter.ActiveTestRun = null;
                string runStatus = ExitCode.In(ExitCodeEnum.Passed, ExitCodeEnum.Unstable) ? "Job succeeded" : "Job failed";
                int numFailures = results.TestRuns.Count(t => t.TestState == TestState.Failed);
                int numSuccess = results.TestRuns.Count(t => t.TestState == TestState.Passed);
                int numErrors = results.TestRuns.Count(t => t.TestState == TestState.Error);
                ConsoleWriter.WriteLine(Resources.LauncherDoubleSeperator);
                ConsoleWriter.WriteLine(string.Format(Resources.LauncherDisplayStatistics, runStatus, results.TestRuns.Count, numSuccess, numFailures, numErrors));

                if (!runner.RunWasCancelled)
                {
                    results.TestRuns.ForEach(tr => ConsoleWriter.WriteLine(((tr.HasWarnings) ? "Warning".PadLeft(7) : tr.TestState.ToString().PadRight(7)) + ": " + tr.TestPath));
                    
                    ConsoleWriter.WriteLine(Resources.LauncherDoubleSeperator);
                    if (ConsoleWriter.ErrorSummaryLines != null && ConsoleWriter.ErrorSummaryLines.Count > 0)
                    {
                        ConsoleWriter.WriteLine("Job Errors summary:");
                        ConsoleWriter.ErrorSummaryLines.ForEach(line => ConsoleWriter.WriteLine(line));
                    }
                }

                Environment.Exit((int)ExitCode);
            }
            finally
            {
                try
                {
                    runner.Dispose();
                }
                catch (Exception ex)
                {
                    ConsoleWriter.WriteLine(string.Format(Resources.LauncherRunnerDisposeError, ex.Message));
                };
            }

        }

    }
}
