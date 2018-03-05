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

package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.AlmConfigParameter;
import com.adm.bamboo.plugin.uft.helpers.VariableService;
import com.adm.bamboo.plugin.uft.ui.AlmLabEnvPrepareUftTaskConfigurator;
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
import com.atlassian.bamboo.variable.VariableDefinitionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AlmLabEnvPrepareUftTask implements AbstractLauncherTask {
    private final VariableService variableService;

    public AlmLabEnvPrepareUftTask(VariableDefinitionManager variableDefinitionManager) {

        this.variableService = new VariableService(variableDefinitionManager);
    }

    @NotNull
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
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
                variableService.saveGlobalVariable(outputConfig, confId);
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
    public Properties getTaskProperties(TaskContext taskContext) throws Exception {
        return null;
    }

    @Override
    public TaskResult collateResults(@NotNull TaskContext taskContext, Properties mergedProperties) {
        return null;
    }
}
