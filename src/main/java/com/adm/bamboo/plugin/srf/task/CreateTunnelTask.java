/*
 *
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
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
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


