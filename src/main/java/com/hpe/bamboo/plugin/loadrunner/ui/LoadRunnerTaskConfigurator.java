package com.hpe.bamboo.plugin.loadrunner.ui;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.struts.DefaultTextProvider;
import com.atlassian.struts.TextProvider;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by habash on 30/05/2017.
 */
public class LoadRunnerTaskConfigurator extends AbstractTaskConfigurator {

    private static TextProvider textProvider = new DefaultTextProvider();
    private static final String TESTS = textProvider.getText("lr.param.name.tests");
    private static final String TIMEOUT = textProvider.getText("lr.param.name.timeout");
    private static final String POLLING_INTERVAL = textProvider.getText("lr.param.name.pollingInterval");
    private static final String EXEC_TIMEOUT = textProvider.getText("lr.param.name.execTimeout");
    private static final String IGNORE_ERRORS = textProvider.getText("lr.param.name.ignoreErrors");
    private static final String LABEL_TESTS = textProvider.getText("lr.param.label.tests");
    private static final String LABEL_TIMEOUT = textProvider.getText("lr.param.label.timeout");
    private static final String LABEL_POLLING_INTERVAL = textProvider.getText("lr.param.label.pollingInterval");
    private static final String LABEL_EXEC_TIMEOUT = textProvider.getText("lr.param.label.execTimeout");
    private static final String LABEL_IGNORE_ERRORS = textProvider.getText("lr.param.label.ignoreErrors");
    private static final String DEFAULT_TIMEOUT = textProvider.getText("lr.param.value.timeout");
    private static final String DEFAULT_POLLING_INTERVAL = textProvider.getText("lr.param.value.pollingInterval");
    private static final String DEFAULT_EXEC_TIMEOUT = textProvider.getText("lr.param.value.execTimeout");

    private static final String INT_REGEX = "[0-9]*";


    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(TESTS, params.getString(TESTS));
        config.put(TIMEOUT, params.getString(TIMEOUT));
        config.put(POLLING_INTERVAL, params.getString(POLLING_INTERVAL));
        config.put(EXEC_TIMEOUT, params.getString(EXEC_TIMEOUT));
        config.put(IGNORE_ERRORS, params.getString(IGNORE_ERRORS));

        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        String testsValue = params.getString(TESTS);
        if (!goodInput(TESTS, testsValue))
        {
            errorCollection.addError(TESTS, textProvider.getText("lr.param.error.tests"));
        }
        //TBD: Make sure the value is a positive integers
        String timeoutValue = params.getString(TIMEOUT);
        if (!goodInput(TIMEOUT, timeoutValue))
        {
            errorCollection.addError(TIMEOUT, textProvider.getText("lr.param.error.timeout"));
        }
        //TBD: Make sure the value is a positive integers
        String pollingValue = params.getString(POLLING_INTERVAL);
        if (!goodInput(POLLING_INTERVAL, pollingValue))
        {
            errorCollection.addError(POLLING_INTERVAL, textProvider.getText("lr.param.error.pollingInterval"));
        }
        //TBD: Make sure the value is a positive integers
        String execTimeoutValue = params.getString(EXEC_TIMEOUT);
        if (!goodInput(EXEC_TIMEOUT, execTimeoutValue))
        {
            errorCollection.addError(EXEC_TIMEOUT, textProvider.getText("lr.param.error.execTimeout"));
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);

        context.put(TIMEOUT, DEFAULT_TIMEOUT);
        context.put(POLLING_INTERVAL, DEFAULT_POLLING_INTERVAL);
        context.put(EXEC_TIMEOUT, DEFAULT_EXEC_TIMEOUT);

    }

    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        context.put(TESTS, taskDefinition.getConfiguration().get(TESTS));
        context.put(TIMEOUT, taskDefinition.getConfiguration().get(TIMEOUT));
        context.put(POLLING_INTERVAL, taskDefinition.getConfiguration().get(POLLING_INTERVAL));
        context.put(EXEC_TIMEOUT, taskDefinition.getConfiguration().get(EXEC_TIMEOUT));
        context.put(IGNORE_ERRORS, taskDefinition.getConfiguration().get(IGNORE_ERRORS));
    }

    private boolean goodInput(String key, String input) {
        boolean good = true;
        if(input == null || StringUtils.isEmpty(input))
            good = false;
        else if(TIMEOUT.equals(key) || POLLING_INTERVAL.equals(key)
    || EXEC_TIMEOUT.equals(key)) {
            if(!input.matches(INT_REGEX)) {
                good = false;
            }
        }
        return good;
    }
}

