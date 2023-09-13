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
