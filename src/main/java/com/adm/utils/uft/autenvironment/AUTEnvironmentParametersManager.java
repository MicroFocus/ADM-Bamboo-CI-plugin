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

package com.adm.utils.uft.autenvironment;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.autenvironment.request.get.GetAutEnvFoldersByIdRequest;
import com.adm.utils.uft.autenvironment.request.get.GetParametersByAutEnvConfIdRequest;
import com.adm.utils.uft.autenvironment.request.put.AUTEnvironmnentParameter;
import com.adm.utils.uft.autenvironment.request.put.PutAutEnvironmentParametersBulkRequest;
import com.adm.utils.uft.model.AUTEnvironmentFolder;
import com.adm.utils.uft.model.AutEnvironmentParameterModel;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;
import com.adm.utils.uft.sdk.handler.JsonHandler;

import java.util.*;

public class AUTEnvironmentParametersManager {
    public final static String PARAMETER_PATH_DELIMITER = "/";

    private Logger logger;
    private Client client;

    private String parametersRootFolderId;
    private String autEnvironmentConfigurationId;

    private Map<String, AUTEnvironmnentParameter> parameters;
    private List<AutEnvironmentParameterModel> parametersToAssign;

    private String pathToJsonFile;

    public AUTEnvironmentParametersManager(
            Client client,
            List<AutEnvironmentParameterModel> parametersToAssign,
            String parametersRootFolderId,
            String autEnvironmentConfigurationId,
            String pathToJsonFile,
            Logger logger) {

        this.logger = logger;
        this.client = client;
        this.parametersToAssign = parametersToAssign;
        this.parametersRootFolderId = parametersRootFolderId;
        this.autEnvironmentConfigurationId = autEnvironmentConfigurationId;
        this.pathToJsonFile = pathToJsonFile;
    }

    public Collection<AUTEnvironmnentParameter> getParametersToUpdate() {

        parameters = getAllParametersByAutEnvConfId();
        Map<String, AUTEnvironmentFolder> parametersFolders = getAllRelevantParametersFolders();

        for (AUTEnvironmnentParameter parameter : parameters.values()) {
            parameter.setFullPath(parametersFolders.get(parameter.getParentId()).getPath()
                    + PARAMETER_PATH_DELIMITER
                    + parameter.getName());

        }

        resolveValuesOfParameters();
        return getResolvedParametersWithAssignedValues();
    }

    public void updateParametersValues(Collection<AUTEnvironmnentParameter> parametersToUpdate) {

        Response response =
                new PutAutEnvironmentParametersBulkRequest(client, parametersToUpdate).execute();
        if (!response.isOk()) {
            throw new SSEException(
                    String.format(
                            "Failed to update the parameters of AUT Environment Configuration with ID: [%s]",
                            autEnvironmentConfigurationId),
                    response.getFailure());
        }
        logger.log("Submitted all parameters to ALM");
    }

    private Map<String, AUTEnvironmnentParameter> getAllParametersByAutEnvConfId() {

        Map<String, AUTEnvironmnentParameter> parametersMap = new HashMap<String, AUTEnvironmnentParameter>();
        Response response =
                new GetParametersByAutEnvConfIdRequest(client, autEnvironmentConfigurationId).execute();
        if (!response.isOk()) {
            throw new SSEException(
                    String.format(
                            "Failed to retrieve the parameters of AUT Environment Configuration with ID: [%s]",
                            autEnvironmentConfigurationId),
                    response.getFailure());
        }

        List<Map<String, String>> parameters = XPathUtils.toEntities(response.toString());

        for (Map<String, String> parameter : parameters) {

            String id = parameter.get(AUTEnvironmnentParameter.ALM_PARAMETER_ID_FIELD);
            AUTEnvironmnentParameter param =
                    new AUTEnvironmnentParameter(
                            id,
                            parameter.get(AUTEnvironmnentParameter.ALM_PARAMETER_PARENT_ID_FIELD),
                            parameter.get(AUTEnvironmnentParameter.ALM_PARAMETER_NAME_FIELD));
            parametersMap.put(id, param);

        }

        return parametersMap;

    }

