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

package com.adm.bamboo.plugin.performancecenter.impl;


import com.atlassian.bamboo.build.fileserver.DefaultBuildDirectoryManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;

import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.FINISHED;
import static com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState.RUN_FAILURE;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;

/**
 * Created by bemh on 7/23/2017.
 */
public class PcComponentsImpl {


    HttpContext context = null;
    HttpClient client = null;
    TaskContext taskContext;
    PcModelBamboo pcModel;
    PcClientBamboo pcClient;
    PcRunResponse response = null;

    String pcReportFile;

    BuildLogger buildLogger;


    public PcComponentsImpl(TaskContext taskContext, BuildLogger buildLogger, String pcServerName, String almUserName, String almPassword, String almDomain, String almProject,
                            String testId, String autoTestInstanceID, String testInstanceId, String timeslotDurationHours, String timeslotDurationMinutes,
                            PostRunAction postRunAction, boolean vudsMode, boolean sla, String description, String addRunToTrendReport, String trendReportId, boolean HTTPSProtocol, String proxyOutURL, String proxyUser, String proxyPassword, boolean authenticateWithToken){

//    public PcComponentsImpl(BuildLogger buildLogger){

        CookieStore cookieStore;
        context = new BasicHttpContext();
        this.taskContext = taskContext;
        cookieStore = new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        this.buildLogger = buildLogger;

        PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault());
        cxMgr.setMaxTotal(100);
        cxMgr.setDefaultMaxPerRoute(20);
        client = new DefaultHttpClient(cxMgr);
        pcModel = new PcModelBamboo(pcServerName, almUserName,almPassword, almDomain, almProject,testId,autoTestInstanceID, testInstanceId,timeslotDurationHours,timeslotDurationMinutes, postRunAction,vudsMode,sla, description,addRunToTrendReport,trendReportId,HTTPSProtocol,proxyOutURL,proxyUser,proxyPassword, authenticateWithToken);
//        PcModelBamboo pcModel = new PcModelBamboo(PC_SERVER_NAME, ALM_USER_NAME,ALM_PASSWORD, ALM_DOMAIN, ALM_PROJECT,TEST_ID,TESTINSTANCEID, TEST_INSTANCE_ID,TIMESLOT_DURATION_HOURS,TIMESLOT_DURATION_MINUTES, POST_RUN_ACTION,VUDS_MODE, DESCRIPTION,ADD_RUN_TO_TREND_REPORT,TREND_REPORT_ID,IS_HTTPS,PROXY_OUT_URL);
        pcClient = new PcClientBamboo(pcModel,taskContext,buildLogger);
    }


    public Boolean pcAuthenticate() throws IOException, PcException {

        Boolean loggedIn = false;
        loggedIn =  pcClient.login();
        return loggedIn;

    }

    public String startRun() throws IOException, PcException, InterruptedException {
        int runId = 0;
        String eventLogString = "";
        boolean trendReportReady = false;



        try {
            runId = pcClient.startRun();

            response = pcClient.waitForRunCompletion(runId);

            RunState runState = RunState.get(response.getRunState());
            if (response != null && runState == FINISHED) {
                DefaultBuildDirectoryManager defaultBuildDirectoryManager = new DefaultBuildDirectoryManager();
                pcReportFile = pcClient.publishRunReport(runId,String.valueOf(taskContext.getWorkingDirectory()));

                buildLogger.addBuildLogEntry("View analysis report of run: " + runId + ", in the Artifacts Tab.");

                // Adding the trend report section if ID has been set
                if(("USE_ID").equals(pcModel.getAddRunToTrendReport()) && pcModel.getTrendReportId() != null && runState != RUN_FAILURE){
                    pcClient.addRunToTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.waitForRunToPublishOnTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.downloadTrendReportAsPdf(pcModel.getTrendReportId(),getTrendReportsDirectory(runId));
                    trendReportReady = true;
                }

                // Adding the trend report if the Associated Trend report is selected.
                if(("ASSOCIATED").equals(pcModel.getAddRunToTrendReport()) && runState != RUN_FAILURE){
                    pcClient.addRunToTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.waitForRunToPublishOnTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.downloadTrendReportAsPdf(pcModel.getTrendReportId(), getTrendReportsDirectory(runId));
                    trendReportReady = true;
                }

            } else if (response != null && runState.ordinal() > FINISHED.ordinal()) {
                PcRunEventLog eventLog = pcClient.getRunEventLog(runId);
                eventLogString = buildEventLogString(eventLog);
            }


        } catch (PcException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return String.valueOf(runId);
    }

    public Boolean isSlaStatusPassed()
    {
        if(response != null)
            return response.getRunSLAStatus().equalsIgnoreCase("passed");
        return false;
    }

    public String getRunSLAStatus()
    {
        if(response != null)
            return response.getRunSLAStatus();
		return "unknown";
    }

    private String getTrendReportsDirectory(int runId) {
//        return String.valueOf(taskContext.getWorkingDirectory())  + File.separator + taskContext.getBuildContext().getBuildNumber() +  File.separator + "TrendReports";
        return String.valueOf(taskContext.getWorkingDirectory())  + File.separator + "Reports" +  File.separator + "TrendReports";
    }


    private String buildEventLogString(PcRunEventLog eventLog) {

        String logFormat = "%-5s | %-7s | %-19s | %s\n";
        StringBuilder eventLogStr = new StringBuilder("Event Log:\n\n" + String.format(logFormat, "ID", "TYPE", "TIME","DESCRIPTION"));
        for (PcRunEventLogRecord record : eventLog.getRecordsList()) {
            eventLogStr.append(String.format(logFormat, record.getID(), record.getType(), record.getTime(), record.getDescription()));
        }
        return eventLogStr.toString();
    }




}
