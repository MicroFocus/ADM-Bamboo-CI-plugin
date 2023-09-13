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
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;
using Mercury.TD.Client.Ota.QC9;

//using Mercury.TD.Client.Ota.Api;

namespace HpToolsLauncher
{
    public class AlmTestSetsRunner : RunnerBase, IDisposable
    {
        private ITDConnection13 _tdConnection;
        private ITDConnection2 _tdConnectionOld;

        private static readonly char[] _backSlash = new char[] { '\\' };

        public ITDConnection13 TdConnection
        {
            get
            {
                if (_tdConnection == null)
                {
                    CreateTdConnection();
                }
                return _tdConnection;
            }
        }

        public ITDConnection2 TdConnectionOld
        {
            get
            {
                if (_tdConnectionOld == null)
                    CreateTdConnectionOld();
                return _tdConnectionOld;
            }
        }

        QcRunMode m_runMode = QcRunMode.RUN_LOCAL;
        double m_timeout = -1;
        bool m_blnConnected = false;
        //ITDConnection2 tdConnection = null;
        List<string> colTestSets = new List<string>();
        string m_runHost = null;
        string m_qcServer = null;
        string m_qcUser = null;
        string m_qcProject = null;
        string m_qcDomain = null;

        public bool SSOEnabled { get; set; }

        public string ClientID { get; set; }

        public string ApiKey { get; set; }

        public bool Connected
        {
            get { return m_blnConnected; }
            set { m_blnConnected = value; }
        }

        public List<string> TestSets
        {
            get { return colTestSets; }
            set { colTestSets = value; }
        }

        public QcRunMode RunMode
        {
            get { return m_runMode; }
            set { m_runMode = value; }
        }

        public double Timeout
        {
            get { return m_timeout; }
            set { m_timeout = value; }
        }

        public string RunHost
        {
            get { return m_runHost; }
            set { m_runHost = value; }
        }
        public TestStorageType Storage { get; set; }

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="qcServer"></param>
        /// <param name="qcUser"></param>
        /// <param name="qcPassword"></param>
        /// <param name="qcDomain"></param>
        /// <param name="qcProject"></param>
        /// <param name="intQcTimeout"></param>
        /// <param name="enmQcRunMode"></param>
        /// <param name="runHost"></param>
        /// <param name="qcTestSets"></param>
        public AlmTestSetsRunner(string qcServer,
                                string qcUser,
                                string qcPassword,
                                string qcDomain,
                                string qcProject,
                                double intQcTimeout,
                                QcRunMode enmQcRunMode,
                                string runHost,
                                List<string> qcTestSets,
                                TestStorageType testStorageType,
                                bool isSSOEnabled,
                                string qcClientId,
                                string qcApiKey)
        {
            Timeout = intQcTimeout;
            RunMode = enmQcRunMode;
            RunHost = runHost;

            m_qcServer = qcServer;
            m_qcUser = qcUser;
            m_qcProject = qcProject;
            m_qcDomain = qcDomain;
            SSOEnabled = isSSOEnabled;
            ClientID = qcClientId;
            ApiKey = qcApiKey;
          
            Connected = ConnectToProject(m_qcServer, m_qcUser, qcPassword, m_qcDomain, m_qcProject, SSOEnabled, ClientID, ApiKey);
            TestSets = qcTestSets;
            Storage = testStorageType;
            if (!Connected)
            {
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }
        }

        /// <summary>
        /// destructor - ensures dispose of connection
        /// </summary>
        ~AlmTestSetsRunner()
        {
            Dispose(false);
        }


        /// <summary>
        /// runs the tests given to the object.
        /// </summary>
        /// <returns></returns>
        public override TestSuiteRunResults Run()
        {
            if (!Connected)
                return null;
            TestSuiteRunResults activeRunDesc = new TestSuiteRunResults();
            //find all the testSets under if given some folders in our list
            try
            {
                FindAllTestSetsUnderFolders();
            }
            catch (Exception ex)
            {

                ConsoleWriter.WriteErrLine(string.Format(Resources.AlmRunnerErrorBadQcInstallation, ex.Message, ex.StackTrace));
                return null;
            }

            //run all the TestSets
            foreach (string testset in TestSets)
            {
                string testset1 = testset.TrimEnd(_backSlash);

                int pos = testset1.LastIndexOf('\\');
                string tsDir = string.Empty;
                string tsName = testset1;
                if (pos != -1)
                {
                    tsDir = testset1.Substring(0, pos).Trim(_backSlash);
                    tsName = testset1.Substring(pos, testset1.Length - pos).Trim(_backSlash);
                }

                TestSuiteRunResults desc = RunTestSet(tsDir, tsName, Timeout, RunMode, RunHost, Storage);
                if (desc != null)
                    activeRunDesc.AppendResults(desc);
            }

            return activeRunDesc;
        }

