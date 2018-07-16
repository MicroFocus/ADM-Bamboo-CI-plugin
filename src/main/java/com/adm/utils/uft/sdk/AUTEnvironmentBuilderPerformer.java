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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.autenvironment.AUTEnvironmentManager;
import com.adm.utils.uft.autenvironment.AUTEnvironmentParametersManager;
import com.adm.utils.uft.autenvironment.request.put.AUTEnvironmnentParameter;
import com.adm.utils.uft.model.AutEnvironmentConfigModel;
import com.adm.utils.uft.model.AutEnvironmentParameterModel;

import java.util.Collection;
import java.util.List;

public class AUTEnvironmentBuilderPerformer {
    private Client client;
    private Logger logger;
    private AutEnvironmentConfigModel model;

    public AUTEnvironmentBuilderPerformer(Client client, Logger logger, AutEnvironmentConfigModel model) {
        this.client = client;
        this.logger = logger;
        this.model = model;
    }

    public void start() throws Throwable {

        try {
            if (login()) {
                appendQCSessionCookies();
                performAutOperations();
            } else {
                throw new SSEException("Failed to login to ALM");
            }
        } catch (Throwable cause) {
            logger.log(String.format(
                    "Failed to update ALM AUT Environment. Cause: %s",
                    cause.getMessage()));
            throw cause;
        }
    }

    private boolean login() {

        boolean ret;
        try {
            ret =
                    new RestAuthenticator().login(
                            client,
                            model.getAlmUserName(),
                            model.getAlmPassword(),
                            logger);
        } catch (Throwable cause) {
            ret = false;
            logger.log(String.format(
                    "Failed login to ALM Server URL: %s. Exception: %s",
                    model.getAlmServerUrl(),
                    cause.getMessage()));
        }

        return ret;
    }

    private void appendQCSessionCookies() {

        Response response =
                client.httpPost(
                        client.build("rest/site-session"),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        if (!response.isOk()) {
            throw new SSEException("Cannot append QCSession cookies", response.getFailure());
        }
    }

    private void performAutOperations() {

        String autEnvironmentId = model.getAutEnvID();
        AUTEnvironmentManager autEnvironmentManager = new AUTEnvironmentManager(client, logger);
        String parametersRootFolderId = autEnvironmentManager.getParametersRootFolderIdByAutEnvId(autEnvironmentId);
        String autEnvironmentConfigurationId = getAutEnvironmentConfigurationId(autEnvironmentManager, autEnvironmentId);
        model.setCurrentConfigID(autEnvironmentConfigurationId);

        assignValuesToAutParameters(autEnvironmentConfigurationId, parametersRootFolderId);
    }

    private String getAutEnvironmentConfigurationId(
            AUTEnvironmentManager autEnvironmentManager,
            String autEnvironmentId) {

        String autEnvironmentConfigurationId =
                autEnvironmentManager.shouldUseExistingConfiguration(model)
                        ? model.getAutEnvConf()
                        : autEnvironmentManager.createNewAutEnvironmentConfiguration(autEnvironmentId, model);

        if (StringUtils.isNullOrEmpty(autEnvironmentConfigurationId)) {
            throw new SSEException("There's no AUT Environment Configuration in order to proceed");
        }
        return autEnvironmentConfigurationId;

    }

    private void assignValuesToAutParameters(
            String autEnvironmentConfigurationId,
            String parametersRootFolderId) {

        List<AutEnvironmentParameterModel> confParams = model.getAutEnvironmentParameters();
        if (confParams == null || confParams.size() == 0) {
            logger.log("There's no AUT Environment parameters to assign for this build...");
            return;
        }

        AUTEnvironmentParametersManager parametersManager = new AUTEnvironmentParametersManager(
                client,
                confParams,
                parametersRootFolderId,
                autEnvironmentConfigurationId,
                model.getPathToJsonFile(),
                logger);

        Collection<AUTEnvironmnentParameter> parametersToUpdate = parametersManager.getParametersToUpdate();
        parametersManager.updateParametersValues(parametersToUpdate);
    }
}
