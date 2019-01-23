/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.srf.configurator;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// controls what objects and values are available when rendering the User Interface, how input is persisted and validated
public class CreateTunnelConfigurator extends AbstractTaskConfigurator {

    public static final String TUNNEL_CLIENT_PATH = "SRF Tunnel Client Path";
    public static final String CONFIG_FILE_PATH = "SRF Tunnel Config File";

    // Convert the params from the ui into a config map to be stored in the database for being used by the task.
    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(TUNNEL_CLIENT_PATH, params.getString(TUNNEL_CLIENT_PATH));
        config.put(CONFIG_FILE_PATH, params.getString(CONFIG_FILE_PATH));

        return config;
    }

    // Fill the saved data of the task when opening it after the last save
    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        context.put(TUNNEL_CLIENT_PATH, taskDefinition.getConfiguration().get(TUNNEL_CLIENT_PATH));
        context.put(CONFIG_FILE_PATH, taskDefinition.getConfiguration().get(CONFIG_FILE_PATH));
    }

    // Fill the data of the task when opening it at the first time
    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
    }

    // Validate the params submitted from the UI for this task definition
    @NotNull
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        if (StringUtils.isEmpty(params.getString(TUNNEL_CLIENT_PATH))) {
            errorCollection.addError(TUNNEL_CLIENT_PATH, "SRF Tunnel Client Path must be set");
        }

        if (StringUtils.isEmpty(params.getString(CONFIG_FILE_PATH))) {
            errorCollection.addError(CONFIG_FILE_PATH, "SRF Tunnel Config File must be set");
        }
    }
}
