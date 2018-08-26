/*
 *
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
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
