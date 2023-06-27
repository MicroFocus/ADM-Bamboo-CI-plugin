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

package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.ui.UploadApplicationUftTaskConfigurator;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.integration.HttpConnectionException;
import com.adm.utils.uft.integration.JobOperation;
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

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.adm.bamboo.plugin.uft.results.TestResultHelper.collateTestResults;
import static com.adm.utils.uft.TaskUtils.logErrorMessage;

public class UploadApplicationUftTask implements AbstractLauncherTask {
    private final I18nBean i18nBean;
    private final TestCollationService testCollationService;


    public UploadApplicationUftTask(@NotNull final TestCollationService testCollationService, @NotNull final I18nBeanFactory i18nBeanFactory) {
        this.i18nBean = i18nBeanFactory.getI18nBean();
        this.testCollationService = testCollationService;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final ConfigurationMap map = taskContext.getConfigurationMap();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        String mcServerUrl = map.get(UFTConstants.MC_SERVER_URL.getValue());
        String mcUserName = map.get(UFTConstants.MC_USERNAME.getValue());
        String mcPassword = map.get(UFTConstants.MC_PASSWORD.getValue());

        //proxy info
        String proxyAddress = null;
        String proxyUserName = null;
        String proxyPassword = null;

        boolean useProxy = BooleanUtils.toBoolean(map.get(UFTConstants.USE_PROXY.getValue()));
        if (useProxy) {
            proxyAddress = map.get(UFTConstants.PROXY_ADDRESS.getValue());
            Boolean specifyAuthentication = BooleanUtils.toBoolean(UFTConstants.SPECIFY_AUTHENTICATION.getValue());
            if (specifyAuthentication) {
                proxyUserName = map.get(UFTConstants.PROXY_USERNAME.getValue());
                proxyPassword = map.get(UFTConstants.PROXY_PASSWORD.getValue());
            }
        }

        JobOperation operation = new JobOperation(mcServerUrl, mcUserName, mcPassword, proxyAddress, proxyUserName, proxyPassword);

        List<String> applicationsPaths = UploadApplicationUftTaskConfigurator.fetchMCApplicationPathFromContext(map);

        if (applicationsPaths == null || applicationsPaths.size() == 0) {
            return logErrorMessage(i18nBean.getText("UploadApplicationTask.error.atLeastOneApp"), buildLogger, taskContext);
        }

        for (String path : applicationsPaths) {
            String appName = new File(path).getName();
            buildLogger.addBuildLogEntry(String.format(i18nBean.getText("UploadApplicationTask.msg.startUpload"), appName));

            String info = null;
            try {
                info = operation.upload(path);
            } catch (HttpConnectionException e) {
                return logErrorMessage(i18nBean.getText("UploadApplicationTask.error.failToConnectMCServer"), buildLogger, taskContext);
            } catch (FileNotFoundException e) {
                return logErrorMessage(String.format(i18nBean.getText("UploadApplicationTask.error.failToFindApp"), appName), buildLogger, taskContext);
            } catch (IOException e) {
                return logErrorMessage(i18nBean.getText("UploadApplicationTask.error.uploadApp"), buildLogger, taskContext);
            }

            if (StringUtils.isNullOrEmpty(info)) {
                return logErrorMessage(i18nBean.getText("UploadApplicationTask.error.unknownErr"), buildLogger, taskContext);
            }

            try {
                JSONObject jsonObject = (JSONObject) JSONValue.parseStrict(info);
                if ((boolean) jsonObject.get("error")) {
                    return logErrorMessage((String) jsonObject.get("message"), buildLogger, taskContext);
                }
            } catch (ParseException e) {
                return logErrorMessage(i18nBean.getText("UploadApplicationTask.error.unknownErr"), buildLogger, taskContext);
            }

            buildLogger.addBuildLogEntry(i18nBean.getText("UploadApplicationTask.msg.uploadSuccessful"));
        }

        if (applicationsPaths.size() > 1) {
            buildLogger.addBuildLogEntry(i18nBean.getText("UploadApplicationTask.msg.allUploadSuccessful"));
        }

        return collateResults(taskContext, null);
    }

    @Override
    public TaskResult collateResults(@NotNull final TaskContext taskContext, final Properties mergedProperties) {
        try {
            collateTestResults(testCollationService, taskContext);
            return TaskResultBuilder.newBuilder(taskContext).build();
        } catch (Exception ex) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }

    @Override
    public Properties getTaskProperties(TaskContext taskContext) throws Exception {
        return null;
    }
}
