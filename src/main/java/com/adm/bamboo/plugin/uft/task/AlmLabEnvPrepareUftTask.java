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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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

package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.AlmConfigParameter;
import com.adm.bamboo.plugin.uft.ui.AlmLabEnvPrepareUftTaskConfigurator;
import com.adm.utils.uft.Aes256Encryptor;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.model.AutEnvironmentConfigModel;
import com.adm.utils.uft.model.AutEnvironmentParameterModel;
import com.adm.utils.uft.model.AutEnvironmentParameterType;
import com.adm.utils.uft.rest.RestClient;
import com.adm.utils.uft.sdk.AUTEnvironmentBuilderPerformer;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.CustomVariableContext;

import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AlmLabEnvPrepareUftTask implements AbstractLauncherTask {
    private final CustomVariableContext customVariableContext;

    public AlmLabEnvPrepareUftTask(@ComponentImport CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }

    @Override
    public CustomVariableContext getCustomVariableContext() {
        return customVariableContext;
    }

    @NotNull
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final VariableContext variableContext = taskContext.getCommonContext().getVariableContext();
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        ConfigurationMap confMap = taskContext.getConfigurationMap();
        TaskState state = TaskState.SUCCESS;

        String domain = confMap.get(UFTConstants.DOMAIN.getValue());
        String project = confMap.get(UFTConstants.PROJECT_LAB_ENV.getValue());
        String userName = confMap.get(UFTConstants.USER_NAME_LAB_ENV.getValue());

        boolean useExistingAutEnvConf = AlmLabEnvPrepareUftTaskConfigurator.useExistingConfiguration(confMap);
        String configuration = useExistingAutEnvConf ?
                confMap.get(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue()) :
                confMap.get(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue());

        List<AutEnvironmentParameterModel> autEnvironmentParameters = new ArrayList<AutEnvironmentParameterModel>();
        for (AlmConfigParameter prm : AlmLabEnvPrepareUftTaskConfigurator.fetchAlmParametersFromContext(confMap)) {
            AutEnvironmentParameterType type = convertType(prm.getAlmParamSourceType());

            autEnvironmentParameters.add(
                    new AutEnvironmentParameterModel(prm.getAlmParamName(),
                            prm.getAlmParamValue(),
                            type,
                            prm.getAlmParamOnlyFirst()));
        }

        String almServerPath = confMap.get(UFTConstants.ALM_SERVER.getValue());

        RestClient restClient = new RestClient(
                almServerPath,
                domain,
                project,
                userName);


        AutEnvironmentConfigModel autEnvModel = new AutEnvironmentConfigModel(
                almServerPath,
                userName,
                confMap.get(UFTConstants.PASSWORD_LAB_ENV.getValue()),
                domain,
                project,
                useExistingAutEnvConf,
                confMap.get(UFTConstants.AUT_ENV_ID.getValue()),
                configuration,
                confMap.get(UFTConstants.PATH_TO_JSON_FILE.getValue()),
                autEnvironmentParameters);

        try {

            Logger logger = new Logger() {
                public void log(String message) {
                    buildLogger.addBuildLogEntry(message);
                }
            };

            AUTEnvironmentBuilderPerformer performer = new AUTEnvironmentBuilderPerformer(restClient, logger, autEnvModel);
            performer.start();

            String outputConfig = confMap.get(UFTConstants.OUTPUT_CONFIG_ID.getValue());

            if (!StringUtils.isNullOrEmpty(outputConfig)) {

                String confId = autEnvModel.getCurrentConfigID();
                variableContext.addResultVariable(outputConfig, confId);
            }

        } catch (InterruptedException e) {
            state = TaskState.ERROR;
        } catch (Throwable cause) {
            state = TaskState.FAILED;
        }

        TaskResultBuilder result = TaskResultBuilder.newBuilder(taskContext).setState(state);

        return result.build();
    }

    private AutEnvironmentParameterType convertType(String sourceType) {
        if (sourceType.equals(UFTConstants.ENV_ALM_PARAMETERS_TYPE_ENV.getValue()))
            return AutEnvironmentParameterType.ENVIRONMENT;

        if (sourceType.equals(UFTConstants.ENV_ALM_PARAMETERS_TYPE_JSON.getValue()))
            return AutEnvironmentParameterType.EXTERNAL;

        if (sourceType.equals(UFTConstants.ENV_ALM_PARAMETERS_TYPE_MAN.getValue()))
            return AutEnvironmentParameterType.USER_DEFINED;

        return AutEnvironmentParameterType.UNDEFINED;
    }

    @Override
    public Properties getTaskProperties(TaskContext taskContext) {
        return null;
    }

    @Override
    public TaskResult collateResults(@NotNull TaskContext taskContext, Properties mergedProperties) {
        return null;
    }

    @Override
    public Aes256Encryptor getAes256Encryptor() {
        return null;
    }
}
