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

package com.adm.utils.uft.model;

import java.util.List;

public class AutEnvironmentConfigModel {
    private String almServerUrl;
    private final String almUserName;
    private final String almPassword;
    private final String almDomain;
    private final String almProject;

    private final boolean useExistingAutEnvConf;

    private final String autEnvID;
    private final String autEnvConf;

    private final String pathToJsonFile;
    private List<AutEnvironmentParameterModel> autEnvironmentParameters;

    private String currentConfigID;

    public AutEnvironmentConfigModel(String almServerUrl,
                                     String almUserName,
                                     String almPassword,
                                     String almDomain,
                                     String almProject,
                                     boolean useExistingAutEnvConf,
                                     String autEnvID,
                                     String envConf,
                                     String pathToJsonFile,
                                     List<AutEnvironmentParameterModel> autEnvironmentParameters){
        this.almUserName = almUserName;
        this.almPassword = almPassword;
        this.almDomain = almDomain;
        this.almProject = almProject;
        this.useExistingAutEnvConf = useExistingAutEnvConf;
        this.autEnvID = autEnvID;
        this.autEnvConf = envConf;
        this.pathToJsonFile = pathToJsonFile;
        this.almServerUrl = almServerUrl;
        this.autEnvironmentParameters = autEnvironmentParameters;
    }

    public String getAlmServerUrl() {
        return almServerUrl;
    }

    public String getAlmUserName() {
        return almUserName;
    }

    public String getAlmPassword() {
        return almPassword;
    }

    public String getAlmDomain() {
        return almDomain;
    }

    public String getAlmProject() {
        return almProject;
    }

    public boolean isUseExistingAutEnvConf() {
        return useExistingAutEnvConf;
    }

    public String getAutEnvID() {
        return autEnvID;
    }

    public String getAutEnvConf() {
        return autEnvConf;
    }


    public List<AutEnvironmentParameterModel> getAutEnvironmentParameters() {
        return autEnvironmentParameters;
    }

    public String getPathToJsonFile() {
        return pathToJsonFile;
    }


    public String getCurrentConfigID() {
        return currentConfigID;
    }

    public void setCurrentConfigID(String currentConfigID) {
        this.currentConfigID = currentConfigID;
    }
}
