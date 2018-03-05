/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents (“Material”) accessible here may contain branding from
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
