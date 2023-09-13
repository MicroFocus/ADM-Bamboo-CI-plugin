/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
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
