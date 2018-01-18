package com.adm.bamboo.plugin.sv.ui;

import com.adm.utils.sv.SVConsts;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVUndeployTaskConfigurator extends AbstractTaskConfigurator {

    private static final Map SERVICE_SELECTION = new HashMap();

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(SVConsts.URL, params.getString(SVConsts.URL));
        config.put(SVConsts.USERNAME, params.getString(SVConsts.USERNAME));
        config.put(SVConsts.PASSWORD, params.getString(SVConsts.PASSWORD));
        config.put(SVConsts.SERVICE_SELECTION, params.getString(SVConsts.SERVICE_SELECTION));
        config.put(SVConsts.SERVICE_NAME_OR_ID, params.getString(SVConsts.SERVICE_NAME_OR_ID));
        config.put(SVConsts.PROJECT_PATH, params.getString(SVConsts.PROJECT_PATH));
        config.put(SVConsts.PROJECT_PASSWORD, params.getString(SVConsts.PROJECT_PASSWORD));
        config.put(SVConsts.FORCE, params.getString(SVConsts.FORCE));
        config.put(SVConsts.CONTINUE, params.getString(SVConsts.CONTINUE));

        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        String urlValue = params.getString(SVConsts.URL);
        if (!validInput(urlValue))
        {
            errorCollection.addError(SVConsts.URL,
                    SVConsts.ERROR_URL);
        }
        if(SVConsts.SELECTED_SERVICE_ONLY.equals(params.getString(SVConsts.SERVICE_SELECTION)))
        {
            String serviceNameOrIdValue = params.getString(SVConsts.SERVICE_NAME_OR_ID);
            if(!validInput(serviceNameOrIdValue)) {
                errorCollection.addError(SVConsts.SERVICE_NAME_OR_ID, SVConsts.ERROR_SERVICE_NAME_OR_ID);
            }
        }else if(SVConsts.ALL_SERVICES_FROM_PROJECT.equals(params.getString(SVConsts.SERVICE_SELECTION))){
            String projectPathValue = params.getString(SVConsts.PROJECT_PATH);
            if(!validInput(projectPathValue)) {
                errorCollection.addError(SVConsts.PROJECT_PATH, SVConsts.ERROR_PROJECT_PATH);
            }
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        populateContextForLists(context);

        context.put(SVConsts.SERVICE_SELECTION, SVConsts.SELECTED_SERVICE_ONLY);
    }

    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        populateContext(context, taskDefinition);
        populateContextForLists(context);
    }

    private void populateContextForLists(@NotNull final Map<String, Object> context) {
        I18nBean textProvider = getI18nBean();
        if(SERVICE_SELECTION.isEmpty()) {
            SERVICE_SELECTION.put(SVConsts.SELECTED_SERVICE_ONLY, textProvider.getText("sv.param.label.selectedServiceOnly"));
            SERVICE_SELECTION.put(SVConsts.ALL_SERVICES_FROM_PROJECT, textProvider.getText("sv.param.label.allServicesFromProject"));
            SERVICE_SELECTION.put(SVConsts.ALL_SERVICES_DEPLOYED_ON_SERVER, textProvider.getText("sv.param.label.allServicesDeployedOnServer"));
        }
        context.put("SVServiceSelectionMap", SERVICE_SELECTION);
    }

    private void populateContext(@NotNull final Map<String, Object> context,
                                 @NotNull final TaskDefinition taskDefinition) {


        Map<String, String> configuration = taskDefinition.getConfiguration();
        context.put(SVConsts.URL, configuration.get(SVConsts.URL));
        context.put(SVConsts.USERNAME, configuration.get(SVConsts.USERNAME));
        context.put(SVConsts.PASSWORD, configuration.get(SVConsts.PASSWORD));
        context.put(SVConsts.SERVICE_SELECTION, configuration.get(SVConsts.SERVICE_SELECTION));
        context.put(SVConsts.SERVICE_NAME_OR_ID, configuration.get(SVConsts.SERVICE_NAME_OR_ID));
        context.put(SVConsts.PROJECT_PATH, configuration.get(SVConsts.PROJECT_PATH));
        context.put(SVConsts.PROJECT_PASSWORD, configuration.get(SVConsts.PROJECT_PASSWORD));
        context.put(SVConsts.FORCE, configuration.get(SVConsts.FORCE));
        context.put(SVConsts.CONTINUE, configuration.get(SVConsts.CONTINUE));
    }

    private boolean validInput(String input) {
        boolean valid = true;
        if(input == null || StringUtils.isEmpty(input)) {
            valid = false;
        }
        return valid;
    }
}
