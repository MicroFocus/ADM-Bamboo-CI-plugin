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
public class SVDeployTaskConfigurator extends AbstractTaskConfigurator {

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(SVConsts.URL, params.getString(SVConsts.URL));
        config.put(SVConsts.USERNAME, params.getString(SVConsts.USERNAME));
        config.put(SVConsts.PASSWORD, params.getString(SVConsts.PASSWORD));
        config.put(SVConsts.SERVICE_NAME_OR_ID, params.getString(SVConsts.SERVICE_NAME_OR_ID));
        config.put(SVConsts.PROJECT_PATH, params.getString(SVConsts.PROJECT_PATH));
        config.put(SVConsts.PROJECT_PASSWORD, params.getString(SVConsts.PROJECT_PASSWORD));
        config.put(SVConsts.FORCE, params.getString(SVConsts.FORCE));
        config.put(SVConsts.FIRST_SUITABLE_AGENT_FALLBACK, params.getString(SVConsts.FIRST_SUITABLE_AGENT_FALLBACK));

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
        String projectPathValue = params.getString(SVConsts.PROJECT_PATH);
        if (!validInput(projectPathValue))
        {
            errorCollection.addError(SVConsts.PROJECT_PATH,
                    SVConsts.ERROR_PROJECT_PATH);
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

    private void populateContext(@NotNull final Map<String, Object> context,
                                 @NotNull final TaskDefinition taskDefinition) {


        Map<String, String> configuration = taskDefinition.getConfiguration();
        context.put(SVConsts.URL, configuration.get(SVConsts.URL));
        context.put(SVConsts.USERNAME, configuration.get(SVConsts.USERNAME));
        context.put(SVConsts.PASSWORD, configuration.get(SVConsts.PASSWORD));
        context.put(SVConsts.SERVICE_NAME_OR_ID, configuration.get(SVConsts.SERVICE_NAME_OR_ID));
        context.put(SVConsts.PROJECT_PATH, configuration.get(SVConsts.PROJECT_PATH));
        context.put(SVConsts.PROJECT_PASSWORD, configuration.get(SVConsts.PROJECT_PASSWORD));
        context.put(SVConsts.FORCE, configuration.get(SVConsts.FORCE));
        context.put(SVConsts.FIRST_SUITABLE_AGENT_FALLBACK, configuration.get(SVConsts.FIRST_SUITABLE_AGENT_FALLBACK));
    }

    private boolean validInput(String input) {
        boolean valid = true;
        if(input == null || StringUtils.isEmpty(input)) {
            valid = false;
        }
        return valid;
    }
}
