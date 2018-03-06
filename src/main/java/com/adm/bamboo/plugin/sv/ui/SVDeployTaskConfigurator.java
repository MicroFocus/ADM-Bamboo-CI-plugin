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

package com.adm.bamboo.plugin.sv.ui;

import java.util.Map;
import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;

public class SVDeployTaskConfigurator extends AbstractTaskConfigurator {

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(SVConstants.URL, params.getString(SVConstants.URL));
        config.put(SVConstants.USERNAME, params.getString(SVConstants.USERNAME));
        config.put(SVConstants.PASSWORD, params.getString(SVConstants.PASSWORD));
        config.put(SVConstants.SERVICE_NAME_OR_ID, params.getString(SVConstants.SERVICE_NAME_OR_ID));
        config.put(SVConstants.PROJECT_PATH, params.getString(SVConstants.PROJECT_PATH));
        config.put(SVConstants.PROJECT_PASSWORD, params.getString(SVConstants.PROJECT_PASSWORD));
        config.put(SVConstants.FORCE, params.getString(SVConstants.FORCE));
        config.put(SVConstants.FIRST_SUITABLE_AGENT_FALLBACK, params.getString(SVConstants.FIRST_SUITABLE_AGENT_FALLBACK));

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
        String projectPathValue = params.getString(SVConstants.PROJECT_PATH);
        if (!SVExecutorUtil.validInput(projectPathValue))
        {
            errorCollection.addError(SVConstants.PROJECT_PATH,
                    SVConstants.ERROR_PROJECT_PATH);
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        populateContext(context, taskDefinition);
    }

    private void populateContext(final Map<String, Object> context,
                                 final TaskDefinition taskDefinition) {


        Map<String, String> configuration = taskDefinition.getConfiguration();
        context.put(SVConstants.URL, configuration.get(SVConstants.URL));
        context.put(SVConstants.USERNAME, configuration.get(SVConstants.USERNAME));
        context.put(SVConstants.PASSWORD, configuration.get(SVConstants.PASSWORD));
        context.put(SVConstants.SERVICE_NAME_OR_ID, configuration.get(SVConstants.SERVICE_NAME_OR_ID));
        context.put(SVConstants.PROJECT_PATH, configuration.get(SVConstants.PROJECT_PATH));
        context.put(SVConstants.PROJECT_PASSWORD, configuration.get(SVConstants.PROJECT_PASSWORD));
        context.put(SVConstants.FORCE, configuration.get(SVConstants.FORCE));
        context.put(SVConstants.FIRST_SUITABLE_AGENT_FALLBACK, configuration.get(SVConstants.FIRST_SUITABLE_AGENT_FALLBACK));
    }
}
