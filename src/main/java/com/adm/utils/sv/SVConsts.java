package com.adm.utils.sv;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVConsts {
    //###########################################################################
    //########################      Bamboo Param Names   ########################
    //###########################################################################
    public static final String URL = "url";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String SERVICE_SELECTION = "serviceSelection";
    public static final String SERVICE_NAME_OR_ID = "serviceNameOrId";
    public static final String PROJECT_PATH = "projectPath";
    public static final String PROJECT_PASSWORD = "projectPassword";
    public static final String FORCE = "force";
    public static final String CONTINUE = "continue";
    public static final String FIRST_SUITABLE_AGENT_FALLBACK = "firstSuitableAgentFallback";
    public static final String TARGET_DIRECTORY = "targetDirectory";
    public static final String CLEAN_TARGET_DIRECTORY = "cleanTargetDirectory";
    public static final String SWITCH_SERVICE_TO_STANDBY = "switchServiceToStandBy";
    public static final String DATA_MODEL = "dataModel";
    public static final String DM_NAME_OR_ID = "dmNameOrId";
    public static final String PERFORMANCE_MODEL = "performanceModel";
    public static final String PM_NAME_OR_ID = "pmNameOrId";
    public static final String SERVICE_MODE = "serviceMode";

    public static final String ALL_SERVICES_FROM_PROJECT = "PROJECT";
    public static final String ALL_SERVICES_DEPLOYED_ON_SERVER = "ALL_DEPLOYED";
    public static final String SELECTED_SERVICE_ONLY = "SERVICE";

    public static final String DM_SPECIFIC = "BY_NAME";
    public static final String NONE_DATA_MODEL = "NONE";
    public static final String DM_DEFAULT = "DEFAULT";

    public static final String PM_SPECIFIC = "BY_NAME";
    public static final String NONE_PERFORMANCE_MODEL = "NONE";
    public static final String OFFLINE = "OFFLINE";
    public static final String DEFAULT_PERFORMANCE_MODEL = "DEFAULT";

    public static final String STAND_BY = "STAND_BY";
    public static final String SIMULATING = "SIMULATING";
    public static final String LEARNING = "LEARNING";


    //###########################################################################
    //########################      Bamboo Param Labels  ########################
    //###########################################################################
    public static final String LABEL_URL = "url";
    public static final String LABEL_USERNAME = "userName";
    public static final String LABEL_PASSWORD = "password";
    public static final String LABEL_SERVICE_SELECTION = "serviceSelection";
    public static final String LABEL_SERVICE_NAME_OR_ID = "serviceNameOrId";
    public static final String LABEL_PROJECT_PATH = "projectPath";
    public static final String LABEL_PROJECT_PASSWORD = "projectPassword";
    //###########################################################################
    //########################           Errors          ########################
    //###########################################################################
    public static final String ERROR_PROJECT_PATH = "Error: Project path cannot be empty";
    public static final String ERROR_URL = "Error: Management Endpoint URL cannot be empty";
    public static final String ERROR_SERVICE_NAME_OR_ID = "Error: Service name or id must be set";
    public static final String ERROR_TARGET_DIRECTORY = "Error: Target directory cannot be empty";
    public static final String ERROR_DM_NAME_OR_ID = "Error: Data model cannot be empty if 'Specific' model is selected";
    public static final String ERROR_PM_NAME_OR_ID = "Error: Performance model cannot be empty if 'Specific' model is selected";
    //###########################################################################
    //######################## Service virtualization Related Values ########################
    //###########################################################################


    //###########################################################################
    //########################        Log Messages       ########################
    //###########################################################################



    //###########################################################################
    //########################       Miscellaneous       ########################
    //###########################################################################
    public static final String TESTS_DELIMITER = "\n";
    public static final int TEST_RUN_INTERRUPTED = -1;
    public static final boolean ABORT_TEST = true;
    public static final int SUCCESS = 0;
    public static final String CAPABILITY_KEY = "Service Virtualization";
}
