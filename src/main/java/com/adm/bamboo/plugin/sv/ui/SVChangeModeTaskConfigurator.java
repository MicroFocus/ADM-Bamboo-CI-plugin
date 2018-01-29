package com.adm.bamboo.plugin.sv.ui;

import com.adm.utils.sv.SVConsts;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/4/17
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVChangeModeTaskConfigurator extends AbstractTaskConfigurator {

    private static final Map SERVICE_SELECTION = new LinkedHashMap();
    private static final Map DATA_MODEL = new LinkedHashMap();
    private static final Map PERFORMANCE_MODEL = new LinkedHashMap();
    private static final Map SERVICE_MODE = new LinkedHashMap();

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

        config.put(SVConsts.SERVICE_MODE, params.getString(SVConsts.SERVICE_MODE));

        config.put(SVConsts.DATA_MODEL, params.getString(SVConsts.DATA_MODEL));
        config.put(SVConsts.DM_NAME_OR_ID, params.getString(SVConsts.DM_NAME_OR_ID));

        config.put(SVConsts.PERFORMANCE_MODEL, params.getString(SVConsts.PERFORMANCE_MODEL));
        config.put(SVConsts.PM_NAME_OR_ID, params.getString(SVConsts.PM_NAME_OR_ID));

        config.put(SVConsts.FORCE, params.getString(SVConsts.FORCE));


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
        if(SVConsts.DM_SPECIFIC.equals(params.getString(SVConsts.DATA_MODEL)))
        {
            String DMNameOrIdValue = params.getString(SVConsts.DM_NAME_OR_ID);
            if(!validInput(DMNameOrIdValue)) {
                errorCollection.addError(SVConsts.DM_NAME_OR_ID, SVConsts.ERROR_DM_NAME_OR_ID);
            }
        }
        if(SVConsts.PM_SPECIFIC.equals(params.getString(SVConsts.PERFORMANCE_MODEL)))
        {
            String PMNameOrIdValue = params.getString(SVConsts.PM_NAME_OR_ID);
            if(!validInput(PMNameOrIdValue)) {
                errorCollection.addError(SVConsts.PM_NAME_OR_ID, SVConsts.ERROR_PM_NAME_OR_ID);
            }
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        populateContextForLists(context);

        context.put(SVConsts.SERVICE_SELECTION, SVConsts.SELECTED_SERVICE_ONLY);
        context.put(SVConsts.SERVICE_MODE, SVConsts.STAND_BY);
        context.put(SVConsts.DATA_MODEL, SVConsts.DM_DEFAULT);
        context.put(SVConsts.PERFORMANCE_MODEL, SVConsts.NONE_PERFORMANCE_MODEL);
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
        if(DATA_MODEL.isEmpty()) {
            DATA_MODEL.put(SVConsts.DM_DEFAULT, textProvider.getText("sv.param.label.dmDefault"));
            DATA_MODEL.put(SVConsts.NONE_DATA_MODEL, textProvider.getText("sv.param.label.noneDataModel"));
            DATA_MODEL.put(SVConsts.DM_SPECIFIC, textProvider.getText("sv.param.label.dmSpecific"));
        }
        if(PERFORMANCE_MODEL.isEmpty()) {
            PERFORMANCE_MODEL.put(SVConsts.NONE_PERFORMANCE_MODEL, textProvider.getText("sv.param.label.nonePerformanceModel"));
            PERFORMANCE_MODEL.put(SVConsts.OFFLINE, textProvider.getText("sv.param.label.offline"));
            PERFORMANCE_MODEL.put(SVConsts.DEFAULT_PERFORMANCE_MODEL, textProvider.getText("sv.param.label.defaultPerformanceModel"));
            PERFORMANCE_MODEL.put(SVConsts.PM_SPECIFIC, textProvider.getText("sv.param.label.pmSpecific"));
        }
        if(SERVICE_MODE.isEmpty()) {
            SERVICE_MODE.put(SVConsts.STAND_BY, textProvider.getText("sv.param.label.standBy"));
            SERVICE_MODE.put(SVConsts.SIMULATING, textProvider.getText("sv.param.label.simulate"));
            SERVICE_MODE.put(SVConsts.LEARNING, textProvider.getText("sv.param.label.learn"));
        }
        context.put("SVServiceSelectionMap", SERVICE_SELECTION);
        context.put("SVDataModelMap", DATA_MODEL);
        context.put("SVPerformanceModelMap", PERFORMANCE_MODEL);
        context.put("SVServiceModeMap", SERVICE_MODE);
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

        context.put(SVConsts.SERVICE_MODE, configuration.get(SVConsts.SERVICE_MODE));

        context.put(SVConsts.DATA_MODEL, configuration.get(SVConsts.DATA_MODEL));
        context.put(SVConsts.DM_NAME_OR_ID, configuration.get(SVConsts.DM_NAME_OR_ID));

        context.put(SVConsts.PERFORMANCE_MODEL, configuration.get(SVConsts.PERFORMANCE_MODEL));
        context.put(SVConsts.PM_NAME_OR_ID, configuration.get(SVConsts.PM_NAME_OR_ID));
        context.put(SVConsts.FORCE, configuration.get(SVConsts.FORCE));
    }

    private boolean validInput(String input) {
        boolean valid = true;
        if(input == null || StringUtils.isEmpty(input)) {
            valid = false;
        }
        return valid;
    }
}