        /// <summary>
        /// Creates a connection to QC (for ALM 12.60 and 15)
        /// </summary>
        private void CreateTdConnection()
        {
            Type type = Type.GetTypeFromProgID("TDApiOle80.TDConnection");

            if (type == null)
            {
                ConsoleWriter.WriteLine(GetAlmNotInstalledError());
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            try
            {
                object conn = Activator.CreateInstance(type);
                _tdConnection = conn as ITDConnection13;
            }
            catch (FileNotFoundException ex)
            {
                ConsoleWriter.WriteLine(GetAlmNotInstalledError());
                ConsoleWriter.WriteLine(ex.Message);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }
        }

        /// <summary>
        /// creats a connection to Qc
        /// </summary>
        private void CreateTdConnectionOld()
        {
            Type type = Type.GetTypeFromProgID("TDApiOle80.TDConnection");

            if (type == null)
            {
                ConsoleWriter.WriteLine(GetAlmNotInstalledError());
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            try
            {
                object conn = Activator.CreateInstance(type);
                this._tdConnectionOld = conn as ITDConnection2;
            }
            catch (FileNotFoundException)
            {
                ConsoleWriter.WriteLine(GetAlmNotInstalledError());
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }
        }

        /// <summary>
        /// finds all folders in the TestSet list, scans their tree and adds all sets under the given folders
        /// updates the TestSets by expanding the folders, and removing them, so only Test sets remain in the collection
        /// </summary>
        private void FindAllTestSetsUnderFolders()
        {
            List<string> extraSetsList = new();
            List<string> removeSetsList = new();
            // var tsTreeManager = (ITestSetTreeManager)tdConnection.TestSetTreeManager;
            ITestSetTreeManager tsTreeManager;
            if (TdConnection != null)
            {
                _tdConnection.KeepConnection = true;
                tsTreeManager = (ITestSetTreeManager)_tdConnection.TestSetTreeManager;
            }
            else
            {
                _tdConnectionOld.KeepConnection = true;
                tsTreeManager = (ITestSetTreeManager)_tdConnectionOld.TestSetTreeManager;
            }

            //go over all the testsets / testSetFolders and check which is which
            foreach (string testsetOrFolder in TestSets)
            {
                //try getting the folder
                ITestSetFolder tsFolder = GetFolder($"Root\\{testsetOrFolder.TrimEnd(_backSlash)}");

                //if it exists it's a folder and should be traversed to find all sets
                if (tsFolder != null)
                {
                    removeSetsList.Add(testsetOrFolder);

                    List<string> setList = GetAllTestSetsFromDirTree(tsFolder);
                    extraSetsList.AddRange(setList);
                }

            }

            TestSets.RemoveAll(removeSetsList.Contains);
            TestSets.AddRange(extraSetsList);
        }

        /// <summary>
        /// recursively find all testsets in the qc directory tree, starting from a given folder
        /// </summary>
        /// <param name="tsFolder"></param>
        /// <param name="tsTreeManager"></param>
        /// <returns></returns>
        private List<string> GetAllTestSetsFromDirTree(ITestSetFolder tsFolder)
        {
            List<string> retVal = new List<string>();
            List children = tsFolder.FindChildren(string.Empty);
            List testSets = tsFolder.FindTestSets(string.Empty);

            if (testSets != null)
            {
                foreach (ITestSet childSet in testSets)
                {
                    string tsPath = childSet.TestSetFolder.Path;
                    tsPath = tsPath.Substring(5).Trim(_backSlash);
                    string tsFullPath = $"{tsPath}\\{childSet.Name}";
                    retVal.Add(tsFullPath.TrimEnd());
                }
            }

            if (children != null)
            {
                foreach (ITestSetFolder childFolder in children)
                {
                    GetAllTestSetsFromDirTree(childFolder);
                }
            }
            return retVal;
        }


        /// <summary>
        /// get a QC folder
        /// </summary>
        /// <param name="testset"></param>
        /// <returns>the folder object</returns>
        private ITestSetFolder GetFolder(string testset)
        {
            var tsTreeManager = (ITestSetTreeManager)_tdConnection.TestSetTreeManager;
            ITestSetFolder tsFolder;
            try
            {
                tsFolder = (ITestSetFolder)tsTreeManager.get_NodeByPath(testset);
            }
            catch
            {
                return null;
            }
            return tsFolder;
        }

        /// <summary>
        /// gets test index given it's name
        /// </summary>
        /// <param name="strName"></param>
        /// <param name="results"></param>
        /// <returns></returns>
        public int GetIdxByTestName(string strName, TestSuiteRunResults results)
        {
            TestRunResults res = null;
            int retVal = -1;

            for (int i = 0; i < results.TestRuns.Count(); ++i)
            {
                res = results.TestRuns[i];

                if (res != null && res.TestName == strName)
                {
                    retVal = i;
                    break;
                }
            }
            return retVal;
        }

        /// <summary>
        /// returns a description of the failure
        /// </summary>
        /// <param name="p_Test"></param>
        /// <returns></returns>
        private string GenerateFailedLog(IRun p_Test)
        {
            try
            {
                if (p_Test.StepFactory is not StepFactory sf)
                    return string.Empty;
                if (sf.NewList(string.Empty) is not IList stepList)
                    return string.Empty;

                string l_szFailedMessage = string.Empty;

                //' loop on each step in the steps
                foreach (IStep s in stepList)
                {
                    if (s.Status == "Failed")
                        l_szFailedMessage += s["ST_DESCRIPTION"] + "'\n\r";
                }
                return l_szFailedMessage;
            }
            catch
            {
                return string.Empty;
            }
        }


        /// <summary>
        /// writes a summary of the test run after it's over
        /// </summary>
        /// <param name="prevTest"></param>
        private string GetTestInstancesString(ITestSet set)
        {
            string retVal = string.Empty;
            try
            {
                TSTestFactory factory = set.TSTestFactory;
                List list = factory.NewList(string.Empty);

                if (list == null)
                    return string.Empty;

                foreach (ITSTest testInstance in list)
                {
                    retVal += testInstance.ID + ",";
                }
                retVal.TrimEnd(", \n".ToCharArray());
            }
            catch
            { }
            return retVal;
        }

        /// <summary>
        /// runs a test set with given parameters (and a valid connection to the QC server)
        /// </summary>
        /// <param name="tsFolderName">testSet folder name</param>
        /// <param name="tsName">testSet name</param>
        /// <param name="timeout">-1 for unlimited, or number of miliseconds</param>
        /// <param name="runMode">run on LocalMachine or remote</param>
        /// <param name="runHost">if run on remote machine - remote machine name</param>
        /// <returns></returns>
        public TestSuiteRunResults RunTestSet(string tsFolderName, string tsName, double timeout, QcRunMode runMode, string runHost, TestStorageType testStorageType)
        {
            string currentTestSetInstances = string.Empty;
            TestSuiteRunResults runDesc = new TestSuiteRunResults();
            ITestSetTreeManager tsTreeManager;

            if (TdConnection != null)
            {
                _ = _tdConnection.TestSetFactory;
                tsTreeManager = (ITestSetTreeManager)_tdConnection.TestSetTreeManager;
            }
            else
            {
                _ = _tdConnectionOld.TestSetFactory;
                tsTreeManager = (ITestSetTreeManager)_tdConnectionOld.TestSetTreeManager;
            }

            string tsPath = $"Root\\{tsFolderName}";
            ITestSetFolder tsFolder;
            try
            {
                tsFolder = (ITestSetFolder)tsTreeManager.get_NodeByPath(tsPath);
            }
            catch (COMException)
            {
                //not found
                tsFolder = null;
            }

            List tsList;
            if (tsFolder == null)
            {
                //node wasn't found, folder = null
                ConsoleWriter.WriteErrLine(string.Format(Resources.AlmRunnerNoSuchFolder, tsFolder));

                //this will make sure run will fail at the end. (since there was an error)
                Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                return null;
            }
            else
            {
                tsList = tsFolder.FindTestSets(tsName);
            }
            if (tsList == null)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerCantFindTest, tsName));

                //this will make sure run will fail at the end. (since there was an error)
                Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                return null;
            }
            ITestSet targetTestSet = null;
            foreach (ITestSet ts in tsList)
            {
                if (ts.Name.Equals(tsName, StringComparison.InvariantCultureIgnoreCase))
                {
                    targetTestSet = ts;
                    break;
                }
            }

