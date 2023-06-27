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

package com.adm.utils.uft.autenvironment;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.autenvironment.request.get.GetAutEnvironmentByIdOldApiRequest;
import com.adm.utils.uft.autenvironment.request.get.GetAutEnvironmentByIdRequest;
import com.adm.utils.uft.autenvironment.request.get.GetAutEnvironmentConfigurationByIdRequest;
import com.adm.utils.uft.autenvironment.request.post.CreateAutEnvConfRequest;
import com.adm.utils.uft.model.AutEnvironmentConfigModel;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AUTEnvironmentManager {

    public final static String ALM_AUT_ENVIRONMENT_CONFIGURATION_ID_FIELD = "id";

    private Logger logger;
    private Client client;

    public AUTEnvironmentManager(Client client, Logger logger) {

        this.client = client;
        this.logger = logger;
    }

    public String getParametersRootFolderIdByAutEnvId(String autEnvironmentId) {

        String parametersRootFolderId = null;
        Response response = new GetAutEnvironmentByIdRequest(client, autEnvironmentId).execute();
        /**
         * This if here for backward compatibility to ALM 11.52. After the support for version <
         * 12.00 will be removed this 'if' statement can be removed
         * **/
        if (!response.isOk() && response.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            response = new GetAutEnvironmentByIdOldApiRequest(client, autEnvironmentId).execute();
        }
        try {
            List<Map<String, String>> entities = XPathUtils.toEntities(response.toString());
            if (!response.isOk() || entities.size() != 1) {
                throw new SSEException(String.format(
                        "Failed to get AUT Environment with ID: [%s]",
                        autEnvironmentId), response.getFailure());
            }

            Map<String, String> autEnvironment = entities.get(0);
            parametersRootFolderId = //autEnvironment == null ? null : autEnvironment.get("parent-id");
                    autEnvironment == null ? null : autEnvironment.get("root-app-param-folder-id");
        } catch (Throwable e) {
            logger.log(String.format("Failed to parse response: %s", response));
        }

        return parametersRootFolderId;
    }

    public String createNewAutEnvironmentConfiguration(
            String autEnvironmentId,
            AutEnvironmentConfigModel autEnvironmentModel) {

        String newConfigurationName =
                autEnvironmentModel.isUseExistingAutEnvConf()
                        || StringUtils.isNullOrEmpty(autEnvironmentModel.getAutEnvConf())
                        ? createTempConfigurationName()
                        : autEnvironmentModel.getAutEnvConf();
        return createNewAutEnvironmentConfiguration(autEnvironmentId, newConfigurationName);

    }

    private String createNewAutEnvironmentConfiguration(
            String autEnvironmentId,
            String newAutEnvConfigurationName) {

        String newAutEnvironmentConfigurationId = null;
        Response response =
                new CreateAutEnvConfRequest(client, autEnvironmentId, newAutEnvConfigurationName).execute();
        if (!response.isOk()) {
            logger.log(String.format(
                    "Failed to create new AUT Environment Configuration named: [%s] for AUT Environment with id: [%s]",
                    newAutEnvConfigurationName,
                    autEnvironmentId));
            return null;
        }
        try {
            newAutEnvironmentConfigurationId =
                    XPathUtils.getAttributeValue(
                            response.toString(),
                            ALM_AUT_ENVIRONMENT_CONFIGURATION_ID_FIELD);
        } catch (Throwable e) {
            logger.log(String.format("Failed to parse response: %s", response));
        }

        return newAutEnvironmentConfigurationId;
    }

    public boolean shouldUseExistingConfiguration(AutEnvironmentConfigModel autEnvironmentModel) {

        return autEnvironmentModel.isUseExistingAutEnvConf()
                && isAutEnvironmentConfigurationExists(autEnvironmentModel.getAutEnvConf());

    }

    private boolean isAutEnvironmentConfigurationExists(String existingAutEnvConfId) {

        Response response =
                new GetAutEnvironmentConfigurationByIdRequest(client, existingAutEnvConfId).execute();
        if (!response.isOk() || XPathUtils.toEntities(response.toString()).size() != 1) {
            logger.log(String.format(
                    "Failed to get AUT Environment Configuration with ID: [%s]. Will try to create a new one",
                    existingAutEnvConfId));
            return false;
        }
        return true;

    }

    private String createTempConfigurationName() {

        return "Configuration_" + Calendar.getInstance().getTime().toString();
    }
}
