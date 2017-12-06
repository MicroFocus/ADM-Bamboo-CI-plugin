package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.AlmConfigParameter;
import com.adm.bamboo.plugin.uft.helpers.VariableService;
import com.adm.bamboo.plugin.uft.ui.AlmLabEnvPrepareUftTaskConfigurator;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.VariableDefinitionManager;
import com.hpe.application.automation.tools.common.StringUtils;
import com.hpe.application.automation.tools.common.model.AutEnvironmentConfigModel;
import com.hpe.application.automation.tools.common.model.AutEnvironmentParameterModel;
import com.hpe.application.automation.tools.common.model.AutEnvironmentParameterType;
import com.hpe.application.automation.tools.common.rest.RestClient;
import com.hpe.application.automation.tools.common.sdk.AUTEnvironmentBuilderPerformer;
import com.hpe.application.automation.tools.common.sdk.Logger;
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
