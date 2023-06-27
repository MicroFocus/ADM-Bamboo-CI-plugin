/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.model.CdaDetails;

public class Args {
    private final String _url;
    private final String _almSSO;
    private final String _clientID;
    private final String _apiKeySecret;
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
            String almSSO,
            String clientID,
            String apiKeySecret,
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
        _almSSO = almSSO;
        _clientID = clientID;
        _apiKeySecret = apiKeySecret;
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

    public String getAlmSSO() {
        return _almSSO;
    }

    public String getClientId() {
        return _clientID;
    }

    public String getApiKeySecret() {
        return _apiKeySecret;
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
