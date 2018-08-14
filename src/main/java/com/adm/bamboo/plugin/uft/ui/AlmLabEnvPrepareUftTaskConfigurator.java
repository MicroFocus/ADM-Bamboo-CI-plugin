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
import com.adm.bamboo.plugin.uft.helpers.AlmConfigParameter;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmLabEnvPrepareUftTaskConfigurator extends AbstractLauncherTaskConfigurator {
    private static final Map ENV_ALM_CONFIG_OPTIONS = new HashMap();

    @NotNull
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
                                                     @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.ALM_SERVER.getValue(), params.getString(UFTConstants.ALM_SERVER.getValue()));
        config.put(UFTConstants.USER_NAME_LAB_ENV.getValue(), params.getString(UFTConstants.USER_NAME_LAB_ENV.getValue()));
        config.put(UFTConstants.PASSWORD.getValue(), params.getString(UFTConstants.PASSWORD.getValue()));
        config.put(UFTConstants.DOMAIN.getValue(), params.getString(UFTConstants.DOMAIN.getValue()));
        config.put(UFTConstants.PROJECT_LAB_ENV.getValue(), params.getString(UFTConstants.PROJECT_LAB_ENV.getValue()));

        config.put(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue(), params.getString(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue()));
        config.put(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue(), params.getString(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue()));
        config.put(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue(), params.getString(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue()));
        config.put(UFTConstants.AUT_ENV_ID.getValue(), params.getString(UFTConstants.AUT_ENV_ID.getValue()));
        config.put(UFTConstants.PATH_TO_JSON_FILE.getValue(), params.getString(UFTConstants.PATH_TO_JSON_FILE.getValue()));
        config.put(UFTConstants.ASSIGN_ENV_CONF_ID.getValue(), params.getString(UFTConstants.ASSIGN_ENV_CONF_ID.getValue()));
        config.put(UFTConstants.OUTPUT_CONFIG_ID.getValue(), params.getString(UFTConstants.OUTPUT_CONFIG_ID.getValue()));

        //parse params
        String[] typesArr = params.getStringArray(UFTConstants.ENV_ALM_PARAMETERS_TYPE.getValue());
        String[] namesArr = params.getStringArray(UFTConstants.ENV_ALM_PARAMETERS_NAME.getValue());
        String[] valuesArr = params.getStringArray(UFTConstants.ENV_ALM_PARAMETERS_VALUE.getValue());
        String[] chkOnlyFirstArr = params.getStringArray(UFTConstants.ENV_ALM_PARAMETERS_ONLYFIRST.getValue());

        if (namesArr != null && typesArr != null && valuesArr != null) {
            for (int i = 0, chk = 0; i < Math.min(namesArr.length, typesArr.length); ++i) {
                if (StringUtils.isEmpty(namesArr[i]) || StringUtils.isEmpty(valuesArr[i]))
                    continue;

                String dlm = "&;";
                String s = typesArr[i] + dlm + namesArr[i] + dlm + valuesArr[i];

                if (typesArr[i].equals(UFTConstants.PATH_TO_JSON_FILE.getValue()) && chkOnlyFirstArr != null && chkOnlyFirstArr.length > 0) {
                    s += dlm + chkOnlyFirstArr[chk];
                    chk++;
                } else {
                    s += dlm + "false";
                }
                config.put("alm_param_" + i, s);
            }
        }

        return config;
    }

    @NotNull
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        I18nBean textProvider = getI18nBean();

        if (StringUtils.isEmpty(params.getString(UFTConstants.ALM_SERVER.getValue()))) {
            errorCollection.addError(UFTConstants.ALM_SERVER.getValue(), textProvider.getText("error.ALMServerIsEmpty"));
        }

        if (StringUtils.isEmpty(params.getString(UFTConstants.USER_NAME_LAB_ENV.getValue()))) {
            errorCollection.addError(UFTConstants.USER_NAME_LAB_ENV.getValue(), textProvider.getText("error.userNameIsEmpty"));
        }
        if (StringUtils.isEmpty(params.getString(UFTConstants.DOMAIN.getValue()))) {
            errorCollection.addError(UFTConstants.DOMAIN.getValue(), textProvider.getText("error.domainIsEmpty"));
        }
        if (StringUtils.isEmpty(params.getString(UFTConstants.PROJECT_LAB_ENV.getValue()))) {
            errorCollection.addError(UFTConstants.PROJECT_LAB_ENV.getValue(), textProvider.getText("AlmLabEnvPrepareTask.error.projectIsEmpty"));
        }

        if (StringUtils.isEmpty(params.getString(UFTConstants.AUT_ENV_ID.getValue()))) {
            errorCollection.addError(UFTConstants.AUT_ENV_ID.getValue(), textProvider.getText("AlmLabEnvPrepareTask.error.autEnvIDIsEmpty"));
        }

        if (UFTConstants.ENV_ALM_CONFIG_PATTERN_OPTION_NEW.getValue().equals(params.getString(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue()))) {
            if (StringUtils.isEmpty(params.getString(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue()))) {
                errorCollection.addError(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue(), textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
            }
        } else {
            if (StringUtils.isEmpty(params.getString(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue()))) {
                errorCollection.addError(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue(), textProvider.getText("AlmLabEnvPrepareTask.error.assignAUTEnvConfValueIsNotAssigned"));
            }
        }
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue(), UFTConstants.ENV_ALM_CONFIG_PATTERN_OPTION_EXIST.getValue());
        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context,
                                       @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context = populateContext(context, taskDefinition);
        populateContextForLists(context);
    }

    private void populateContextForLists(@NotNull final Map<String, Object> context) {
        I18nBean textProvider = getI18nBean();

        if (ENV_ALM_CONFIG_OPTIONS.isEmpty()) {
            ENV_ALM_CONFIG_OPTIONS.put(UFTConstants.ENV_ALM_CONFIG_PATTERN_OPTION_EXIST.getValue(), textProvider.getText("AlmLabEnvPrepareTask.useAnExistingConfInputLbl"));
            ENV_ALM_CONFIG_OPTIONS.put(UFTConstants.ENV_ALM_CONFIG_PATTERN_OPTION_NEW.getValue(), textProvider.getText("AlmLabEnvPrepareTask.createNewConfInputLbl"));
        }
        context.put("ALMConfigOptionsMap", ENV_ALM_CONFIG_OPTIONS);

        Map<String, String> paramTypes = new HashMap<String, String>();
        paramTypes.put(UFTConstants.ENV_ALM_PARAMETERS_TYPE_ENV.getValue(), textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Environment"));
        paramTypes.put(UFTConstants.ENV_ALM_PARAMETERS_TYPE_JSON.getValue(), textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.FromJSON"));
        paramTypes.put(UFTConstants.ENV_ALM_PARAMETERS_TYPE_MAN.getValue(), textProvider.getText("AlmLabEnvPrepareTask.Parameter.Type.Manual"));

        context.put("ALMParamsTypes", paramTypes);
    }

    private final Map<String, Object> populateContext(@NotNull final Map<String, Object> context,
                                 @NotNull final TaskDefinition taskDefinition) {

        final Map<String, String> configuration = taskDefinition.getConfiguration();

        context.put(UFTConstants.ALM_SERVER.getValue(), configuration.get(UFTConstants.ALM_SERVER.getValue()));
        context.put(UFTConstants.USER_NAME_LAB_ENV.getValue(), configuration.get(UFTConstants.USER_NAME_LAB_ENV.getValue()));
        context.put(UFTConstants.PASSWORD_LAB_ENV.getValue(), configuration.get(UFTConstants.PASSWORD_LAB_ENV.getValue()));
        context.put(UFTConstants.DOMAIN.getValue(), configuration.get(UFTConstants.DOMAIN.getValue()));
        context.put(UFTConstants.PROJECT_LAB_ENV.getValue(), configuration.get(UFTConstants.PROJECT_LAB_ENV.getValue()));

        context.put(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue(), configuration.get(UFTConstants.ENV_ALM_CONFIGS_OPTION.getValue()));
        context.put(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue(), configuration.get(UFTConstants.AUT_ENV_NEW_CONFIG_NAME.getValue()));
        context.put(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue(), configuration.get(UFTConstants.AUT_ENV_EXIST_CONFIG_ID.getValue()));
        context.put(UFTConstants.AUT_ENV_ID.getValue(), configuration.get(UFTConstants.AUT_ENV_ID.getValue()));

        context.put(UFTConstants.PATH_TO_JSON_FILE.getValue(), configuration.get(UFTConstants.PATH_TO_JSON_FILE.getValue()));
        context.put(UFTConstants.ASSIGN_ENV_CONF_ID.getValue(), configuration.get(UFTConstants.ASSIGN_ENV_CONF_ID.getValue()));
        context.put(UFTConstants.OUTPUT_CONFIG_ID.getValue(), configuration.get(UFTConstants.OUTPUT_CONFIG_ID.getValue()));

        List<AlmConfigParameter> almParams = fetchAlmParametersFromContext(configuration);
        context.put("almParams", almParams);

        return context;
    }

    public static List<AlmConfigParameter> fetchAlmParametersFromContext(@NotNull final Map<String, String> context) {
        List<AlmConfigParameter> almParams = new ArrayList<AlmConfigParameter>(context.size());

        for (String key : context.keySet()) {
            if (key.startsWith("alm_param_")) {
                String[] arr = context.get(key).split("&;");
                almParams.add(new AlmConfigParameter(arr[0], arr[1], arr[2], arr[3]));
            }
        }

        return almParams;
    }

    public static boolean useExistingConfiguration(Map<String, String> confMap) {
        return confMap.get(UFTConstants.ENV_ALM_CONFIGS_OPTION).equals(UFTConstants.ENV_ALM_CONFIG_PATTERN_OPTION_EXIST);
    }

}
