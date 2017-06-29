package com.hpe.utils.loadrunner;

import com.atlassian.struts.DefaultTextProvider;
import com.atlassian.struts.TextProvider;

/**
 * Created by habash on 19/06/2017.
 */
public class LRConsts {

    public static TextProvider textProvider = new DefaultTextProvider();
    //###########################################################################
    //########################      Bamboo Param Names   ########################
    //###########################################################################
    public static final String TESTS = textProvider.getText("lr.param.name.tests");
    public static final String TIMEOUT = textProvider.getText("lr.param.name.timeout");
    public static final String POLLING_INTERVAL = textProvider.getText("lr.param.name.pollingInterval");
    public static final String EXEC_TIMEOUT = textProvider.getText("lr.param.name.execTimeout");
    public static final String IGNORE_ERRORS = textProvider.getText("lr.param.name.ignoreErrors");
    //###########################################################################
    //########################      Bamboo Param Labels  ########################
    //###########################################################################
    public static final String LABEL_TESTS = textProvider.getText("lr.param.label.tests");
    public static final String LABEL_TIMEOUT = textProvider.getText("lr.param.label.timeout");
    public static final String LABEL_POLLING_INTERVAL = textProvider.getText("lr.param.label.pollingInterval");
    public static final String LABEL_EXEC_TIMEOUT = textProvider.getText("lr.param.label.execTimeout");
    public static final String LABEL_IGNORE_ERRORS = textProvider.getText("lr.param.label.ignoreErrors");
    //###########################################################################
    //########################           Errors          ########################
    //###########################################################################
    public static final String ERROR_TESTS = textProvider.getText("lr.param.error.tests");
    public static final String ERROR_TIMEOUT = textProvider.getText("lr.param.error.timeout");
    public static final String ERROR_POLLING_INTERVAL = textProvider.getText("lr.param.error.pollingInterval");
    public static final String ERROR_EXEC_TIMEOUT = textProvider.getText("lr.param.error.execTimeout");
    public static final String ERROR_PARAM_FILE_CREATION = "Error: A problem occurred while creating the param file!";
    public static final String ERROR_RESOURCE_NOT_FOUND = "Error: The resource %s could not be found!";
    //###########################################################################
    //######################## LoadRunner Related Values ########################
    //###########################################################################
    public static final String DEFAULT_TIMEOUT = textProvider.getText("lr.param.value.timeout");
    public static final String DEFAULT_POLLING_INTERVAL = textProvider.getText("lr.param.value.pollingInterval");
    public static final String DEFAULT_EXEC_TIMEOUT = textProvider.getText("lr.param.value.execTimeout");
    public static final String HP_TOOLS_LAUNCHER = "HpToolsLauncher.exe";
    public static final String HP_TOOLS_ABORTER = "HpToolsAborter.exe";

    //###########################################################################
    //########################        Log Messages       ########################
    //###########################################################################
    public static final String LOG_PARAM_FILE_CREATION = "Param file was created successfully in folder %s";
    public static final String LOG_EXECUTABLE_COPIED = "Copied %s to the agent successfully";

    //###########################################################################
    //########################       Miscellaneous       ########################
    //###########################################################################
    public static final String TESTS_DELIMITER = "\n";
    public static final String PARAM_FILE_TEST = "Test";
    public static final String PARAM_FILE_TIMEOUT = "Timeout";
    public static final String PARAM_FILE_POLLING_INTERVAL = "ControllerPollingInterval";
    public static final String PARAM_FILE_EXEC_TIMEOUT = "ScenarioExecutionTimeout";
    public static final String PARAM_FILE_IGNORE_ERRORS = "IgnoreErrors";
    public static final String PARAM_FILE_DATE_FORMAT = "dd-MM-yyyy_HH-mm-ss-SSS";

}