    private Map<String, AUTEnvironmentFolder> getAllRelevantParametersFolders() {

        Map<String, AUTEnvironmentFolder> parametersFolders =
                new HashMap<String, AUTEnvironmentFolder>();
        StringBuilder foldersToGet = new StringBuilder(parametersRootFolderId);

        for (AUTEnvironmnentParameter parameter : parameters.values()) {
            foldersToGet.append("%20OR%20" + parameter.getParentId());
        }

        Response response =
                new GetAutEnvFoldersByIdRequest(client, foldersToGet.toString()).execute();
        if (!response.isOk()) {
            throw new SSEException(
                    String.format(
                            "Failed to retrieve parameters folders of AUT Environment Configuration with ID: [%s]",
                            autEnvironmentConfigurationId),
                    response.getFailure());
        }

        List<Map<String, String>> folders = XPathUtils.toEntities(response.toString());

        for (Map<String, String> folder : folders) {

            String folderId = folder.get(AUTEnvironmentFolder.ALM_PARAMETER_FOLDER_ID_FIELD);
            if (!parametersFolders.containsKey(folderId)) {
                AUTEnvironmentFolder autEnvironmentFolder =
                        new AUTEnvironmentFolder(
                                folderId,
                                folder.get(AUTEnvironmentFolder.ALM_PARAMETER_FOLDER_PARENT_ID_FIELD),
                                folder.get(AUTEnvironmentFolder.ALM_PARAMETER_FOLDER_NAME_FIELD));
                parametersFolders.put(folderId, autEnvironmentFolder);

            }
        }

        for (AUTEnvironmentFolder folder : parametersFolders.values()) {
            calculatePaths(folder, parametersFolders);
        }
        return parametersFolders;
    }

    private String calculatePaths(
            AUTEnvironmentFolder folder,
            Map<String, AUTEnvironmentFolder> parametersFolders) {

        String calculatedPath;
        if (folder.getId().equals(parametersRootFolderId)) {
            calculatedPath = folder.getName();
        } else {
            calculatedPath =
                    StringUtils.isNullOrEmpty(folder.getPath())
                            ? calculatePaths(
                            parametersFolders.get(folder.getParentId()),
                            parametersFolders)
                            + PARAMETER_PATH_DELIMITER
                            + folder.getName() : folder.getPath();
        }

        folder.setPath(calculatedPath);
        return calculatedPath;
    }

    private void resolveValuesOfParameters() {

        boolean shouldLoadJsonObject = true;
        Object jsonObject = null;
        JsonHandler jsonHandler = new JsonHandler(logger);

        for (AutEnvironmentParameterModel parameter : parametersToAssign) {

            String resolvedValue = "";
            switch (parameter.getParamType()) {

                case ENVIRONMENT:
                    resolvedValue = parameter.getValue();
                    break;
                case EXTERNAL:
                    if (shouldLoadJsonObject) {
                        jsonObject = jsonHandler.load(pathToJsonFile);
                        shouldLoadJsonObject = false;
                    }
                    resolvedValue =
                            jsonHandler.getValueFromJsonAsString(
                                    jsonObject,
                                    parameter.getValue(),
                                    parameter.isShouldGetOnlyFirstValueFromJson());
                    break;
                case USER_DEFINED:
                    resolvedValue = parameter.getValue();
                    break;
                case UNDEFINED:
                    resolvedValue = "";
                    break;
            }

            parameter.setResolvedValue(resolvedValue);
        }

    }

    private Collection<AUTEnvironmnentParameter> getResolvedParametersWithAssignedValues() {

        Collection<AUTEnvironmnentParameter> valuesToReturn =
                new ArrayList<AUTEnvironmnentParameter>();
        for (AutEnvironmentParameterModel parameterByModel : parametersToAssign) {
            String parameterPathByModel = parameterByModel.getName();

            for (AUTEnvironmnentParameter parameter : parameters.values()) {
                if (parameterPathByModel.equalsIgnoreCase(parameter.getFullPath())) {
                    String resolvedValue = parameterByModel.getResolvedValue();
                    parameter.setValue(resolvedValue);
                    logger.log(String.format(
                            "Parameter: [%s] of type: [%s] will get the value: [%s] ",
                            parameter.getFullPath(),
                            parameterByModel.getParamType(),
                            resolvedValue));
                    valuesToReturn.add(parameter);
                    break;
                }
            }

        }
        logger.log(parametersToAssign.size() > 0
                ? "Finished assignment of values for all parameters"
                : "There was no parameters to assign");

        return valuesToReturn;
    }
}
