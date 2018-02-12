package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.LauncherParamsBuilder;
import com.adm.utils.uft.enums.RunType;
import com.adm.bamboo.plugin.uft.results.TestResultHelperAlm;
import com.adm.utils.uft.enums.AlmRunMode;
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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.adm.bamboo.plugin.uft.results.TestResultHelper.collateTestResults;

public class RunFromAlmUftTask implements AbstractLauncherTask {
    private final I18nBean i18nBean;
    private final TestCollationService testCollationService;

    public RunFromAlmUftTask(final TestCollationService testCollationService, @NotNull final I18nBeanFactory i18nBeanFactory) {
        this.i18nBean = i18nBeanFactory.getI18nBean();
        this.testCollationService = testCollationService;
    }

    @Override
    public Properties getTaskProperties(final TaskContext taskContext) throws Exception {
        final String splitMarker = "\n";
        final ConfigurationMap map = taskContext.getConfigurationMap();

        LauncherParamsBuilder builder = new LauncherParamsBuilder();
        builder.setRunType(RunType.ALM);

        final String almServerPath = map.get(UFTConstants.ALM_SERVER.getValue());
        builder.setAlmServerUrl(almServerPath);

        builder.setAlmUserName(map.get(UFTConstants.USER_NAME.getValue()));
        builder.setAlmPassword(map.get(UFTConstants.PASSWORD.getValue()));
        builder.setAlmDomain(map.get(UFTConstants.DOMAIN.getValue()));
        builder.setAlmProject(map.get(UFTConstants.PROJECT.getValue()));

        String runMode = map.get(UFTConstants.RUN_MODE.getValue());
        if (runMode.equals(UFTConstants.RUN_LOCALLY_PARAMETER.getValue())) {
            builder.setAlmRunMode(AlmRunMode.RUN_LOCAL);
        } else if (runMode.equals(UFTConstants.RUN_ON_PLANNED_HOST_PARAMETER.getValue())) {
            builder.setAlmRunMode(AlmRunMode.RUN_PLANNED_HOST);
        } else if (runMode.equals(UFTConstants.RUN_REMOTELY_PARAMETER.getValue())) {
            builder.setAlmRunMode(AlmRunMode.RUN_REMOTE);
        }

        builder.setAlmRunHost(map.get(UFTConstants.TESTING_TOOL_HOST.getValue()));

        String timeout = map.get(UFTConstants.TIMEOUT.getValue());
        if (StringUtils.isEmpty(timeout)) {
            builder.setAlmTimeout(UFTConstants.DEFAULT_TIMEOUT.getValue());
        } else {
            builder.setAlmTimeout(timeout);
        }

        String almTestSets = map.get(UFTConstants.TESTS_PATH.getValue());
        if (!StringUtils.isEmpty(almTestSets)) {
            String[] testSetsArr = almTestSets.replaceAll("\r", "").split(splitMarker);
            int index = 1;
            for (String testSet : testSetsArr) {
                builder.setTestSet(index, testSet);
                index++;
            }
        } else {
            builder.setAlmTestSet("");
        }
        return builder.getProperties();
    }


    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
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
            File resultFile = new File(taskContext.getWorkingDirectory(), mergedProperties.get("resultsFilename").toString());
            TestResultHelperAlm.AddALMArtifacts(taskContext, resultFile, UFTConstants.LINK_SEARCH_FILTER.getValue(), i18nBean);
            return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        } catch (Exception ex) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }
}
