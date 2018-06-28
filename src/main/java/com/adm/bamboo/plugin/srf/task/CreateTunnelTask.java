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

import com.adm.bamboo.plugin.srf.configurator.CreateTunnelConfigurator;
import com.adm.bamboo.plugin.srf.impl.CreateTunnelComponent;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;

import java.io.IOException;


public class CreateTunnelTask implements TaskType
{

    @Override
    public TaskResult execute(final TaskContext taskContext)
    {
        final ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String TUNNEL_CLIENT_PATH = configurationMap.get(CreateTunnelConfigurator.TUNNEL_CLIENT_PATH);
        final String CONFIG_FILE_PATH = configurationMap.get(CreateTunnelConfigurator.CONFIG_FILE_PATH);

        try {
            buildLogger.addBuildLogEntry("=======================");
            buildLogger.addBuildLogEntry("== Create SRF Tunnel ==");
            buildLogger.addBuildLogEntry("=======================");

            // buildLogger.addBuildLogEntry(String.format("Test ID: %s & Tags: %s", TEST_ID , TAGS));

            if (TUNNEL_CLIENT_PATH == null || CONFIG_FILE_PATH == null)
            {
                buildLogger.addBuildLogEntry("Please provide Tunnel client path and config file path" );
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }

            CreateTunnelComponent createTunnelComponent = new CreateTunnelComponent(taskContext, buildLogger, TUNNEL_CLIENT_PATH, CONFIG_FILE_PATH);
            return createTunnelComponent.startRun();
        } catch (IOException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while creating tunnel: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        }
    }
}


