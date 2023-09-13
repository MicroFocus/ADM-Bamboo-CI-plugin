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

package com.adm.utils.loadrunner;

/**
 * Created by habash on 19/06/2017.
 */
public class LRConsts {

    //###########################################################################
    //########################      Bamboo Param Names   ########################
    //###########################################################################
    public static final String TESTS = "tests";
    public static final String TIMEOUT = "timeout";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String EXEC_TIMEOUT = "execTimeout";
    public static final String IGNORE_ERRORS = "ignoreErrors";
    //###########################################################################
    //########################      Bamboo Param Labels  ########################
    //###########################################################################
    public static final String LABEL_TESTS = "Tests";
    public static final String LABEL_TIMEOUT = "Timeout";
    public static final String LABEL_POLLING_INTERVAL = "Controller Polling Interval";
    public static final String LABEL_EXEC_TIMEOUT = "Scenario Execution Timeout";
    public static final String LABEL_IGNORE_ERRORS = "Errors to Ignore";
    //###########################################################################
    //########################           Errors          ########################
    //###########################################################################
    public static final String ERROR_TESTS = "Error: Please add the paths of the tests you would like to run";
    public static final String ERROR_TIMEOUT = "Error: Please add the timeout in full seconds";
    public static final String ERROR_POLLING_INTERVAL = "Error: Please add the polling interval of the controller in full seconds";
    public static final String ERROR_EXEC_TIMEOUT = "Error: Please add the scenario execution timeout in full seconds";
    public static final String ERROR_PARAM_FILE_CREATION = "Error: A problem occurred while creating the param file!";
    public static final String ERROR_RESOURCE_NOT_FOUND = "Error: The resource %s could not be found!";
    public static final String ERROR_LOAD_TEST_RUN_FAILED = "Error: Failed to run load test!";
    public static final String ERROR_RESULTS_DONT_EXIST = "Error: Results files missing";
    //###########################################################################
    //######################## LoadRunner Related Values ########################
    //###########################################################################
    public static final String DEFAULT_POLLING_INTERVAL = "30";
    public static final String DEFAULT_EXEC_TIMEOUT = "10";
    public static final String HP_TOOLS_LAUNCHER = "HpToolsLauncher.exe";
    public static final String HP_TOOLS_ABORTER = "HpToolsAborter.exe";
    public static final String HP_TOOLS_ANALYSIS = "LRAnalysisLauncher.exe";
    public static final String RESULTS_ZIP_NAME = "Results.zip";
    public static final String TOOLS_PATH = "/Tools";
    public static final String RAW_RESULTS_FOLDER = "LRR";
    public static final String LOAD_TEST_SLA_FILE = "SLA.xml";
    public static final String WLRUN_EXE_ABSOLUTE_NAME = "C:\\Program Files (x86)\\HPE\\LoadRunner\\bin\\Wlrun.exe";

    //###########################################################################
    //########################        Log Messages       ########################
    //###########################################################################
    public static final String LOG_PARAM_FILE_CREATION = "Param file was created successfully in folder %s";
    public static final String LOG_EXECUTABLE_COPIED = "Copied %s to the agent successfully";
    public static final String LOG_TEST_RUN_ABORTED = "Test run aborted by user. Stopping the run.";

    //###########################################################################
    //########################       Parameter File      ########################
    //###########################################################################
    public static final String PARAM_FILE_TEST = "Test";
    public static final String PARAM_FILE_TIMEOUT = "fsTimeout";
    public static final String PARAM_FILE_POLLING_INTERVAL = "controllerPollingInterval";
    public static final String PARAM_FILE_EXEC_TIMEOUT = "PerScenarioTimeOut";
    public static final String PARAM_FILE_IGNORE_ERRORS = "ignoreErrorStrings";
    public static final String PARAM_FILE_DATE_FORMAT = "dd-MM-yyyy_HH-mm-ss-SSS";
    public static final String PARAM_FILE_NAME = "props%s.txt";
    public static final String PARAM_FILE_RESULT_FILE_NAME = "Results%s.xml";
    public static final String PARAM_FILE_RESULT_FILE = "resultsFilename";
    public static final String PARAM_FILE_COMMAND_FLAG = "-paramfile";
    public static final String PARAM_FILE_RUNTYPE_KEY = "runType";
    public static final String PARAM_FILE_RUNTYPE_VALUE = "FileSystem";

    //###########################################################################
    //########################       Miscellaneous       ########################
    //###########################################################################
    public static final String TESTS_DELIMITER = "\n";
    public static final int TEST_RUN_INTERRUPTED = -1;
    public static final boolean ABORT_TEST = true;
    public static final int SUCCESS = 0;
    public static final String CAPABILITY_KEY = "LoadRunner";

}
