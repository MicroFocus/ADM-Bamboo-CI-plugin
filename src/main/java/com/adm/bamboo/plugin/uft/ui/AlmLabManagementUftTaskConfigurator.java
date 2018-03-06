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
import com.adm.utils.uft.model.CdaDetails;
import com.adm.utils.uft.model.EnumDescription;
import com.adm.utils.uft.model.SseModel;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmLabManagementUftTaskConfigurator extends AbstractLauncherTaskConfigurator {

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.ALM_SERVER.getValue(), params.getString(UFTConstants.ALM_SERVER.getValue()));
        config.put(UFTConstants.USER_NAME.getValue(), params.getString(UFTConstants.USER_NAME.getValue()));
        config.put(UFTConstants.PASSWORD.getValue(), params.getString(UFTConstants.PASSWORD.getValue()));
        config.put(UFTConstants.DOMAIN_PARAM.getValue(), params.getString(UFTConstants.DOMAIN_PARAM.getValue()));
        config.put(UFTConstants.PROJECT_NAME_PARAM.getValue(), params.getString(UFTConstants.PROJECT_NAME_PARAM.getValue()));
        config.put(UFTConstants.RUN_TYPE_PARAM.getValue(), params.getString(UFTConstants.RUN_TYPE_PARAM.getValue()));
        config.put(UFTConstants.TEST_ID_PARAM.getValue(), params.getString(UFTConstants.TEST_ID_PARAM.getValue()));
        config.put(UFTConstants.DESCRIPTION_PARAM.getValue(), params.getString(UFTConstants.DESCRIPTION_PARAM.getValue()));
        config.put(UFTConstants.DURATION_PARAM.getValue(), params.getString(UFTConstants.DURATION_PARAM.getValue()));
        config.put(UFTConstants.ENVIRONMENT_ID_PARAM.getValue(), params.getString(UFTConstants.ENVIRONMENT_ID_PARAM.getValue()));
        config.put(UFTConstants.USE_SDA_PARAM.getValue(), params.getString(UFTConstants.USE_SDA_PARAM.getValue()));
        config.put(UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue(), params.getString((UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue())));
        config.put(UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue(), params.getString((UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue())));
        config.put(UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue(), params.getString((UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue())));
        config.put(UFTConstants.TASK_NAME.getValue(), getI18nBean().getText(UFTConstants.ALM_LAB_TASK_NAME.getValue()));

        return config;
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        I18nBean textProvider = getI18nBean();

        String almServer = params.getString(UFTConstants.ALM_SERVER.getValue());
        if (StringUtils.isEmpty(almServer)) {
            errorCollection.addError(UFTConstants.ALM_SERVER.getValue(), textProvider.getText(UFTConstants.ALM_SERVER_REQUIRED_STRING.getValue()));
        }

        String userName = params.getString(UFTConstants.USER_NAME.getValue());
        if (StringUtils.isEmpty(userName)) {
            errorCollection.addError(UFTConstants.USER_NAME.getValue(), textProvider.getText(UFTConstants.USER_NAME_REQUIRED_STRING.getValue()));
        }

        String domain = params.getString(UFTConstants.DOMAIN.getValue());
        if (StringUtils.isEmpty(domain)) {
            errorCollection.addError(UFTConstants.DOMAIN.getValue(), textProvider.getText(UFTConstants.DOMAIN_REQUIRED_STRING.getValue()));
        }

        String projectName = params.getString(UFTConstants.PROJECT_NAME_PARAM.getValue());
        if (StringUtils.isEmpty(projectName)) {
            errorCollection.addError(UFTConstants.PROJECT_NAME_PARAM.getValue(), textProvider.getText(UFTConstants.PROJECT_NAME_REQUIRED_STRING.getValue()));
        }

        String testId = params.getString(UFTConstants.TEST_ID_PARAM.getValue());
        if (StringUtils.isEmpty(testId)) {
            errorCollection.addError(UFTConstants.TEST_ID_PARAM.getValue(), textProvider.getText(UFTConstants.TEST_ID_REQUIRED_STRING.getValue()));
        }

        String duration = params.getString(UFTConstants.DURATION_PARAM.getValue());
        if (StringUtils.isEmpty(duration)) {
            errorCollection.addError(UFTConstants.DURATION_PARAM.getValue(), textProvider.getText(UFTConstants.DURATION_REQUIRED_STRING.getValue()));
        } else {
            try {
                int durationInt = Integer.parseInt(duration);
                if (durationInt < 30) {
                    errorCollection.addError(UFTConstants.DURATION_PARAM.getValue(), textProvider.getText(UFTConstants.DURATION_MINIMUM_STRING.getValue()));
                }
            } catch (NumberFormatException ex) {
                errorCollection.addError(UFTConstants.DURATION_PARAM.getValue(), textProvider.getText(UFTConstants.DURATION_INVALID_FORMAT_STRING.getValue()));
            }
        }
    }

    @Override
    public void populateContextForCreate(@NotNull final java.util.Map<String, Object> context) {
        super.populateContextForCreate(context);

        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.put(UFTConstants.ALM_SERVER.getValue(), taskDefinition.getConfiguration().get(UFTConstants.ALM_SERVER.getValue()));
        context.put(UFTConstants.USER_NAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USER_NAME.getValue()));
        context.put(UFTConstants.PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PASSWORD.getValue()));
        context.put(UFTConstants.DOMAIN.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DOMAIN.getValue()));
        context.put(UFTConstants.PROJECT_NAME_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROJECT_NAME_PARAM.getValue()));
        context.put(UFTConstants.RUN_TYPE_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.RUN_TYPE_PARAM.getValue()));
        context.put(UFTConstants.TEST_ID_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.TEST_ID_PARAM.getValue()));
        context.put(UFTConstants.DESCRIPTION_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DESCRIPTION_PARAM.getValue()));
        context.put(UFTConstants.DURATION_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.DURATION_PARAM.getValue()));
        context.put(UFTConstants.ENVIRONMENT_ID_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.ENVIRONMENT_ID_PARAM.getValue()));
        context.put(UFTConstants.USE_SDA_PARAM.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_SDA_PARAM.getValue()));
        context.put(UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue(), taskDefinition.getConfiguration().get((UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue())));
        context.put(UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue(), taskDefinition.getConfiguration().get((UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue())));
        context.put(UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue(), taskDefinition.getConfiguration().get((UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue())));

        populateContextForLists(context);
    }

    private void populateContextForLists(@NotNull final Map<String, Object> context) {
        context.put(UFTConstants.RUN_TYPE_ITEMS_PARAM.getValue(), getRunTypes());
        context.put(UFTConstants.DEPLOYMENT_ACTION_ITEMS_PARAM.getValue(), getActions(CdaDetails.getDeploymentActions()));
        context.put(UFTConstants.DEPROVISIONING_ACTION_ITEMS_PARAM.getValue(), getActions(CdaDetails.getDeprovisioningActions()));
    }

    private Map<String, String> getRunTypes() {
        Map<String, String> runTypesMap = new HashMap<String, String>();

        for (EnumDescription description : SseModel.getRunTypes()) {
            runTypesMap.put(description.getValue(), description.getDescription());
        }

        return runTypesMap;
    }

    /**
     * Get deployment/deprovisioning actions
     *
     * @param actions
     * @return actions map
     */
    private Map<String, String> getActions(final List<EnumDescription> actions) {
        Map<String, String> actionsMap = new HashMap<String, String>();

        for (EnumDescription description : actions) {
            actionsMap.put(description.getValue(), description.getDescription());
        }

        return actionsMap;
    }
}
