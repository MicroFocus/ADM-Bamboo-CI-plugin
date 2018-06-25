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

package com.adm.bamboo.plugin.srf;

import com.adm.bamboo.plugin.srf.impl.SrfComponentsImpl;
import com.adm.utils.srf.SrfConfigParameter;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import java.io.IOException;
import java.util.List;


public class TaskExecution implements TaskType
{

    @Override
    public TaskResult execute(final TaskContext taskContext)
    {
        final ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String SRF_ADDRESS = configurationMap.get(TaskConfigurator.SRF_ADDRESS);
        final String TENANT_ID = configurationMap.get(TaskConfigurator.TENANT_ID);
        final String CLIENT_ID = configurationMap.get(TaskConfigurator.SRF_CLIENT_ID);
        final String CLIENT_SECRET = configurationMap.get(TaskConfigurator.SRF_CLIENT_SECRET);
        final String TEST_ID = configurationMap.get(TaskConfigurator.TEST_ID);
        final String TUNNEL = configurationMap.get(TaskConfigurator.TUNNEL);
        final String SHOULD_CLOSE_TUNNEL = configurationMap.get(TaskConfigurator.SHOULD_CLOSE_TUNNEL);
        final String PROXY = configurationMap.get(TaskConfigurator.PROXY);
        final String BUILD = configurationMap.get(TaskConfigurator.BUILD);
        final String RELEASE = configurationMap.get(TaskConfigurator.RELEASE);
        final String TAGS = configurationMap.get(TaskConfigurator.TAGS);
        final List<SrfConfigParameter> PARAMETERS = TaskConfigurator.fetchSrfParametersFromContext(configurationMap);

        try {
            buildLogger.addBuildLogEntry("Executing SRF Test");
            buildLogger.addBuildLogEntry("==================");

            buildLogger.addBuildLogEntry(String.format("Test ID: %s & Tags: %s", TEST_ID , TAGS));

            if (TEST_ID == null || TAGS == null)
            {
                buildLogger.addBuildLogEntry("Please provide Test ID or Tags" );
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }

            SrfComponentsImpl srfComponentsImpl = new SrfComponentsImpl(taskContext,buildLogger,SRF_ADDRESS,TENANT_ID,CLIENT_ID,CLIENT_SECRET,TEST_ID,PROXY,BUILD,RELEASE,TAGS,PARAMETERS,TUNNEL,Boolean.parseBoolean(SHOULD_CLOSE_TUNNEL));
            return srfComponentsImpl.startRun(taskContext, buildLogger);
        } catch (IOException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }
}


