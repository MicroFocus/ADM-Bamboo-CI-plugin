/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
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
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.adm.utils.sv;

public class SVConstants {
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
    public static final String ARCHIVE = "archive";
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
    public static final String PREFIX = "    ";


    //###########################################################################
    //########################       Miscellaneous       ########################
    //###########################################################################
    public static final String TESTS_DELIMITER = "\n";
    public static final int TEST_RUN_INTERRUPTED = -1;
    public static final boolean ABORT_TEST = true;
    public static final int SUCCESS = 0;
    public static final String CAPABILITY_KEY = "Service Virtualization";
}
