package com.adm.bamboo.plugin.uft.ui;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTaskConfigurator;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UploadApplicationUftTaskConfigurator extends AbstractLauncherTaskConfigurator {

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(UFTConstants.MC_SERVER_URL.getValue(), params.getString(UFTConstants.MC_SERVER_URL.getValue()));
        config.put(UFTConstants.MC_USERNAME.getValue(), params.getString(UFTConstants.MC_USERNAME.getValue()));
        config.put(UFTConstants.MC_PASSWORD.getValue(), params.getString(UFTConstants.MC_PASSWORD.getValue()));
        config.put(UFTConstants.TASK_NAME.getValue(), getI18nBean().getText(UFTConstants.TASK_NAME.getValue()));


        config.put(UFTConstants.USE_PROXY.getValue(), params.getString(UFTConstants.USE_PROXY.getValue()));
        config.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), params.getString(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));


        config.put(UFTConstants.PROXY_ADDRESS.getValue(), params.getString(UFTConstants.PROXY_ADDRESS.getValue()));
        config.put(UFTConstants.PROXY_USERNAME.getValue(), params.getString(UFTConstants.PROXY_USERNAME.getValue()));
        config.put(UFTConstants.PROXY_PASSWORD.getValue(), params.getString(UFTConstants.PROXY_PASSWORD.getValue()));

        String[] typesArr = params.getStringArray(UFTConstants.MC_APPLICATION_PATH.getValue());

        if (typesArr != null) {
            for (int i = 0; i < typesArr.length; ++i) {
                if (StringUtils.isEmpty(typesArr[i])) {
                    continue;
                }
                config.put("appPath_" + i, typesArr[i]);
            }
        }

        return config;
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {

        super.populateContextForCreate(context);

        populateContextForLists(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        final Map<String, String> configuration = taskDefinition.getConfiguration();

        context.put(UFTConstants.MC_SERVER_URL.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_SERVER_URL.getValue()));
        context.put(UFTConstants.MC_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_USERNAME.getValue()));
        context.put(UFTConstants.MC_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_PASSWORD.getValue()));
        context.put(UFTConstants.MC_APPLICATION_PATH.getValue(), taskDefinition.getConfiguration().get(UFTConstants.MC_APPLICATION_PATH.getValue()));
        context.put(UFTConstants.TASK_ID_LABEL.getValue(), getI18nBean().getText(UFTConstants.TASK_ID_LABEL.getValue()) + String.format("%03d", taskDefinition.getId()));

        context.put(UFTConstants.USE_PROXY.getValue(), taskDefinition.getConfiguration().get(UFTConstants.USE_PROXY.getValue()));
        context.put(UFTConstants.SPECIFY_AUTHENTICATION.getValue(), taskDefinition.getConfiguration().get(UFTConstants.SPECIFY_AUTHENTICATION.getValue()));

        context.put(UFTConstants.PROXY_ADDRESS.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_ADDRESS.getValue()));
        context.put(UFTConstants.PROXY_USERNAME.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_USERNAME.getValue()));
        context.put(UFTConstants.PROXY_PASSWORD.getValue(), taskDefinition.getConfiguration().get(UFTConstants.PROXY_PASSWORD.getValue()));

        List<String> pathList = fetchMCApplicationPathFromContext(configuration);
        context.put("mcPathParams", pathList);
    }

    public static List<String> fetchMCApplicationPathFromContext(@NotNull final Map<String, String> context) {
        List<String> mcPathList = new ArrayList<String>(context.size());

        for (String key : context.keySet()) {
            if (key.startsWith("appPath_")) {
                String path = context.get(key);
                mcPathList.add(path);
            }
        }

        return mcPathList;
    }


    private void populateContextForLists(@org.jetbrains.annotations.NotNull final Map<String, Object> context) {
    }
}
