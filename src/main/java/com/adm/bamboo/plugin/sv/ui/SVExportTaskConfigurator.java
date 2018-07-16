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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.sv.ui;

import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import java.util.LinkedHashMap;
import java.util.Map;

public class SVExportTaskConfigurator extends AbstractTaskConfigurator {

    private static final Map SERVICE_SELECTION = new LinkedHashMap();

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(SVConstants.URL, params.getString(SVConstants.URL));
        config.put(SVConstants.USERNAME, params.getString(SVConstants.USERNAME));
        config.put(SVConstants.PASSWORD, params.getString(SVConstants.PASSWORD));
        config.put(SVConstants.SERVICE_SELECTION, params.getString(SVConstants.SERVICE_SELECTION));
        config.put(SVConstants.SERVICE_NAME_OR_ID, params.getString(SVConstants.SERVICE_NAME_OR_ID));
        config.put(SVConstants.PROJECT_PATH, params.getString(SVConstants.PROJECT_PATH));
        config.put(SVConstants.PROJECT_PASSWORD, params.getString(SVConstants.PROJECT_PASSWORD));
        config.put(SVConstants.TARGET_DIRECTORY, params.getString(SVConstants.TARGET_DIRECTORY));
        config.put(SVConstants.CLEAN_TARGET_DIRECTORY, params.getString(SVConstants.CLEAN_TARGET_DIRECTORY));
        config.put(SVConstants.SWITCH_SERVICE_TO_STANDBY, params.getString(SVConstants.SWITCH_SERVICE_TO_STANDBY));
        config.put(SVConstants.FORCE, params.getString(SVConstants.FORCE));
        config.put(SVConstants.ARCHIVE, params.getString(SVConstants.ARCHIVE));

        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        String urlValue = params.getString(SVConstants.URL);
        if (!SVExecutorUtil.validInput(urlValue))
        {
            errorCollection.addError(SVConstants.URL,
                    SVConstants.ERROR_URL);
        }
        String targetDirectoryValue = params.getString(SVConstants.TARGET_DIRECTORY);
        if (!SVExecutorUtil.validInput(targetDirectoryValue))
        {
            errorCollection.addError(SVConstants.TARGET_DIRECTORY,
                    SVConstants.ERROR_TARGET_DIRECTORY);
        }
        if(SVConstants.SELECTED_SERVICE_ONLY.equals(params.getString(SVConstants.SERVICE_SELECTION)))
        {
            String serviceNameOrIdValue = params.getString(SVConstants.SERVICE_NAME_OR_ID);
            if(!SVExecutorUtil.validInput(serviceNameOrIdValue)) {
                errorCollection.addError(SVConstants.SERVICE_NAME_OR_ID, SVConstants.ERROR_SERVICE_NAME_OR_ID);
            }
        }else if(SVConstants.ALL_SERVICES_FROM_PROJECT.equals(params.getString(SVConstants.SERVICE_SELECTION))){
            String projectPathValue = params.getString(SVConstants.PROJECT_PATH);
            if(!SVExecutorUtil.validInput(projectPathValue)) {
                errorCollection.addError(SVConstants.PROJECT_PATH, SVConstants.ERROR_PROJECT_PATH);
            }
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        populateContextForLists(context);

        context.put(SVConstants.SERVICE_SELECTION, SVConstants.SELECTED_SERVICE_ONLY);
    }

    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        populateContext(context, taskDefinition);
        populateContextForLists(context);
    }

    private void populateContextForLists(final Map<String, Object> context) {
        I18nBean textProvider = getI18nBean();
        if(SERVICE_SELECTION.isEmpty()) {
            SERVICE_SELECTION.put(SVConstants.SELECTED_SERVICE_ONLY, textProvider.getText("sv.param.label.selectedServiceOnly"));
            SERVICE_SELECTION.put(SVConstants.ALL_SERVICES_FROM_PROJECT, textProvider.getText("sv.param.label.allServicesFromProject"));
            SERVICE_SELECTION.put(SVConstants.ALL_SERVICES_DEPLOYED_ON_SERVER, textProvider.getText("sv.param.label.allServicesDeployedOnServer"));
        }
        context.put("SVServiceSelectionMap", SERVICE_SELECTION);
    }

    private void populateContext(final Map<String, Object> context,
                                 final TaskDefinition taskDefinition) {


        Map<String, String> configuration = taskDefinition.getConfiguration();
        context.put(SVConstants.URL, configuration.get(SVConstants.URL));
        context.put(SVConstants.USERNAME, configuration.get(SVConstants.USERNAME));
        context.put(SVConstants.PASSWORD, configuration.get(SVConstants.PASSWORD));
        context.put(SVConstants.SERVICE_SELECTION, configuration.get(SVConstants.SERVICE_SELECTION));
        context.put(SVConstants.SERVICE_NAME_OR_ID, configuration.get(SVConstants.SERVICE_NAME_OR_ID));
        context.put(SVConstants.PROJECT_PATH, configuration.get(SVConstants.PROJECT_PATH));
        context.put(SVConstants.PROJECT_PASSWORD, configuration.get(SVConstants.PROJECT_PASSWORD));
        context.put(SVConstants.TARGET_DIRECTORY, configuration.get(SVConstants.TARGET_DIRECTORY));
        context.put(SVConstants.FORCE, configuration.get(SVConstants.FORCE));
        context.put(SVConstants.ARCHIVE, configuration.get(SVConstants.ARCHIVE));
        context.put(SVConstants.SWITCH_SERVICE_TO_STANDBY, configuration.get(SVConstants.SWITCH_SERVICE_TO_STANDBY));
        context.put(SVConstants.CLEAN_TARGET_DIRECTORY, configuration.get(SVConstants.CLEAN_TARGET_DIRECTORY));
    }
}
