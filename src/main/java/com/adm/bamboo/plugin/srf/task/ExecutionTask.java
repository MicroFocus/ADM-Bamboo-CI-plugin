/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.srf.task;

import com.adm.bamboo.plugin.srf.configurator.ExecutionConfigurator;
import com.adm.bamboo.plugin.srf.impl.ExecutionComponent;
import com.adm.utils.srf.SrfConfigParameter;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import java.io.IOException;
import java.util.List;


public class ExecutionTask implements TaskType
{
    private BuildLogger buildLogger;

    @Override
    public TaskResult execute(final TaskContext taskContext)
    {
        final ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        this.buildLogger = taskContext.getBuildLogger();

        final String SRF_ADDRESS = configurationMap.get(ExecutionConfigurator.SRF_ADDRESS);
        final String CLIENT_ID = configurationMap.get(ExecutionConfigurator.SRF_CLIENT_ID);
        final String CLIENT_SECRET = configurationMap.get(ExecutionConfigurator.SRF_CLIENT_SECRET);
        final String TEST_IDS = configurationMap.get(ExecutionConfigurator.TEST_IDS);
        final String TUNNEL = configurationMap.get(ExecutionConfigurator.TUNNEL);
        final String SHOULD_CLOSE_TUNNEL = configurationMap.get(ExecutionConfigurator.SHOULD_CLOSE_TUNNEL);
        final String PROXY = configurationMap.get(ExecutionConfigurator.PROXY);
        final String BUILD = configurationMap.get(ExecutionConfigurator.BUILD);
        final String RELEASE = configurationMap.get(ExecutionConfigurator.RELEASE);
        final String TAGS = configurationMap.get(ExecutionConfigurator.TAGS);
        final List<SrfConfigParameter> PARAMETERS = ExecutionConfigurator.fetchSrfParametersFromContext(configurationMap);

        try {
            buildLogger.addBuildLogEntry("========================");
            buildLogger.addBuildLogEntry("== Executing SRF Test ==");
            buildLogger.addBuildLogEntry("========================");

            if (TEST_IDS != null && !TEST_IDS.isEmpty())
                buildLogger.addBuildLogEntry(String.format("Test IDs: %s", TEST_IDS ));
            else
                buildLogger.addBuildLogEntry(String.format("Test Tags: %s", TAGS ));

            ExecutionComponent executionComponents = new ExecutionComponent(taskContext,buildLogger,SRF_ADDRESS,CLIENT_ID,CLIENT_SECRET,TEST_IDS,PROXY,BUILD,RELEASE,TAGS,PARAMETERS,TUNNEL,Boolean.parseBoolean(SHOULD_CLOSE_TUNNEL));
            return executionComponents.startRun();
        } catch (IOException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }
}


