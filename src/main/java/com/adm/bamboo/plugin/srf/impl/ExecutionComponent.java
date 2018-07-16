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


package com.adm.bamboo.plugin.srf.impl;

import com.adm.bamboo.plugin.srf.results.SrfResultFileWriter;
import com.adm.utils.srf.SrfConfigParameter;
import com.adm.utils.srf.SrfException;
import com.adm.utils.srf.SrfSseEventNotification;
import com.adm.utils.srf.SseEventListener;
import com.adm.utils.srf.SrfTrustManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.launchdarkly.eventsource.EventSource;
import net.sf.json.JSONArray;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.util.HttpURLConnection;
import net.sf.json.JSONObject;
import org.apache.maven.wagon.authorization.AuthorizationException;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ExecutionComponent implements Observer {
    private TaskContext taskContext;
    private SrfClient srfClient;
    private JSONArray jobIds;
    private String srfAddress;
    private String tenant;
    private String testIds;
    private String clientId;
    private String clientSecret;
    private String proxy;
    private String build;
    private String release;
    private String tags;
    private List<SrfConfigParameter> parameters;
    private String tunnel;
    private String token;
    private String workspaceId;
    private boolean shouldCloseTunnel;
    private transient EventSource eventSrc;
    private static SrfTrustManager _trustMgr = new SrfTrustManager();
    private HashSet<String> runningCount;
    private BuildLogger buildLogger;
    private CompletableFuture<TaskResult> srfExecutionFuture;
    private SseEventListener sseEventListener;
    private static SSLSocketFactory socketFactory;
    private transient HttpURLConnection con;


    public ExecutionComponent(TaskContext taskContext, BuildLogger buildLogger, String srfAddress, String clientId, String clientSecret, String testIds, String proxy, String build, String release, String tags,
                              List<SrfConfigParameter> parameters, String tunnel, boolean shouldCloseTunnel) {

        this.srfAddress =  srfAddress;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.testIds = testIds;
        this.proxy = proxy;
        this.build = build;
        this.release = release;
        this.tags = tags;
        this.parameters = parameters;
        this.tunnel = tunnel;
        this.shouldCloseTunnel = shouldCloseTunnel;
        this.buildLogger = buildLogger;
        this.taskContext = taskContext;
        this.tenant = (clientId.split("_")[0]).substring(1);
    }

    public TaskResult startRun() throws IOException {
        String srfProxy =  proxy;
        handleSrfAddress(srfAddress);
        runningCount = new HashSet<>();
        this.sseEventListener = new SseEventListener(this.buildLogger);
        this.sseEventListener.addObserver(this);
        this.srfExecutionFuture = new CompletableFuture<>();
        this.token=null;

        URL proxy = null;
        if((srfProxy!= null) && (srfProxy.length() != 0)) {
            proxy = new URL(srfProxy);
        }

        try{
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,new SrfTrustManager[]{_trustMgr }, null);
            SSLContext.setDefault(sslContext);
            socketFactory = sslContext.getSocketFactory();
            this.srfClient = new SrfClient(buildLogger, srfAddress, tenant, socketFactory, proxy);
        }
        catch (NoSuchAlgorithmException | KeyManagementException e){
            buildLogger.addErrorLogEntry("StartRun failed:" + e.getMessage());
        }

        jobIds = null;
        try {
            srfClient.login(this.clientId, this.clientSecret);
            this.token = srfClient.getAccessToken();
            this.workspaceId = srfClient.getWorkspaceId();
            initSrfEventListener();
            jobIds = executeTestsSet();
        } catch (UnknownHostException | ConnectException | SSLHandshakeException | IllegalArgumentException | AuthorizationException | AuthenticationException e) {
            cleanUp();
            e.printStackTrace();
            buildLogger.addErrorLogEntry(String.format("ERROR: Failed logging into SRF server: %s %s", srfAddress, e));
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        } catch (IOException | SrfException e) {
            cleanUp();
            e.printStackTrace();
            buildLogger.addErrorLogEntry(String.format("ERROR: Failed executing test, %s", e));
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        }

        try {
            return this.srfExecutionFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry(String.format("ERROR: Failed executing test, %s", e));
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            buildLogger.addErrorLogEntry(String.format("ERROR: Failed executing test, %s", e));

            if (!jobIds.isEmpty()) {
                for (int i = 0; i < jobIds.size(); i++) {
                    String jobId = jobIds.get(i).toString();
                    srfClient.cancelJob(jobId.toString());
                }
            }
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        } finally {
            cleanUp();
        }
    }

    private void handleSrfAddress(String address) throws MalformedURLException {
        boolean https = true;
        if (!address.startsWith("https://")) {
            if (!address.startsWith("http://")) {
                String tmp = address;
                address = "https://";
                address = address.concat(tmp);
            } else
                https = false;
        }
        URL urlTmp = new URL(address);
        if (urlTmp.getPort() == -1) {
            if (https)
                address = address.concat(":443");
            else
                address = address.concat(":80");
    }

        srfAddress = address;
    }

    private JSONArray executeTestsSet() throws IOException, SrfException, AuthorizationException {
        JSONObject requestBody = createExecutionReqBody();
        JSONArray jobs = srfClient.executeTestsSet(requestBody);
        if (jobs == null || jobs.size() == 0)
            throw new SrfException(String.format("No tests found for %s", this.testIds != null && !this.testIds.equals("") ? "test id: " + this.testIds : "test tags: " + this.tags));
        return getJobIds(jobs);
    }

    private JSONObject createExecutionReqBody() throws IOException, SrfException {
        JSONObject data = new JSONObject();
        JSONObject testParams = new JSONObject();

        if (testIds != null && !testIds.isEmpty()) {
            String[] normalizedTestIds = normalizeParam(this.testIds);
            data.put("testYac", normalizedTestIds);
        } else if (tags != null && !tags.isEmpty()) {
            String[] tagNames = normalizeParam(tags);
            data.put("tags", tagNames);
        } else
            throw new SrfException("Both test ids and test tags are empty");

        if (tunnel != null && tunnel.length() > 0) {
            data.put("tunnelName", tunnel);
        }

        if(data.size() == 0){
            throw new IOException("Wrong filter");
        }

        testParams.put("filter", data);
        if (build != null && build.length() > 0) {
            data.put("build", build);
            buildLogger.addBuildLogEntry(String.format("Required build: %s", build));
        }
        if (release != null && release.length() > 0){
            data.put("release", release);
            buildLogger.addBuildLogEntry(String.format("Required release: %s", release));
        }

        HashMap<String, String> paramObj = new HashMap<>();
        int cnt = 0;

        if (parameters != null && !parameters.isEmpty()) {
            cnt = parameters.size();
            if (cnt > 0)
                buildLogger.addBuildLogEntry("Parameters:");
            for (int i = 0; i < cnt; i++) {
                String name = parameters.get(i).getSrfParamName();
                String val =  parameters.get(i).getSrfParamValue();
                paramObj.put(name, val);
                buildLogger.addBuildLogEntry(String.format("%1s : %2s", name, val));
            }
        }

        if (cnt > 0)
            data.put("params", paramObj);

        return data;
    }

    private String[] normalizeParam(String paramToNormalize) {
        String[] params = paramToNormalize.split(",");
        for (int i = 0; i < params.length; i++) {
            // Normalize param
            String param = params[i];
            params[i] = param.trim();
        }
        return params;
    }

    private JSONArray getJobIds(JSONArray jobs) {
        JSONArray jobIds = new JSONArray();
        int cnt = jobs.size();
        for (int k = 0; k < cnt; k++ ){
            JSONObject job = jobs.getJSONObject(k);
            JSONObject jobExecutionError = job.getJSONObject("error");
            if (jobExecutionError.size() != 0) {
                JSONObject errorParameters = jobExecutionError.getJSONObject("parameters");
                jobIds.add(errorParameters.getString("jobId"));
                runningCount.add(errorParameters.getString("testRunId"));
            } else {
                jobIds.add(job.getString("jobId"));
                runningCount.add(job.getString("testRunId"));
            }
        }
        return jobIds;
    }

    @Override
    public void update(Observable o, Object eventType) {
        SrfSseEventNotification srfSseEventNotification = (SrfSseEventNotification) eventType;
        switch (srfSseEventNotification.srfTestRunEvent) {
            case TEST_RUN_END:
                boolean removed = this.runningCount.remove(srfSseEventNotification.testRunId);
                if (!removed){
                    buildLogger.addErrorLogEntry(String.format("Received TEST_RUN_END event for non existing run %s", srfSseEventNotification.testRunId));
                    return;
                }

                if (runningCount.size() > 0)
                    return;
                break;
            default:
                return;
        }

        JSONArray testRes;
        try {
            testRes = srfClient.getTestRuns(jobIds);
            String status = getBuildStatus(testRes);

            SrfResultFileWriter resultWriter = new SrfResultFileWriter(taskContext, buildLogger);
            resultWriter.writeHtmlReport(testRes, tenant, srfAddress, workspaceId);

            ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl("Build_" + String.valueOf(taskContext.getBuildContext().getBuildNumber()) + "_reports",false,null);
            artifact.setCopyPattern("**/*");
            String location  = "Report_" + taskContext.getBuildContext().getBuildNumber();
            artifact.setLocation(location);
            taskContext.getBuildContext().getArtifactContext().getDefinitionContexts().add(artifact);

            switch (status) {
                case "success":
                    this.srfExecutionFuture.complete(TaskResultBuilder.newBuilder(taskContext).success().build());
                    return;
                case "errored":
                    this.srfExecutionFuture.complete(TaskResultBuilder.newBuilder(taskContext).failedWithError().build());
                    return;
                case "canceled":
                case "failed":
                    this.srfExecutionFuture.complete(TaskResultBuilder.newBuilder(taskContext).failed().build());
                    return;
                default:
                    buildLogger.addBuildLogEntry(String.format("ExecutionComponent.update: received undefined build result: %s", taskContext.getBuildContext().getBuildResult().toString()));
                    this.srfExecutionFuture.complete(TaskResultBuilder.newBuilder(taskContext).failedWithError().build());
                    break;
            }

        } catch (Exception e) {
            buildLogger.addErrorLogEntry("Update error:" + e.getMessage());
            this.srfExecutionFuture.complete(TaskResultBuilder.newBuilder(taskContext).failedWithError().build());
        } finally {
            cleanUp();
        }
    }

    private void initSrfEventListener() throws IllegalArgumentException, MalformedURLException {
        // !!! Important Notice !!!
        // by using 'level=session' in the sse request we're ensuring that we'll get only events related
        // to this run since we're creating a new session (token) for each run
        EventSource.Builder builder;
        String queryParams =  "?level=session&types=test-run-started,test-run-ended,test-run-count,script-step-updated,script-step-created,script-run-started,script-run-ended";
        String urlSse = srfAddress.concat(String.format("/rest/sse/workspaces/%s/events", workspaceId)).concat(queryParams).concat("&access-token=").concat(token);

        if((proxy!= null) && (proxy.length() != 0)) {
            URL proxyURL = new URL(proxy);
            String proxyHost = proxyURL.getHost();
            int proxyPort = proxyURL.getPort();
            builder = new EventSource.Builder(this.sseEventListener, URI.create(urlSse)).proxy(proxyHost,proxyPort);
        } else {
            builder = new EventSource.Builder(this.sseEventListener, URI.create(urlSse));
        }

        EventSource eventSource = builder.build() ;
        eventSource.setReconnectionTimeMs(3000);
        eventSource.start();
    }

    private void cleanUp() {
        if (eventSrc != null) {
            eventSrc.close();
            eventSrc = null;
        }

        if (con != null){
            con.disconnect();
            con = null;
        }

        try{
            if(shouldCloseTunnel && CreateTunnelComponent.Tunnels != null && CreateTunnelComponent.Tunnels.size() > 0){
                for (Process p:CreateTunnelComponent.Tunnels){
                    p.destroy();
                }
                CreateTunnelComponent.Tunnels.clear();
            }
        } catch (Exception e){ }

    }

    private String getBuildStatus(JSONArray report) {
        int testsCnt = report.size();
        int testRunErrors = 0;
        int testRunCancellations = 0;
        int successfulTestRun = 0;
        String buildStatus = "error";

        for (int i = 0; i < testsCnt; i++) {

            JSONObject test = (JSONObject) (report.get(i));
            String status = test.getString("status");
            if (status == null)
                status = "error";

            switch (status) {
                case "success":
                case "completed":
                    successfulTestRun++;
                    break;
                case "canceled":
                    testRunCancellations++;
                    break;
                default:
                    testRunErrors++;
                    break;
            }

            if (successfulTestRun == testsCnt) {
                buildStatus = "success";
            } else if (testRunErrors > 0) {
                buildStatus = "error";
            } else if (testRunCancellations > 0) {
                buildStatus = "canceled";
            }
        }

        buildLogger.addBuildLogEntry(String.format("Returning build status: %s", buildStatus));
        return buildStatus;
    }
}


