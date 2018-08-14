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

package com.adm.bamboo.plugin.performancecenter;

import com.adm.bamboo.plugin.performancecenter.impl.PcComponentsImpl;
import com.microfocus.adm.performancecenter.plugins.common.pcEntities.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;

import java.io.IOException;


/**
 * Created by bemh on 7/23/2017.
 */
public class TaskExecution implements TaskType
{

    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {



        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final String PC_SERVER = taskContext.getConfigurationMap().get(TaskConfigurator.PC_SERVER);
        final String HTTPS = taskContext.getConfigurationMap().get(TaskConfigurator.HTTPS);
        final String USER = taskContext.getConfigurationMap().get(TaskConfigurator.USER);
        final String PASSWORD = taskContext.getConfigurationMap().get(TaskConfigurator.PASSWORD);
        final String DOMAIN = taskContext.getConfigurationMap().get(TaskConfigurator.DOMAIN);
        final String PROJECT = taskContext.getConfigurationMap().get(TaskConfigurator.PROJECT);
        final String TEST_ID = taskContext.getConfigurationMap().get(TaskConfigurator.TEST_ID);
        final String TEST_INSTANCE_ID_RADIO = taskContext.getConfigurationMap().get(TaskConfigurator.TEST_INSTANCE_ID_RADIO);
        final String TEST_INSTANCE_ID = taskContext.getConfigurationMap().get(TaskConfigurator.TEST_INSTANCE_ID);
        final String LOCAL_PROXY  = taskContext.getConfigurationMap().get(TaskConfigurator.LOCAL_PROXY);
        final String PROXY_USER = taskContext.getConfigurationMap().get(TaskConfigurator.PROXY_USER);
        final String PROXY_PASSWORD = taskContext.getConfigurationMap().get(TaskConfigurator.PROXY_PASSWORD);
        final String POST_RUN_ACTION = taskContext.getConfigurationMap().get(TaskConfigurator.POST_RUN_ACTION);
        final String TRENDING_RADIO = taskContext.getConfigurationMap().get(TaskConfigurator.TRENDING_RADIO);
        final String TREND_REPORT_ID = taskContext.getConfigurationMap().get(TaskConfigurator.TREND_REPORT_ID);
        final String TIMESLOT_HOURS = taskContext.getConfigurationMap().get(TaskConfigurator.TIMESLOT_HOURS);
        final String TIMESLOT_MINUTES = taskContext.getConfigurationMap().get(TaskConfigurator.TIMESLOT_MINUTES);
        final String VUDS = taskContext.getConfigurationMap().get(TaskConfigurator.VUDS);
        final String SLA = taskContext.getConfigurationMap().get(TaskConfigurator.SLA);
        //String workingDirPath = String.valueOf(taskContext.getWorkingDirectory());
        String runID;


        PcComponentsImpl r = new PcComponentsImpl(taskContext,buildLogger,PC_SERVER,USER,PASSWORD,DOMAIN,PROJECT,TEST_ID,TEST_INSTANCE_ID_RADIO,TEST_INSTANCE_ID,TIMESLOT_HOURS,TIMESLOT_MINUTES, convertStringBackToPostRunAction(POST_RUN_ACTION),Boolean.parseBoolean(VUDS),Boolean.parseBoolean(SLA),"",TRENDING_RADIO,TREND_REPORT_ID,Boolean.parseBoolean(HTTPS),LOCAL_PROXY,PROXY_USER,PROXY_PASSWORD);


        try {
            if (!r.pcAuthenticate())
                throw new PcException("Unable to login");
            buildLogger.addBuildLogEntry("Executing Load Test:");
            buildLogger.addBuildLogEntry("====================");
            buildLogger.addBuildLogEntry("Test ID:" + TEST_ID);
            if("AUTO".equals(TEST_INSTANCE_ID_RADIO)){
                buildLogger.addBuildLogEntry("Test Instance ID:" + TEST_INSTANCE_ID_RADIO);
            }else{
                buildLogger.addBuildLogEntry("Test Instance ID:" + TEST_INSTANCE_ID);
            }
            buildLogger.addBuildLogEntry("Timeslot Duration::" + TIMESLOT_HOURS + ":" + TIMESLOT_MINUTES + " (h:mm)");
            buildLogger.addBuildLogEntry("Post Run Action:" + POST_RUN_ACTION);
            buildLogger.addBuildLogEntry("Use VUDS:" + ("true".equals(VUDS.toLowerCase())?"true":"false"));

            buildLogger.addBuildLogEntry("====================");
            runID = r.startRun();
            buildLogger.addBuildLogEntry("====================");



        } catch (IOException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        } catch (PcException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry("Error while executing load test: " + e.toString());
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }



    private PostRunAction convertStringBackToPostRunAction(String postRunAction){
        for(PostRunAction p: PostRunAction.values()){
            if(postRunAction.replaceAll("_"," ").equals(p.getValue())){
                return p;
            }
        }
        return PostRunAction.DO_NOTHING;

    }



}


