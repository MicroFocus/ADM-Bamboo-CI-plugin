package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.LauncherParamsBuilder;
import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.enums.AlmRunMode;
import com.adm.utils.uft.enums.RunType;
import com.adm.utils.uft.model.CdaDetails;
import com.adm.utils.uft.rest.RestClient;
import com.adm.utils.uft.result.ResultSerializer;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.adm.utils.uft.sdk.Args;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.RunManager;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.adm.bamboo.plugin.uft.results.TestResultHelper.collateTestResults;
import static com.adm.bamboo.plugin.uft.results.TestResultHelperAlm.AddALMArtifacts;

public class RunFromAlmLabManagementUftTask implements AbstractLauncherTask {
    private final TestCollationService testCollationService;
    private static I18nBean i18nBean;

    private final String LINK_SEARCH_FILTER = "run report for run id";

    public RunFromAlmLabManagementUftTask(final TestCollationService testCollationService, @NotNull final I18nBeanFactory i18nBeanFactory) {
        this.testCollationService = testCollationService;
        i18nBean = i18nBeanFactory.getI18nBean();
    }

    @Override
    public Properties getTaskProperties(TaskContext taskContext) throws Exception {
        final ConfigurationMap map = taskContext.getConfigurationMap();
        LauncherParamsBuilder builder = new LauncherParamsBuilder();

        builder.setRunType(RunType.ALM_LAB_MANAGEMENT);

        builder.setAlmServerUrl(map.get(UFTConstants.ALM_SERVER.getValue()));
        builder.setAlmUserName(map.get(UFTConstants.USER_NAME.getValue()));
        builder.setAlmPassword(map.get(UFTConstants.PASSWORD.getValue()));
        builder.setAlmDomain(map.get(UFTConstants.DOMAIN.getValue()));
        builder.setAlmProject(map.get(UFTConstants.PROJECT.getValue()));
        builder.setAlmRunMode(AlmRunMode.RUN_LOCAL);
        builder.setTestSetId(1, map.get(UFTConstants.TEST_ID_PARAM.getValue()));
        builder.setAlmTimeout(map.get(UFTConstants.DURATION_PARAM.getValue()));
        builder.setAlmRunHost("");

        CdaDetails cdaDetails = null;
        boolean useCda = BooleanUtils.toBoolean(map.get(UFTConstants.USE_SDA_PARAM.getValue()));
        if (useCda) {
            cdaDetails = new CdaDetails(map.get(UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue()),
                    map.get(UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue()),
                    map.get(UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue()));
        }

        if(cdaDetails != null){
            builder.setDeploymentAction(cdaDetails.getDeploymentAction());
            builder.setDeployedEvironmentName(cdaDetails.getDeployedEnvironmentName());
            builder.setDeprovisioningAction(cdaDetails.getDeprovisioningAction());
        }

        return builder.getProperties();
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        return AbstractLauncherTask.super.execute(taskContext);
    }

    @Override
    public Integer runTask(final File workingDirectory, final String launcherPath,
                           final String paramFile, final BuildLogger logger) throws IOException, InterruptedException {
        return AbstractLauncherTask.super.runTask(workingDirectory, launcherPath, paramFile, logger);
    }

    @Override
    public TaskResult collateResults(@NotNull TaskContext taskContext, Properties mergedProperties) {
        try {
            collateTestResults(testCollationService, taskContext);
            AddALMArtifacts(taskContext, null, LINK_SEARCH_FILTER, i18nBean);
            return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        } catch (Exception ex) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }

}
