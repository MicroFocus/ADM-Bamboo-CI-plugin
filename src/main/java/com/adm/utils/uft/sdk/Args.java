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

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.model.CdaDetails;

public class Args {
    private final String _url;
    private final String _domain;
    private final String _project;
    private final String _username;
    private final String _password;
    private final String _runType;
    private final String _entityId;
    private final String _duration;
    private final String _description;
    private final String _postRunAction;
    private final String _environmentConfigurationId;

    private final CdaDetails _cdaDetails;

    public Args(
            String url,
            String domain,
            String project,
            String username,
            String password,
            String runType,
            String entityId,
            String duration,
            String description,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {

        _url = url;
        _domain = domain;
        _project = project;
        _username = username;
        _password = password;
        _entityId = entityId;
        _runType = runType;
        _duration = duration;
        _description = description;
        _postRunAction = postRunAction;
        _environmentConfigurationId = environmentConfigurationId;
        _cdaDetails = cdaDetails;
    }

    public String getUrl() {

        return _url;
    }

    public String getDomain() {

        return _domain;
    }

    public String getProject() {

        return _project;
    }

    public String getUsername() {

        return _username;
    }

    public String getPassword() {

        return _password;
    }

    public String getEntityId() {

        return _entityId;
    }

    public String getRunType() {
        return _runType;
    }

    public String getDuration() {

        return _duration;
    }

    public String getDescription() {

        return _description;
    }

    public String getPostRunAction() {

        return _postRunAction;
    }

    public String getEnvironmentConfigurationId() {

        return _environmentConfigurationId;
    }

    public CdaDetails getCdaDetails() {

        return _cdaDetails;
    }
}
