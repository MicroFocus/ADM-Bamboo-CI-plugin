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
    private TaskContext taskContext;
    private BuildLogger buildLogger;

    @Override
    public TaskResult execute(final TaskContext taskContext)
    {
        this.taskContext = taskContext;
        final ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        this.buildLogger = taskContext.getBuildLogger();

        final String SRF_ADDRESS = configurationMap.get(ExecutionConfigurator.SRF_ADDRESS);
        final String TENANT_ID = configurationMap.get(ExecutionConfigurator.TENANT_ID);
        final String CLIENT_ID = configurationMap.get(ExecutionConfigurator.SRF_CLIENT_ID);
        final String CLIENT_SECRET = configurationMap.get(ExecutionConfigurator.SRF_CLIENT_SECRET);
        final String TEST_ID = configurationMap.get(ExecutionConfigurator.TEST_ID);
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

            if (isValid(SRF_ADDRESS) || isValid(TENANT_ID) || isValid(CLIENT_ID) || isValid(CLIENT_SECRET))
            {
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }

            buildLogger.addBuildLogEntry(String.format("Test ID: %s & Tags: %s", TEST_ID , TAGS));

            if (TEST_ID == null || TAGS == null)
            {
                buildLogger.addErrorLogEntry("Please provide Test ID or Tags" );
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }

            ExecutionComponent executionComponents = new ExecutionComponent(taskContext,buildLogger,SRF_ADDRESS,TENANT_ID,CLIENT_ID,CLIENT_SECRET,TEST_ID,PROXY,BUILD,RELEASE,TAGS,PARAMETERS,TUNNEL,Boolean.parseBoolean(SHOULD_CLOSE_TUNNEL));
            return executionComponents.startRun();
        } catch (IOException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }

    private boolean isValid(String param) {
        if (param.startsWith("${bamboo.")) {
            buildLogger.addErrorLogEntry(String.format("Please provide: %s", param.substring(9, param.length()-1)) );
            return true;
        }

        return false;
    }
}


