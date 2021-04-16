/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.enums;

import java.util.HashMap;
import java.util.Map;

public enum UFTConstants {

    //Run from File System
    TASK_NAME_VALUE("RunFromFileSystemTask.taskName"),
    TESTS_PATH("testPathInput"),
    TIMEOUT("timeoutInput"),

    MC_SERVER_URL("mcServerURLInput"),
    MC_USERNAME("mcUserNameInput"),
    MC_PASSWORD("mcPasswordInput"),
    MC_APPLICATION_PATH("mcApplicationPathInput"),
    MC_APPLICATION_ID_KEY("mcApplicationIDKeyInput"),
    USE_MC_SETTINGS("useMC"),

    PUBLISH_MODE_ALWAYS("RunFromFileSystemTask.publishMode.always"),
    PUBLISH_MODE_FAILED("RunFromFileSystemTask.publishMode.failed"),
    PUBLISH_MODE_NEVER("RunFromFileSystemTask.publishMode.never"),
    ARTIFACT_NAME_FORMAT("RunFromFileSystemTask.artifactNameFormat"),
    PUBLISH_MODE_PARAM("publishMode"),
    PUBLISH_MODE_ITEMS("publishModeItems"),
    PUBLISH_MODE_ALWAYS_VALUE("always"),
    PUBLISH_MODE_FAILED_VALUE("failed"),
    PUBLISH_MODE_NEVER_VALUE("never"),

    //job
    JOB_UUID("jobUUID"),
    OS("OS"),
    DEVICE_ID("deviceId"),
    DEVICE_NAME("manufacturerAndModel"),
    SOURCE("targetLab"),
    EXTRA_APPS("extraApps"),
    LAUNCH_APP_NAME("launchApplicationName"),
    AUT_ACTIONS("autActions"),
    INSTRUMENTED("instrumented"),
    DEVICE_METRICS("deviceMetrics"),

    //SSL
    USE_SSL("useSSL"),

    //proxy info
    USE_PROXY("useProxy"),
    SPECIFY_AUTHENTICATION("specifyAuthentication"),

    PROXY_ADDRESS("proxyAddress"),
    PROXY_USERNAME("proxyUserName"),
    PROXY_PASSWORD("proxyPassword"),

    //MC info
    MC_INFO("mcInfo"),

    RESULT_HTML_REPORT_FILE_NAME("run_results.html"),
    HTML_REPORT_FILE_NAME("Report.html"),

    //Run from Alm server
    ALM_SERVER("almServer"),
    ALM_SSO("almSSO"),
    CLIENT_ID("clientID"),
    API_KEY_SECRET("apiKeySecret"),
    USER_NAME("userName"),
    PASSWORD("password"),
    DOMAIN("domain"),
    PROJECT("projectName"),
    RUN_MODE("runMode"),
    RUN_MODE_PARAMETER("runModeItems"),
    TESTING_TOOL_HOST("testingToolHost"),
    DEFAULT_TIMEOUT("-1"),
    RUN_LOCALLY_LBL("RunFromAlmTask.runLocallyLbl"),
    RUN_ON_PLANNED_HOST_LBL("RunFromAlmTask.runOnPlannedHostLbl"),
    RUN_REMOTELY_LBL("RunFromAlmTask.runRemotelyLbl"),
    RUN_LOCALLY_PARAMETER("1"),
    RUN_ON_PLANNED_HOST_PARAMETER("2"),
    RUN_REMOTELY_PARAMETER("3"),
    ALM_TASK_NAME("RunFromAlmTask.taskName"),

    //Run from Alm Lab Management
    DOMAIN_PARAM("domain"),
    PROJECT_NAME_PARAM("projectName"),
    RUN_TYPE_PARAM("runType"),
    TEST_ID_PARAM("testId"),
    DESCRIPTION_PARAM("description"),
    DURATION_PARAM("duration"),
    ENVIRONMENT_ID_PARAM("enviromentId"),
    USE_SDA_PARAM("useSda"),
    DEPLOYMENT_ACTION_PARAM("deploymentAction"),
    DEPLOYED_ENVIRONMENT_NAME("deployedEnvironmentName"),
    DEPROVISIONING_ACTION_PARAM("deprovisioningAction"),

    RUN_TYPE_ITEMS_PARAM("runTypeItems"),
    DEPLOYMENT_ACTION_ITEMS_PARAM("deploymentActionItems"),
    DEPROVISIONING_ACTION_ITEMS_PARAM("deprovisioningActionItems"),

    ALM_SERVER_REQUIRED_STRING("RunFromAlmLabManagementTask.almServer.required"),
    USER_NAME_REQUIRED_STRING("RunFromAlmLabManagementTask.userName.required"),
    DOMAIN_REQUIRED_STRING("RunFromAlmLabManagementTask.domain.required"),
    PROJECT_NAME_REQUIRED_STRING("RunFromAlmLabManagementTask.projectName.required"),
    TEST_ID_REQUIRED_STRING("RunFromAlmLabManagementTask.testId.required"),
    DURATION_REQUIRED_STRING("RunFromAlmLabManagementTask.duration.required"),
    DURATION_MINIMUM_STRING("RunFromAlmLabManagementTask.duration.minimum"),
    DURATION_INVALID_FORMAT_STRING("RunFromAlmLabManagementTask.duration.invalidFormat"),
    ALM_LAB_TASK_NAME("RunFromAlmLabManagementTask.taskName"),
    LINK_SEARCH_FILTER("EntityID="),

    //ALM Lab Environment Preparation
    USER_NAME_LAB_ENV("almUserName"),
    PASSWORD_LAB_ENV("almUserPassword"),
    PROJECT_LAB_ENV("almProject"),

    AUT_ENV_ID("AUTEnvID"),
    AUT_ENV_NEW_CONFIG_NAME("NewAUTConfName"),
    AUT_ENV_EXIST_CONFIG_ID("AUTConfName"),

    PATH_TO_JSON_FILE("pathToJSONFile"),
    ASSIGN_ENV_CONF_ID("assignAUTEnvConfIDto"),

    ENV_ALM_PARAMETERS_TYPE_ENV("ALMParamTypeEnv"),
    ENV_ALM_PARAMETERS_TYPE_JSON("ALMParamTypeJson"),
    ENV_ALM_PARAMETERS_TYPE_MAN("ALMParamTypeManual"),

    OUTPUT_CONFIG_ID("outEnvID"),

    //lists and maps for controls with collections
    ENV_ALM_CONFIGS_OPTION("ALMConfigOptions"),
    ENV_ALM_CONFIG_PATTERN_OPTION_NEW("ALMConfUseNew"),
    ENV_ALM_CONFIG_PATTERN_OPTION_EXIST("ALMConfUseExist"),

    ENV_ALM_PARAMETERS_TYPE("almParamTypes"),

    ENV_ALM_PARAMETERS_NAME("almParamName"),
    ENV_ALM_PARAMETERS_VALUE("almParamValue"),
    ENV_ALM_PARAMETERS_ONLYFIRST("almParamOnlyFirst"),

    //Common Task Configuration Properties
    TASK_NAME("taskName"),
    TOOLS_PATH("/Tools"),
    TASK_ID_LABEL("CommonTask.taskId"),
    ICON("icon"),

    //test status
    TEST_STATUS_FAIL("fail");

    private final String value;

    UFTConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}