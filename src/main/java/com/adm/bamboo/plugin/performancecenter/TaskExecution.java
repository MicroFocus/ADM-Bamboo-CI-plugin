/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.performancecenter;

import com.adm.bamboo.plugin.performancecenter.impl.PcComponentsImpl;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
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
        final String AUTHENTICATE_WITH_TOKEN = taskContext.getConfigurationMap().get(TaskConfigurator.AUTHENTICATE_WITH_TOKEN);
        //String workingDirPath = String.valueOf(taskContext.getWorkingDirectory());
        String runID;


        PcComponentsImpl r = new PcComponentsImpl(taskContext,buildLogger,PC_SERVER,USER,PASSWORD,DOMAIN,PROJECT,TEST_ID,TEST_INSTANCE_ID_RADIO,TEST_INSTANCE_ID,TIMESLOT_HOURS,TIMESLOT_MINUTES, convertStringBackToPostRunAction(POST_RUN_ACTION),Boolean.parseBoolean(VUDS),Boolean.parseBoolean(SLA),"",TRENDING_RADIO,TREND_REPORT_ID,Boolean.parseBoolean(HTTPS),LOCAL_PROXY,PROXY_USER,PROXY_PASSWORD, Boolean.parseBoolean(AUTHENTICATE_WITH_TOKEN));


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

            if(Boolean.parseBoolean(SLA) == true && !r.isSlaStatusPassed())
            {
                buildLogger.addErrorLogEntry("Run measurements did not reach SLA criteria. Run SLA Status: " + r.getRunSLAStatus());
                return TaskResultBuilder.newBuilder(taskContext).failed().build();
            }

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


