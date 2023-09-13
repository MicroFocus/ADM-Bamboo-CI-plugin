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

import com.adm.utils.sv.SVConstants;
import com.adm.utils.sv.SVExecutorUtil;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;

import java.util.LinkedHashMap;
import java.util.Map;

public class SVChangeModeTaskConfigurator extends AbstractTaskConfigurator {

    private static final Map SERVICE_SELECTION = new LinkedHashMap();
    private static final Map DATA_MODEL = new LinkedHashMap();
    private static final Map PERFORMANCE_MODEL = new LinkedHashMap();
    private static final Map SERVICE_MODE = new LinkedHashMap();

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

        config.put(SVConstants.SERVICE_MODE, params.getString(SVConstants.SERVICE_MODE));

        config.put(SVConstants.DATA_MODEL, params.getString(SVConstants.DATA_MODEL));
        config.put(SVConstants.DM_NAME_OR_ID, params.getString(SVConstants.DM_NAME_OR_ID));

        config.put(SVConstants.PERFORMANCE_MODEL, params.getString(SVConstants.PERFORMANCE_MODEL));
        config.put(SVConstants.PM_NAME_OR_ID, params.getString(SVConstants.PM_NAME_OR_ID));

        config.put(SVConstants.FORCE, params.getString(SVConstants.FORCE));


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
        if(SVConstants.DM_SPECIFIC.equals(params.getString(SVConstants.DATA_MODEL)))
        {
            String DMNameOrIdValue = params.getString(SVConstants.DM_NAME_OR_ID);
            if(!SVExecutorUtil.validInput(DMNameOrIdValue)) {
                errorCollection.addError(SVConstants.DM_NAME_OR_ID, SVConstants.ERROR_DM_NAME_OR_ID);
            }
        }
        if(SVConstants.PM_SPECIFIC.equals(params.getString(SVConstants.PERFORMANCE_MODEL)))
        {
            String PMNameOrIdValue = params.getString(SVConstants.PM_NAME_OR_ID);
            if(!SVExecutorUtil.validInput(PMNameOrIdValue)) {
                errorCollection.addError(SVConstants.PM_NAME_OR_ID, SVConstants.ERROR_PM_NAME_OR_ID);
            }
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        populateContextForLists(context);

        context.put(SVConstants.SERVICE_SELECTION, SVConstants.SELECTED_SERVICE_ONLY);
        context.put(SVConstants.SERVICE_MODE, SVConstants.STAND_BY);
        context.put(SVConstants.DATA_MODEL, SVConstants.DM_DEFAULT);
        context.put(SVConstants.PERFORMANCE_MODEL, SVConstants.NONE_PERFORMANCE_MODEL);
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
        if(DATA_MODEL.isEmpty()) {
            DATA_MODEL.put(SVConstants.DM_DEFAULT, textProvider.getText("sv.param.label.dmDefault"));
            DATA_MODEL.put(SVConstants.NONE_DATA_MODEL, textProvider.getText("sv.param.label.noneDataModel"));
            DATA_MODEL.put(SVConstants.DM_SPECIFIC, textProvider.getText("sv.param.label.dmSpecific"));
        }
        if(PERFORMANCE_MODEL.isEmpty()) {
            PERFORMANCE_MODEL.put(SVConstants.NONE_PERFORMANCE_MODEL, textProvider.getText("sv.param.label.nonePerformanceModel"));
            PERFORMANCE_MODEL.put(SVConstants.OFFLINE, textProvider.getText("sv.param.label.offline"));
            PERFORMANCE_MODEL.put(SVConstants.DEFAULT_PERFORMANCE_MODEL, textProvider.getText("sv.param.label.defaultPerformanceModel"));
            PERFORMANCE_MODEL.put(SVConstants.PM_SPECIFIC, textProvider.getText("sv.param.label.pmSpecific"));
        }
        if(SERVICE_MODE.isEmpty()) {
            SERVICE_MODE.put(SVConstants.STAND_BY, textProvider.getText("sv.param.label.standBy"));
            SERVICE_MODE.put(SVConstants.SIMULATING, textProvider.getText("sv.param.label.simulate"));
            SERVICE_MODE.put(SVConstants.LEARNING, textProvider.getText("sv.param.label.learn"));
        }
        context.put("SVServiceSelectionMap", SERVICE_SELECTION);
        context.put("SVDataModelMap", DATA_MODEL);
        context.put("SVPerformanceModelMap", PERFORMANCE_MODEL);
        context.put("SVServiceModeMap", SERVICE_MODE);
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

        context.put(SVConstants.SERVICE_MODE, configuration.get(SVConstants.SERVICE_MODE));

        context.put(SVConstants.DATA_MODEL, configuration.get(SVConstants.DATA_MODEL));
        context.put(SVConstants.DM_NAME_OR_ID, configuration.get(SVConstants.DM_NAME_OR_ID));

        context.put(SVConstants.PERFORMANCE_MODEL, configuration.get(SVConstants.PERFORMANCE_MODEL));
        context.put(SVConstants.PM_NAME_OR_ID, configuration.get(SVConstants.PM_NAME_OR_ID));
        context.put(SVConstants.FORCE, configuration.get(SVConstants.FORCE));
    }
}
