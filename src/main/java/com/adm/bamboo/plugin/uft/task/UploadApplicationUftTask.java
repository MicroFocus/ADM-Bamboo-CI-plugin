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
import com.adm.bamboo.plugin.uft.ui.UploadApplicationUftTaskConfigurator;
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

        String mcServerUrl = map.get(UFTConstants.MC_SERVER_URL);
        String mcUserName = map.get(UFTConstants.MC_USERNAME);
        String mcPassword = map.get(UFTConstants.MC_PASSWORD);

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
            logErrorMessage(i18nBean.getText("UploadApplicationTask.error.atLeastOneApp"), buildLogger, taskContext);
        }

        for (String path : applicationsPaths) {
            String appName = new File(path).getName();
            buildLogger.addBuildLogEntry(i18nBean.getText("UploadApplicationTask.msg.startUpload" + appName));

            try {
                String info = operation.upload(path);
            } catch (HttpConnectionException e) {
                logErrorMessage(i18nBean.getText("UploadApplicationTask.error.failToConnectMCServer"), buildLogger, taskContext);
            } catch (FileNotFoundException e) {
                logErrorMessage(i18nBean.getText("UploadApplicationTask.error.failToFindApp"), buildLogger, taskContext);
            } catch (IOException e) {
                logErrorMessage(i18nBean.getText("UploadApplicationTask.error.uploadApp"), buildLogger, taskContext);
            }
            buildLogger.addBuildLogEntry(i18nBean.getText("UploadApplicationTask.msg.uploadSuccefull"));
        }

        if (applicationsPaths.size() > 1) {
            buildLogger.addBuildLogEntry(i18nBean.getText("UploadApplicationTask.msg.allUploadSuccefull"));
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