            if (targetTestSet == null)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerCantFindTest, tsName));

                //this will make sure run will fail at the end. (since there was an error)
                Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                return null;
            }


            ConsoleWriter.WriteLine(Resources.GeneralDoubleSeperator);
            ConsoleWriter.WriteLine(Resources.AlmRunnerStartingExecution);
            ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerDisplayTest, tsName, targetTestSet.ID));

            ITSScheduler scheduler;
            try
            {
                //need to run this to install everyhting needed http://AlmServer:8080/qcbin/start_a.jsp?common=true
                //start the scheduler
                scheduler = targetTestSet.StartExecution(string.Empty);
            }
            catch
            {
                scheduler = null;
            }
            try
            {
                currentTestSetInstances = GetTestInstancesString(targetTestSet);
            }
            catch { }

            if (scheduler == null)
            {
                Console.WriteLine(GetAlmNotInstalledError());

                //proceeding with program execution is tasteless, since nothing will run without a properly installed QC.
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            TSTestFactory tsTestFactory = targetTestSet.TSTestFactory;
            ITDFilter2 tdFilter = tsTestFactory.Filter;
            tdFilter["TC_CYCLE_ID"] = targetTestSet.ID.ToString();

            IList tList = tsTestFactory.NewList(tdFilter.Text);
            try
            {
                //set up for the run depending on where the test instances are to execute
                switch (runMode)
                {
                    case QcRunMode.RUN_LOCAL:
                        // run all tests on the local machine
                        scheduler.RunAllLocally = true;
                        break;
                    case QcRunMode.RUN_REMOTE:
                        // run tests on a specified remote machine
                        scheduler.TdHostName = runHost;
                        break;
                    // RunAllLocally must not be set for remote invocation of tests. As such, do not do this: Scheduler.RunAllLocally = False
                    case QcRunMode.RUN_PLANNED_HOST:
                        // run on the hosts as planned in the test set
                        scheduler.RunAllLocally = false;
                        break;
                }
            }
            catch (Exception ex)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerProblemWithHost, ex.Message));
            }

            ConsoleWriter.WriteLine($"{Resources.AlmRunnerNumTests} {tList.Count}");

            int i = 1;
            foreach (ITSTest3 test in tList)
            {
                string runOnHost = runHost;
                if (runMode == QcRunMode.RUN_PLANNED_HOST)
                    runOnHost = test.HostName;

                //if host isn't taken from QC (PLANNED) and not from the test definition (REMOTE), take it from LOCAL (machineName)
                string hostName = runOnHost;
                if (runMode == QcRunMode.RUN_LOCAL)
                {
                    hostName = Environment.MachineName;
                }
                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerDisplayTestRunOnHost, i, test.Name, hostName));

                scheduler.RunOnHost[test.ID] = runOnHost;

                var testResults = new TestRunResults
                {
                    TestName = test.Name
                };
                runDesc.TestRuns.Add(testResults);

                i++;
            }

            Stopwatch sw = Stopwatch.StartNew();
            try
            {
                //tests are actually run
                scheduler.Run();
            }
            catch (Exception ex)
            {
                ConsoleWriter.WriteLine(Resources.AlmRunnerRunError + ex.Message);
            }

            ConsoleWriter.WriteLine(Resources.AlmRunnerSchedStarted + DateTime.Now.ToString(Launcher.DateFormat));
            ConsoleWriter.WriteLine(Resources.SingleSeperator);
            IExecutionStatus executionStatus = scheduler.ExecutionStatus;
            bool tsExecutionFinished = false;
            ITSTest prevTest = null;
            string abortFilename = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\stop" + Launcher.UniqueTimeStamp + ".txt";

            if (testStorageType == TestStorageType.AlmLabManagement)
            {
                timeout *= 60;
            }

            ITSTest currentTest;
            TestRunResults activeTestDesc;
            //wait for the tests to end ("normally" or because of the timeout)
            while ((tsExecutionFinished == false) && (timeout == -1 || sw.Elapsed.TotalSeconds < timeout))
            {
                executionStatus.RefreshExecStatusInfo("all", true);
                tsExecutionFinished = executionStatus.Finished;

                if (File.Exists(abortFilename))
                {
                    break;
                }

                for (int j = 1; j <= executionStatus.Count; ++j)
                {
                    TestExecStatus testExecStatusObj = executionStatus[j];

                    activeTestDesc = UpdateTestStatus(runDesc, targetTestSet, testExecStatusObj, true);

                    if (activeTestDesc.PrevTestState != activeTestDesc.TestState)
                    {
                        TestState tstate = activeTestDesc.TestState;
                        if (tstate == TestState.Running)
                        {
                            currentTest = targetTestSet.TSTestFactory[testExecStatusObj.TSTestId];
                            int testIndex = GetIdxByTestName(currentTest.Name, runDesc);

                            int prevRunId = GetTestRunId(currentTest);
                            runDesc.TestRuns[testIndex].PrevRunId = prevRunId;

                            //closing previous test
                            if (prevTest != null)
                            {
                                WriteTestRunSummary(prevTest);
                            }

                            //starting new test
                            prevTest = currentTest;

                            //assign the new test the consol writer so it will gather the output

                            ConsoleWriter.ActiveTestRun = runDesc.TestRuns[testIndex];

                            ConsoleWriter.WriteLine($"{DateTime.Now.ToString(Launcher.DateFormat)} Running: {currentTest.Name}");

                            //tell user that the test is running
                            ConsoleWriter.WriteLine($"{DateTime.Now.ToString(Launcher.DateFormat)} Running test: {activeTestDesc.TestName}, Test id: {testExecStatusObj.TestId}, Test instance id: {testExecStatusObj.TSTestId}");

                            //start timing the new test run
                            string foldername = string.Empty;

                            if (targetTestSet.TestSetFolder is ITestSetFolder folder)
                                foldername = folder.Name.Replace(".", "_");

                            //the test group is it's test set. (dots are problematic since jenkins parses them as seperators between packadge and class)
                            activeTestDesc.TestGroup = $"{foldername}\\{targetTestSet.Name}".Replace(".", "_");
                        }

                        TestState enmState = GetTsStateFromQcState(testExecStatusObj.Status);
                        string statusString = enmState.ToString();

                        if (enmState == TestState.Running)
                        {
                            ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerStat, activeTestDesc.TestName, testExecStatusObj.TSTestId, statusString));
                        }
                        else if (enmState != TestState.Waiting)
                        {
                            ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerStatWithMessage, activeTestDesc.TestName, testExecStatusObj.TSTestId, statusString, testExecStatusObj.Message));
                        }
                        if (File.Exists(abortFilename))
                        {
                            break;
                        }
                    }
                }

                //wait 0.2 seconds
                Thread.Sleep(200);

                //check for abortion
                if (File.Exists(abortFilename))
                {
                    _blnRunCancelled = true;

                    ConsoleWriter.WriteLine(Resources.GeneralStopAborted);

                    //stop all test instances in this testSet.
                    scheduler.Stop(currentTestSetInstances);

                    ConsoleWriter.WriteLine(Resources.GeneralAbortedByUser);

                    //stop working 
                    Environment.Exit((int)Launcher.ExitCodeEnum.Aborted);
                }
            }

            //check status for each test
            if (timeout == -1 || sw.Elapsed.TotalSeconds < timeout)
            {
                //close last test
                if (prevTest != null)
                {
                    WriteTestRunSummary(prevTest);
                }

                //done with all tests, stop collecting output in the testRun object.
                ConsoleWriter.ActiveTestRun = null;
                for (int k = 1; k <= executionStatus.Count; ++k)
                {
                    if (File.Exists(abortFilename))
                    {
                        break;
                    }

                    TestExecStatus testExecStatusObj = executionStatus[k];
                    activeTestDesc = UpdateTestStatus(runDesc, targetTestSet, testExecStatusObj, false);

                    UpdateCounters(activeTestDesc, runDesc);

                    //currentTest = targetTestSet.TSTestFactory[testExecStatusObj.TSTestId];

                    string testPath = $"Root\\{tsFolderName}\\{tsName}\\{activeTestDesc.TestName}";

                    activeTestDesc.TestPath = testPath;
                }

                //update the total runtime
                runDesc.TotalRunTime = sw.Elapsed;

                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerTestsetDone, tsName, DateTime.Now.ToString(Launcher.DateFormat)));
            }
            else
            {
                _blnRunCancelled = true;
                ConsoleWriter.WriteLine(Resources.GeneralTimedOut);
                Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
            }

            return runDesc;
        }

        /// <summary>
        /// writes a summary of the test run after it's over
        /// </summary>
        /// <param name="prevTest"></param>
        private void WriteTestRunSummary(ITSTest prevTest)
        {
            int prevRunId = ConsoleWriter.ActiveTestRun.PrevRunId;

            if (TdConnection != null)
            {
                _tdConnection.KeepConnection = true;
            }
            else
            {
                _tdConnectionOld.KeepConnection = true;
            }

            int runid = GetTestRunId(prevTest);
            if (runid > prevRunId)
            {
                string strSteps = GetTestStepsDescFromQc(prevTest);

                if (strSteps.IsNullOrWhiteSpace() && ConsoleWriter.ActiveTestRun.TestState != TestState.Error)
                    strSteps = GetTestRunLog(prevTest);

                if (!strSteps.IsNullOrWhiteSpace())
                    ConsoleWriter.WriteLine(strSteps);

                string linkStr = GetTestRunLink(prevTest, runid);

                ConsoleWriter.WriteLine("\n" + string.Format(Resources.AlmRunnerDisplayLink, linkStr));
            }
            ConsoleWriter.WriteLine($"{DateTime.Now.ToString(Launcher.DateFormat)} {Resources.AlmRunnerTestCompleteCaption} {prevTest.Name}"
                + (runid > prevRunId ? $", {Resources.AlmRunnerRunIdCaption} {runid}" : string.Empty)
                + "\n-------------------------------------------------------------------------------------------------------");
        }

        /// <summary>
        /// gets a link string for the test run in Qc
        /// </summary>
        /// <param name="prevTest"></param>
        /// <param name="runid"></param>
        /// <returns></returns>
        private string GetTestRunLink(ITSTest prevTest, int runid)
        {
            bool oldQc = CheckIsOldQc();
            ITestSet set = prevTest.TestSet;
            string link = $"td://{m_qcProject}.{m_qcDomain}.{m_qcServer.Replace("http://", string.Empty)}/TestLabModule-000000003649890581?EntityType=IRun&EntityID={runid}";
            string linkQc10 = $"td://{m_qcProject}.{m_qcDomain}.{m_qcServer.Replace("http://", string.Empty)}/Test%20Lab?Action=FindRun&TestSetID={set.ID}&TestInstanceID={prevTest.ID}&RunID={runid}";
            return oldQc ? linkQc10 : link;
        }

        private string GetAlmNotInstalledError()
        {
            return $"Could not create scheduler, please verify ALM client installation on run machine by downloading and installing the add-in from: {m_qcServer}/TDConnectivity_index.html";
        }

        /// <summary>
        /// summerizes test steps after test has run
        /// </summary>
        /// <param name="test"></param>
        /// <returns>a string containing descriptions of step states and messags</returns>
        string GetTestStepsDescFromQc(ITSTest test)
        {
            StringBuilder sb = new();
            try
            {
                //get runs for the test
                RunFactory rfactory = test.RunFactory;
                List runs = rfactory.NewList(string.Empty);
                if (runs.Count == 0)
                    return string.Empty;

                //get steps from run
                StepFactory stepFact = runs[runs.Count].StepFactory;
                List steps = stepFact.NewList(string.Empty);
                if (steps.Count == 0)
                    return string.Empty;

                //go over steps and format a string
                foreach (IStep step in steps)
                {
                    sb.Append($"Step: {step.Name}");

                    if (!step.Status.IsNullOrWhiteSpace())
                        sb.Append($", Status: {step.Status}");

                    string desc = step["ST_DESCRIPTION"] as string;
                    if (!desc.IsNullOrEmpty())
                    {
                        desc = $"\n\t{desc.Trim().Replace("\n", "\t").Replace("\r", string.Empty)}";
                        if (!desc.IsNullOrWhiteSpace())
                            sb.AppendLine(desc);
                    }
                }
            }
            catch (Exception ex)
            {
                sb.AppendLine($"Exception while reading step data: {ex.Message}");
            }
            return sb.ToString().TrimEnd();
        }

        private void UpdateCounters(TestRunResults test, TestSuiteRunResults testSuite)
        {
            if (!test.TestState.In(TestState.Running, TestState.Waiting, TestState.Unknown))
                ++testSuite.NumTests;

            if (test.TestState == TestState.Failed)
                ++testSuite.NumFailures;

            if (test.TestState == TestState.Error)
                ++testSuite.NumErrors;
        }

        /// <summary>
        /// translate the qc states into a state enum
        /// </summary>
        /// <param name="qcTestStatus"></param>
        /// <returns></returns>
        private TestState GetTsStateFromQcState(string qcTestStatus)
        {
            if (qcTestStatus == null)
                return TestState.Unknown;
            switch (qcTestStatus)
            {
                case "Waiting":
                    return TestState.Waiting;
                case "Error":
                    return TestState.Error;
                case "No Run":
                    return TestState.NoRun;
                case "Running":
                case "Connecting":
                    return TestState.Running;
                case "Success":
                case "Finished":
                case "FinishedPassed":
                    return TestState.Passed;
                case "FinishedFailed":
                    return TestState.Failed;
                default:
                    break;
            }
            return TestState.Unknown;
        }

        /// <summary>
        /// updates the test status in our list of tests
        /// </summary>
        /// <param name="targetTestSet"></param>
        /// <param name="testExecStatusObj"></param>
        private TestRunResults UpdateTestStatus(TestSuiteRunResults runResults, ITestSet targetTestSet, TestExecStatus testExecStatusObj, bool onlyUpdateState)
        {
            TestRunResults qTest = null;
            ITSTest currentTest = null;
            try
            {
                //find the test for the given status object
                currentTest = targetTestSet.TSTestFactory[testExecStatusObj.TSTestId];

                if (currentTest == null)
                    return null;

                //find the test in our list
                int testIndex = GetIdxByTestName(currentTest.Name, runResults);
                qTest = runResults.TestRuns[testIndex];

                qTest.TestType ??= GetTestType(currentTest);

                //update the state
                qTest.PrevTestState = qTest.TestState;
                qTest.TestState = GetTsStateFromQcState(testExecStatusObj.Status);

                if (!onlyUpdateState)
                {
                    try
                    {
                        //duration and status are updated according to the run
                        qTest.Runtime = TimeSpan.FromSeconds(currentTest.LastRun.Field("RN_DURATION"));
                    }
                    catch
                    {
                        //a problem getting duration, maybe the test isn't done yet - don't stop the flow..
                    }

                    switch (qTest.TestState)
                    {
                        case TestState.Failed:
                            qTest.FailureDesc = GenerateFailedLog(currentTest.LastRun);

                            if (string.IsNullOrWhiteSpace(qTest.FailureDesc))
                                qTest.FailureDesc = $"{testExecStatusObj.Status} : {testExecStatusObj.Message}";
                            break;
                        case TestState.Error:
                            qTest.ErrorDesc = $"{testExecStatusObj.Status} : {testExecStatusObj.Message}";
                            break;
                        case TestState.Warning:
                            qTest.HasWarnings = true;
                            break;
                        default:
                            break;
                    }

                    int runid = GetTestRunId(currentTest);
                    string linkStr = GetTestRunLink(currentTest, runid);

                    string statusString = GetTsStateFromQcState(testExecStatusObj.Status).ToString();
                    ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerTestStat, currentTest.Name, statusString, testExecStatusObj.Message, linkStr));
                    runResults.TestRuns[testIndex] = qTest;
                }
            }
            catch (Exception ex)
            {
                ConsoleWriter.WriteLine(string.Format(Resources.AlmRunnerErrorGettingStat, currentTest.Name, ex.Message));
            }

            return qTest;
        }

        /// <summary>
        /// gets the runId for the given test
        /// </summary>
        /// <param name="currentTest">a test instance</param>
        /// <returns>the run id</returns>
        private static int GetTestRunId(ITSTest currentTest)
        {
            int runid = -1;
            if (currentTest.LastRun is IRun lastrun)
                runid = lastrun.ID;
            return runid;
        }

        /// <summary>
        /// retrieves the run logs for the test when the steps are not reported to Qc (like in ST)
        /// </summary>
        /// <param name="currentTest"></param>
        /// <returns>the test run log</returns>
        private string GetTestRunLog(ITSTest currentTest)
        {
            string TestLog = "log\\vtd_user.log";

            string retVal = string.Empty;
            if (currentTest.LastRun is IRun lastrun)
            {
                try
                {
                    IExtendedStorage storage = lastrun.ExtendedStorage as IExtendedStorage;

                    var path = storage.LoadEx(TestLog, true, out List _, out bool wasFatalError);
                    string logPath = Path.Combine(path, TestLog);

                    if (File.Exists(logPath))
                    {
                        retVal = File.ReadAllText(logPath).TrimEnd();
                    }
                }
                catch
                {
                    retVal = string.Empty;
                }
            }
            retVal = ConsoleWriter.FilterXmlProblematicChars(retVal);
            return retVal;
        }

        /// <summary>
        /// checks Qc version (used for link format, 10 and smaller is old) 
        /// </summary>
        /// <returns>true if this QC is an old one</returns>
        private bool CheckIsOldQc()
        {
            TdConnection.GetTDVersion(out string ver, out _);
            bool oldQc = false;
            if (ver != null)
            {
                int.TryParse(ver, out int intver);
                if (intver <= 10)
                    oldQc = true;
            }
            else
            {
                oldQc = true;
            }
            return oldQc;
        }

        /// <summary>
        /// gets the type for a QC test
        /// </summary>
        /// <param name="currentTest"></param>
        /// <returns></returns>
        private string GetTestType(dynamic currentTest)
        {
            string ttype = currentTest.Test.Type;
            if (ttype.ToUpper() == "SERVICE-TEST")
            {
                ttype = TestType.ST.ToString();
            }
            else
            {
                ttype = TestType.QTP.ToString();
            }
            return ttype;
        }

        /// <summary>
        /// connects to QC and logs in
        /// </summary>
        /// <param name="QCServerURL"></param>
        /// <param name="QCLogin"></param>
        /// <param name="QCPass"></param>
        /// <param name="QCDomain"></param>
        /// <param name="QCProject"></param>
        /// <returns></returns>
        public bool ConnectToProject(string QCServerURL, string QCLogin, string QCPass, string QCDomain, string QCProject, bool SSOEnabled, string qcClientID, string qcApiKey)
        {
            if (QCServerURL.IsNullOrWhiteSpace()
                || (QCLogin.IsNullOrWhiteSpace() && !SSOEnabled)
                || QCDomain.IsNullOrWhiteSpace()
                || QCProject.IsNullOrWhiteSpace()
                || (SSOEnabled && (qcClientID.IsNullOrWhiteSpace()
                || qcApiKey.IsNullOrWhiteSpace())))
            {
                ConsoleWriter.WriteLine(Resources.AlmRunnerConnParamEmpty);
                return false;
            }

            if (TdConnection != null)
            {
                try
                {
                    if (!SSOEnabled)
                    {
                       TdConnection.InitConnectionEx(QCServerURL);
                    }
                    else
                    {
                        TdConnection.InitConnectionWithApiKey(QCServerURL, qcClientID, qcApiKey);
                    }
                }
                catch (Exception ex)
                {
                    ConsoleWriter.WriteLine(ex.Message);
                }
                if (TdConnection.Connected)
                {
                    try
                    {
                        if (!SSOEnabled)
                        {
                            TdConnection.Login(QCLogin, QCPass);
                        }
                    }
                    catch (Exception ex)
                    {
                        ConsoleWriter.WriteLine(ex.Message);
                    }

                    if (TdConnection.LoggedIn)
                    {
                        try
                        {
                            TdConnection.Connect(QCDomain, QCProject);
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine(ex.Message);
                        }

                        if (TdConnection.ProjectConnected)
                        {
                            return true;
                        }

                        ConsoleWriter.WriteErrLine(Resources.AlmRunnerErrorConnectToProj);
                    }
                    else
                    {
                        ConsoleWriter.WriteErrLine(Resources.AlmRunnerErrorAuthorization);
                    }

                }
                else
                {
                    ConsoleWriter.WriteErrLine(string.Format(Resources.AlmRunnerServerUnreachable, QCServerURL));
                }

                return false;
            }
            else //older versions of ALM (< 12.60) 
            {
                try
                {
                    TdConnectionOld.InitConnectionEx(QCServerURL);
                }
                catch (Exception ex)
                {
                    ConsoleWriter.WriteLine(ex.Message);
                }

                if (TdConnectionOld.Connected)
                {
                    try
                    {
                        TdConnectionOld.Login(QCLogin, QCPass);
                    }
                    catch (Exception ex)
                    {
                        ConsoleWriter.WriteLine(ex.Message);
                    }

                    if (TdConnectionOld.LoggedIn)
                    {
                        try
                        {
                            TdConnectionOld.Connect(QCDomain, QCProject);
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine(ex.Message);
                        }

                        if (TdConnectionOld.ProjectConnected)
                        {
                            return true;
                        }

                        ConsoleWriter.WriteErrLine(Resources.AlmRunnerErrorConnectToProj);
                    }
                    else
                    {
                        ConsoleWriter.WriteErrLine(Resources.AlmRunnerErrorAuthorization);
                    }
                }
                else
                {
                    ConsoleWriter.WriteErrLine(string.Format(Resources.AlmRunnerServerUnreachable, QCServerURL));
                }

                return false;
            }
        }

        #region IDisposable Members

        public void Dispose(bool managed)
        {
            if (Connected)
            {
                if (TdConnection != null)
                {
                    _tdConnection.Disconnect();
                    Marshal.ReleaseComObject(_tdConnection);
                }
                else
                {
                    _tdConnectionOld.Disconnect();
                    Marshal.ReleaseComObject(_tdConnectionOld);
                }
            }
        }

        public override void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        #endregion
    }

    public class QCFailure
    {
        public string Name { get; set; }
        public string Desc { get; set; }
    }

    public enum QcRunMode
    {
        RUN_LOCAL,
        RUN_REMOTE,
        RUN_PLANNED_HOST
    }
}
