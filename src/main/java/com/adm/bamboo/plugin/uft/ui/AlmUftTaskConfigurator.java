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
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.uft.ui;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTaskConfigurator;
import com.adm.bamboo.plugin.uft.helpers.HpTasksArtifactRegistrator;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AlmUftTaskConfigurator extends AbstractLauncherTaskConfigurator {
    private ArtifactDefinitionManager artifactDefinitionManager;

    public void setArtifactDefinitionManager(ArtifactDefinitionManager artifactDefinitionManager) {
        this.artifactDefinitionManager = artifactDefinitionManager;
    }

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.ALM_SERVER.getValue(), params.getString(UFTConstants.ALM_SERVER.getValue()));
        config.put(UFTConstants.USER_NAME.getValue(), params.getString(UFTConstants.USER_NAME.getValue()));
        config.put(UFTConstants.PASSWORD.getValue(), params.getString(UFTConstants.PASSWORD.getValue()));
        config.put(UFTConstants.DOMAIN.getValue(), params.getString(UFTConstants.DOMAIN.getValue()));
        config.put(UFTConstants.PROJECT.getValue(), params.getString(UFTConstants.PROJECT.getValue()));
        config.put(UFTConstants.TESTS_PATH.getValue(), params.getString(UFTConstants.TESTS_PATH.getValue()));
        config.put(UFTConstants.TIMEOUT.getValue(), params.getString(UFTConstants.TIMEOUT.getValue()));
        config.put(UFTConstants.RUN_MODE.getValue(), params.getString(UFTConstants.RUN_MODE.getValue()));
        config.put(UFTConstants.TESTING_TOOL_HOST.getValue(), params.getString(UFTConstants.TESTING_TOOL_HOST.getValue()));
        config.put(UFTConstants.TASK_NAME.getValue(), getI18nBean().getText(UFTConstants.ALM_TASK_NAME.getValue()));

        return config;
    }

    private String trimEnd(String s, char ch) {
        if (s == null || s.length() < 1) {
            return s;
        } else if (s.length() == 1 && s.charAt(0) == ch) {
            return "";
        }
        int i = s.length() - 1;
        while (s.charAt(i) == ch && i > 0) {
            s = s.substring(0, i);
            i--;
        }
        return s;
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        I18nBean textProvider = getI18nBean();

        params.put(UFTConstants.ALM_SERVER.getValue(), trimEnd(params.getString(UFTConstants.ALM_SERVER.getValue()), '/'));
        if (StringUtils.isEmpty(params.getString(UFTConstants.ALM_SERVER.getValue()))) {
            errorCollection.addError(UFTConstants.ALM_SERVER.getValue(), textProvider.getText("error.ALMServerIsEmpty"));
        }
        if (StringUtils.isEmpty(params.getString(UFTConstants.USER_NAME.getValue()))) {
            errorCollection.addError(UFTConstants.USER_NAME.getValue(), textProvider.getText("error.userNameIsEmpty"));
        }
        if (StringUtils.isEmpty(params.getString(UFTConstants.DOMAIN.getValue()))) {
            errorCollection.addError(UFTConstants.DOMAIN.getValue(), textProvider.getText("error.domainIsEmpty"));
        }
        if (StringUtils.isEmpty(params.getString(UFTConstants.PROJECT.getValue()))) {
            errorCollection.addError(UFTConstants.PROJECT.getValue(), textProvider.getText("RunFromAlmTask.error.projectIsEmpty"));
        }

        if (StringUtils.isEmpty(params.getString(UFTConstants.TESTS_PATH.getValue()))) {
            errorCollection.addError(UFTConstants.TESTS_PATH.getValue(), textProvider.getText("RunFromAlmTask.error.testSetIsEmpty"));
        }
        String timeoutParameter = params.getString(UFTConstants.TIMEOUT.getValue());
        if (!StringUtils.isEmpty(timeoutParameter)) {
            if (!StringUtils.isNumeric(timeoutParameter) || Integer.parseInt(timeoutParameter) < 0 | Integer.parseInt(timeoutParameter) > 30) {
                errorCollection.addError(UFTConstants.TIMEOUT.getValue(), textProvider.getText("error.timeoutIsNotCorrect"));
            }
        }
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        (new HpTasksArtifactRegistrator()).registerCommonArtifact((Job) context.get("plan"), getI18nBean(), this.artifactDefinitionManager);
        super.populateContextForCreate(context);

        populateContextForLists(context);
        //Default run mode value must be run locally
        context.put(UFTConstants.RUN_MODE.getValue(), UFTConstants.RUN_LOCALLY_PARAMETER.getValue());
    }

    private void populateContextForLists(@NotNull final Map<String, Object> context) {
        context.put(UFTConstants.RUN_MODE_PARAMETER.getValue(), getRunModes());
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        Map<String, String> configuration = taskDefinition.getConfiguration();

        context.put(UFTConstants.ALM_SERVER.getValue(), configuration.get(UFTConstants.ALM_SERVER.getValue()));
        context.put(UFTConstants.USER_NAME.getValue(), configuration.get(UFTConstants.USER_NAME.getValue()));
        context.put(UFTConstants.PASSWORD.getValue(), configuration.get(UFTConstants.PASSWORD.getValue()));
        context.put(UFTConstants.DOMAIN.getValue(), configuration.get(UFTConstants.DOMAIN.getValue()));
        context.put(UFTConstants.PROJECT.getValue(), configuration.get(UFTConstants.PROJECT.getValue()));
        context.put(UFTConstants.TESTS_PATH.getValue(), configuration.get(UFTConstants.TESTS_PATH.getValue()));
        context.put(UFTConstants.TIMEOUT.getValue(), configuration.get(UFTConstants.TIMEOUT.getValue()));
        context.put(UFTConstants.RUN_MODE.getValue(), configuration.get(UFTConstants.RUN_MODE.getValue()));
        context.put(UFTConstants.TESTING_TOOL_HOST.getValue(), configuration.get(UFTConstants.TESTING_TOOL_HOST.getValue()));

        populateContextForLists(context);
    }

    private Map<String, String> getRunModes() {
        Map<String, String> runTypesMap = new HashMap<String, String>();

        I18nBean textProvider = getI18nBean();

        //Don't change run types adding order. It's used for task creation.
        runTypesMap.put(UFTConstants.RUN_LOCALLY_PARAMETER.getValue(), textProvider.getText(UFTConstants.RUN_LOCALLY_LBL.getValue()));
        runTypesMap.put(UFTConstants.RUN_ON_PLANNED_HOST_PARAMETER.getValue(), textProvider.getText(UFTConstants.RUN_ON_PLANNED_HOST_LBL.getValue()));
        runTypesMap.put(UFTConstants.RUN_REMOTELY_PARAMETER.getValue(), textProvider.getText(UFTConstants.RUN_REMOTELY_LBL.getValue()));

        return runTypesMap;
    }
}

