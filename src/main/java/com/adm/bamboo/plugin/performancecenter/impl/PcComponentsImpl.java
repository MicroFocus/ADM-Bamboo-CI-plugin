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

import static com.microfocus.adm.performancecenter.plugins.common.pcEntities.RunState.FINISHED;
import static com.microfocus.adm.performancecenter.plugins.common.pcEntities.RunState.RUN_FAILURE;

import com.microfocus.adm.performancecenter.plugins.common.pcEntities.*;

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
                            PostRunAction postRunAction, boolean vudsMode, boolean sla, String description, String addRunToTrendReport, String trendReportId, boolean HTTPSProtocol, String proxyOutURL, String proxyUser, String proxyPassword){

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
        pcModel = new PcModelBamboo(pcServerName, almUserName,almPassword, almDomain, almProject,testId,autoTestInstanceID, testInstanceId,timeslotDurationHours,timeslotDurationMinutes, postRunAction,vudsMode,sla, description,addRunToTrendReport,trendReportId,HTTPSProtocol,proxyOutURL,proxyUser,proxyPassword);
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


            if (response != null && RunState.get(response.getRunState()) == FINISHED) {
                DefaultBuildDirectoryManager defaultBuildDirectoryManager = new DefaultBuildDirectoryManager();
                pcReportFile = pcClient.publishRunReport(runId,String.valueOf(taskContext.getWorkingDirectory()));

                buildLogger.addBuildLogEntry("View analysis report of run: " + runId + ", in the Artifacts Tab.");

                // Adding the trend report section if ID has been set
                if(("USE_ID").equals(pcModel.getAddRunToTrendReport()) && pcModel.getTrendReportId() != null && RunState.get(response.getRunState()) != RUN_FAILURE){
                    pcClient.addRunToTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.waitForRunToPublishOnTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.downloadTrendReportAsPdf(pcModel.getTrendReportId(),getTrendReportsDirectory(runId));
                    trendReportReady = true;
                }

                // Adding the trend report if the Associated Trend report is selected.
                if(("ASSOCIATED").equals(pcModel.getAddRunToTrendReport()) && RunState.get(response.getRunState()) != RUN_FAILURE){
                    pcClient.addRunToTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.waitForRunToPublishOnTrendReport(runId, pcModel.getTrendReportId());
                    pcClient.downloadTrendReportAsPdf(pcModel.getTrendReportId(), getTrendReportsDirectory(runId));
                    trendReportReady = true;
                }

            } else if (response != null && RunState.get(response.getRunState()).ordinal() > FINISHED.ordinal()) {
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
