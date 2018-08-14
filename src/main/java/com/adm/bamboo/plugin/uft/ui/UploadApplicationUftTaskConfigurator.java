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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UploadApplicationUftTaskConfigurator extends AbstractLauncherTaskConfigurator {

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.MC_SERVER_URL.getValue(), params.getString(UFTConstants.MC_SERVER_URL.getValue()));
        config.put(UFTConstants.MC_USERNAME.getValue(), params.getString(UFTConstants.MC_USERNAME.getValue()));
        config.put(UFTConstants.MC_PASSWORD.getValue(), params.getString(UFTConstants.MC_PASSWORD.getValue()));
        config.put(UFTConstants.TASK_NAME.getValue(), getI18nBean().getText(UFTConstants.TASK_NAME.getValue()));


        config.put(UFTConstants.USE_PROXY.getValue(), params.getString(UFTConstants.USE_PROXY.getValue()));
        config.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), params.getString(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));


        config.put(UFTConstants.PROXY_ADDRESS.getValue(), params.getString(UFTConstants.PROXY_ADDRESS.getValue()));
        config.put(UFTConstants.PROXY_USERNAME.getValue(), params.getString(UFTConstants.PROXY_USERNAME.getValue()));
        config.put(UFTConstants.PROXY_PASSWORD.getValue(), params.getString(UFTConstants.PROXY_PASSWORD.getValue()));

        String[] typesArr = params.getStringArray(UFTConstants.MC_APPLICATION_PATH.getValue());

        if (typesArr != null) {
            for (int i = 0; i < typesArr.length; ++i) {
                if (StringUtils.isEmpty(typesArr[i])) {
                    continue;
                }
                config.put("appPath_" + i, typesArr[i]);
            }
        }

        return config;
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {

        super.populateContextForCreate(context);

        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        final Map<String, String> configuration = taskDefinition.getConfiguration();

        context.put(UFTConstants.MC_SERVER_URL.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_SERVER_URL.getValue()));
        context.put(UFTConstants.MC_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_USERNAME.getValue()));
        context.put(UFTConstants.MC_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_PASSWORD.getValue()));
        context.put(UFTConstants.MC_APPLICATION_PATH.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_APPLICATION_PATH.getValue()));
        context.put(UFTConstants.TASK_ID_LABEL.getValue(), getI18nBean().getText(UFTConstants.TASK_ID_LABEL.getValue()) + String.format("%03d", taskDefinition.getId()));

        context.put(UFTConstants.USE_PROXY.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_PROXY.getValue()));
        context.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), taskDefinition.getConfiguration().get(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));

        context.put(UFTConstants.PROXY_ADDRESS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_ADDRESS.getValue()));
        context.put(UFTConstants.PROXY_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_USERNAME.getValue()));
        context.put(UFTConstants.PROXY_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_PASSWORD.getValue()));

        List<String> pathList = fetchMCApplicationPathFromContext(configuration);
        context.put("mcPathParams", pathList);
    }

    public static List<String> fetchMCApplicationPathFromContext(@NotNull final Map<String, String> context) {
        List<String> mcPathList = new ArrayList<String>(context.size());

        for (String key : context.keySet()) {
            if (key.startsWith("appPath_")) {
                String path = context.get(key);
                mcPathList.add(path);
            }
        }

        return mcPathList;
    }


    private void populateContextForLists(@org.jetbrains.annotations.NotNull final Map<String, Object> context) {
    }
}
