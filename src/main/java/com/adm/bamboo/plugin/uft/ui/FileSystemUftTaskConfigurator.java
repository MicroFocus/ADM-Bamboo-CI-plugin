/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
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

package com.adm.bamboo.plugin.uft.ui;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTaskConfigurator;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class FileSystemUftTaskConfigurator extends AbstractLauncherTaskConfigurator {

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.TESTS_PATH.getValue(), params.getString(UFTConstants.TESTS_PATH.getValue()));
        config.put(UFTConstants.TIMEOUT.getValue(), params.getString(UFTConstants.TIMEOUT.getValue()));
        config.put(UFTConstants.USE_MC_SETTINGS.getValue(), params.getString(UFTConstants.USE_MC_SETTINGS.getValue()));
        config.put(UFTConstants.MC_SERVER_URL.getValue(), params.getString(UFTConstants.MC_SERVER_URL.getValue()));
        config.put(UFTConstants.MC_USERNAME.getValue(), params.getString(UFTConstants.MC_USERNAME.getValue()));
        config.put(UFTConstants.MC_PASSWORD.getValue(), params.getString(UFTConstants.MC_PASSWORD.getValue()));
        config.put(UFTConstants.MC_APPLICATION_PATH.getValue(), params.getString(UFTConstants.MC_APPLICATION_PATH.getValue()));
        config.put(UFTConstants.MC_APPLICATION_ID_KEY.getValue(), params.getString(UFTConstants.MC_APPLICATION_ID_KEY.getValue()));
        config.put(UFTConstants.PUBLISH_MODE_PARAM.getValue(), params.getString(UFTConstants.PUBLISH_MODE_PARAM.getValue()));
        config.put(UFTConstants.TASK_NAME.getValue(), getI18nBean().getText(UFTConstants.TASK_NAME_VALUE.getValue()));

        config.put(UFTConstants.JOB_UUID.getValue(), params.getString(UFTConstants.JOB_UUID.getValue()));

        config.put(UFTConstants.OS.getValue(), params.getString(UFTConstants.OS.getValue()));
        config.put(UFTConstants.DEVICE_ID.getValue(), params.getString(UFTConstants.DEVICE_ID.getValue()));
        config.put(UFTConstants.DEVICE_NAME.getValue(), params.getString(UFTConstants.DEVICE_NAME.getValue()));
        config.put(UFTConstants.SOURCE.getValue(), params.getString(UFTConstants.SOURCE.getValue()));
        config.put(UFTConstants.EXTRA_APPS.getValue(), params.getString(UFTConstants.EXTRA_APPS.getValue()));
        config.put(UFTConstants.LAUNCH_APP_NAME.getValue(), params.getString(UFTConstants.LAUNCH_APP_NAME.getValue()));
        config.put(UFTConstants.AUT_ACTIONS.getValue(), params.getString(UFTConstants.AUT_ACTIONS.getValue()));
        config.put(UFTConstants.INSTRUMENTED.getValue(), params.getString(UFTConstants.INSTRUMENTED.getValue()));
        config.put(UFTConstants.DEVICE_METRICS.getValue(), params.getString(UFTConstants.DEVICE_METRICS.getValue()));

        config.put(UFTConstants.USE_SSL.getValue(), params.getString(UFTConstants.USE_SSL.getValue()));

        config.put(UFTConstants.USE_PROXY.getValue(), params.getString(UFTConstants.USE_PROXY.getValue()));
        config.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), params.getString(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));


        config.put(UFTConstants.PROXY_ADDRESS.getValue(), params.getString(UFTConstants.PROXY_ADDRESS.getValue()));
        config.put(UFTConstants.PROXY_USERNAME.getValue(), params.getString(UFTConstants.PROXY_USERNAME.getValue()));
        config.put(UFTConstants.PROXY_PASSWORD.getValue(), params.getString(UFTConstants.PROXY_PASSWORD.getValue()));
        return config;
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        final String pathParameter = params.getString(UFTConstants.TESTS_PATH.getValue());
        final String timeoutParameter = params.getString(UFTConstants.TIMEOUT.getValue());

        I18nBean textProvider = getI18nBean();

        if (StringUtils.isEmpty(pathParameter)) {
            errorCollection.addError(UFTConstants.TESTS_PATH.getValue(), textProvider.getText("error.testsPathIsEmpty"));
        }

        if (!StringUtils.isEmpty(timeoutParameter)) {
            if (!StringUtils.isNumeric(timeoutParameter) || Integer.parseInt(timeoutParameter) < 0 | Integer.parseInt(timeoutParameter) > 30) {
                errorCollection.addError(UFTConstants.TIMEOUT.getValue(), textProvider.getText("FileSystemTaskConfigurator.error.timeoutIsNotCorrect"));
            }
        }
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(UFTConstants.PUBLISH_MODE_PARAM.getValue(), UFTConstants.PUBLISH_MODE_FAILED_VALUE.getValue());
        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.put(UFTConstants.TESTS_PATH.getValue(), taskDefinition.getConfiguration().get(UFTConstants.TESTS_PATH.getValue()));
        context.put(UFTConstants.TIMEOUT.getValue(), taskDefinition.getConfiguration().get(UFTConstants.TIMEOUT.getValue()));
        context.put(UFTConstants.USE_MC_SETTINGS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_MC_SETTINGS.getValue()));
        context.put(UFTConstants.MC_SERVER_URL.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_SERVER_URL.getValue()));
        context.put(UFTConstants.MC_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_USERNAME.getValue()));
        context.put(UFTConstants.MC_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_PASSWORD.getValue()));
        context.put(UFTConstants.MC_APPLICATION_PATH.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_APPLICATION_PATH.getValue()));
        context.put(UFTConstants.MC_APPLICATION_ID_KEY.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_APPLICATION_ID_KEY.getValue()));
        context.put(UFTConstants.PUBLISH_MODE_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PUBLISH_MODE_PARAM.getValue()));
        context.put(UFTConstants.TASK_ID_LABEL.getValue(), getI18nBean().getText(UFTConstants.TASK_ID_LABEL.getValue()) + String.format("%03d", taskDefinition.getId()));

        context.put(UFTConstants.JOB_UUID.getValue(), taskDefinition.getConfiguration().get(UFTConstants.JOB_UUID.getValue()));

        context.put(UFTConstants.OS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.OS.getValue()));
        context.put(UFTConstants.DEVICE_ID.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DEVICE_ID.getValue()));
        context.put(UFTConstants.DEVICE_NAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DEVICE_NAME.getValue()));
        context.put(UFTConstants.SOURCE.getValue(), taskDefinition.getConfiguration().get(UFTConstants.SOURCE.getValue()));
        context.put(UFTConstants.EXTRA_APPS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.EXTRA_APPS.getValue()));
        context.put(UFTConstants.LAUNCH_APP_NAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.LAUNCH_APP_NAME.getValue()));
        context.put(UFTConstants.AUT_ACTIONS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.AUT_ACTIONS.getValue()));
        context.put(UFTConstants.INSTRUMENTED.getValue(), taskDefinition.getConfiguration().get(UFTConstants.INSTRUMENTED.getValue()));
        context.put(UFTConstants.DEVICE_METRICS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DEVICE_METRICS.getValue()));

        context.put(UFTConstants.USE_SSL.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_SSL.getValue()));

        context.put(UFTConstants.USE_PROXY.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_PROXY.getValue()));
        context.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), taskDefinition.getConfiguration().get(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));


        context.put(UFTConstants.PROXY_ADDRESS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_ADDRESS.getValue()));
        context.put(UFTConstants.PROXY_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_USERNAME.getValue()));
        context.put(UFTConstants.PROXY_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_PASSWORD.getValue()));
        populateContextForLists(context);
    }

    private void populateContextForLists(@NotNull final Map<String, Object> context) {
        context.put(UFTConstants.PUBLISH_MODE_ITEMS.getValue(), getPublishModes());
    }

    private Map<String, String> getPublishModes() {
        Map<String, String> publishModesMap = new HashMap<String, String>();

        I18nBean textProvider = getI18nBean();

        publishModesMap.put(UFTConstants.PUBLISH_MODE_FAILED_VALUE.getValue(), textProvider.getText(UFTConstants.PUBLISH_MODE_FAILED.getValue()));
        publishModesMap.put(UFTConstants.PUBLISH_MODE_ALWAYS_VALUE.getValue(), textProvider.getText(UFTConstants.PUBLISH_MODE_ALWAYS.getValue()));
        publishModesMap.put(UFTConstants.PUBLISH_MODE_NEVER_VALUE.getValue(), textProvider.getText(UFTConstants.PUBLISH_MODE_NEVER.getValue()));

        return publishModesMap;
    }
}
