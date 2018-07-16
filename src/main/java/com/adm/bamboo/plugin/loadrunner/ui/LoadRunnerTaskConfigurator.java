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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.loadrunner.ui;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;
import com.adm.utils.loadrunner.LRConsts;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by habash on 30/05/2017.
 */
public class LoadRunnerTaskConfigurator extends AbstractTaskConfigurator implements TaskRequirementSupport {

    private static final String INT_REGEX = "[0-9]*";

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(LRConsts.TESTS, params.getString(LRConsts.TESTS));
        config.put(LRConsts.TIMEOUT, params.getString(LRConsts.TIMEOUT));
        config.put(LRConsts.POLLING_INTERVAL, params.getString(LRConsts.POLLING_INTERVAL));
        config.put(LRConsts.EXEC_TIMEOUT, params.getString(LRConsts.EXEC_TIMEOUT));
        config.put(LRConsts.IGNORE_ERRORS, params.getString(LRConsts.IGNORE_ERRORS));

        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        String testsValue = params.getString(LRConsts.TESTS);
        if (!goodInput(LRConsts.TESTS, testsValue))
        {
            errorCollection.addError(LRConsts.TESTS,
                    LRConsts.ERROR_TESTS);
        }
        String timeoutValue = params.getString(LRConsts.TIMEOUT);
        if (!goodInput(LRConsts.TIMEOUT, timeoutValue))
        {
            errorCollection.addError(LRConsts.TIMEOUT,
                    LRConsts.ERROR_TIMEOUT);
        }
        String pollingValue = params.getString(LRConsts.POLLING_INTERVAL);
        if (!goodInput(LRConsts.POLLING_INTERVAL, pollingValue))
        {
            errorCollection.addError(LRConsts.POLLING_INTERVAL,
                    LRConsts.ERROR_POLLING_INTERVAL);
        }
        String execTimeoutValue = params.getString(LRConsts.EXEC_TIMEOUT);
        if (!goodInput(LRConsts.EXEC_TIMEOUT, execTimeoutValue))
        {
            errorCollection.addError(LRConsts.EXEC_TIMEOUT,
                    LRConsts.ERROR_EXEC_TIMEOUT);
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

        Map<String, String> conf = taskDefinition.getConfiguration();
        context.put(LRConsts.TESTS, conf.get(LRConsts.TESTS));
        context.put(LRConsts.TIMEOUT, conf.get(LRConsts.TIMEOUT));
        context.put(LRConsts.POLLING_INTERVAL, conf.get(LRConsts.POLLING_INTERVAL));
        context.put(LRConsts.EXEC_TIMEOUT, conf.get(LRConsts.EXEC_TIMEOUT));
        context.put(LRConsts.IGNORE_ERRORS, conf.get(LRConsts.IGNORE_ERRORS));
    }

    @Override
    public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
        return defineLoadRunnerRequirement();
    }

    private Set<Requirement> defineLoadRunnerRequirement() {
        RequirementImpl lrReq = new RequirementImpl(LRConsts.CAPABILITY_KEY, true, ".*");
        Set<Requirement> result = new HashSet<Requirement>();
        result.add(lrReq);
        return result;
    }

    private boolean goodInput(String key, String input) {
        boolean good = true;
        if(input != null) {
            if (StringUtils.isEmpty(input) && LRConsts.TESTS.equals(key))
                good = false;
            else if (LRConsts.TIMEOUT.equals(key) || LRConsts.POLLING_INTERVAL.equals(key)
                    || LRConsts.EXEC_TIMEOUT.equals(key)) {
                if (!StringUtils.isEmpty(input) && !input.matches(INT_REGEX)) {
                    good = false;
                }
            }
        }
        return good;
    }
}

