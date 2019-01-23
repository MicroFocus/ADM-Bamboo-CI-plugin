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

package com.adm.bamboo.plugin.srf.impl;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;

import java.io.*;
import java.util.ArrayList;

public class CreateTunnelComponent {
    private TaskContext taskContext;
    private BuildLogger buildLogger;

    private String tunnelClientPath;
    private String configFilePath;

    static final ArrayList<Process> Tunnels = new ArrayList<Process>();

    public CreateTunnelComponent(TaskContext taskContext, BuildLogger buildLogger, String tunnelClientPath, String configFilePath) {
        this.configFilePath = configFilePath;
        this.tunnelClientPath = tunnelClientPath;
        this.buildLogger = buildLogger;
        this.taskContext = taskContext;
    }

    public TaskResult startRun() throws InterruptedException, IOException {

        String config = String.format("-config=%s", configFilePath);

        ProcessBuilder pb = new ProcessBuilder(tunnelClientPath,  config, "-reconnect-attempts=3", "-log-level=info", "-log=stdout");
        pb.redirectOutput();
        buildLogger.addBuildLogEntry("Launching "+ tunnelClientPath + " " + config );
        String[] cmdArray = { tunnelClientPath , config, "-reconnect-attempts=3", "-log-level=info", "-log=stdout"};
        Process p = Runtime.getRuntime().exec(cmdArray);
        TunnelTracker tracker = new TunnelTracker(buildLogger, p);
        java.lang.Thread th = new Thread(tracker, "trackeer");
        Tunnels.add(p);

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while(true){

            while ((line = br.readLine()) != null) {
                buildLogger.addBuildLogEntry(line);
                if(line.contains("established at")){
                    th.start();
                    buildLogger.addBuildLogEntry("Launched "+ tunnelClientPath);
                    return TaskResultBuilder.newBuilder(taskContext).success().build();
                }
                Thread.sleep(100);
                int exitVal = 0;
                try{
                    exitVal = p.exitValue();
                }
                catch (Exception e){
                    continue;
                }

                switch (exitVal) {
                    case 0:
                        buildLogger.addBuildLogEntry("Tunnel client terminated by the user or the server");
                        return TaskResultBuilder.newBuilder(taskContext).success().build();
                    case 1:
                        buildLogger.addErrorLogEntry("Failed to launch tunnel client : unplanned failure");
                        break;
                    case 2:
                        buildLogger.addErrorLogEntry("Failed to launch tunnel client : Authentication with client/secret failed");
                        break;
                    case 3:
                        buildLogger.addErrorLogEntry("Failed to launch tunnel client : Max connection attempts acceded");
                        break;
                    case 4:
                        buildLogger.addErrorLogEntry("Failed to launch tunnel client : Allocation of tunnel failed E.g. Tunnel name is not unique.\nPlease check if tunnel is already running");
                        break;
                    default:
                        buildLogger.addErrorLogEntry(String.format("Failed to launch tunnel client : Unknown reason(Exit code =%d)", p.exitValue()));
                        break;

                }
                buildLogger.addBuildLogEntry("Closing tunnel process");
                p.destroy();
                return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
            }
        }
    }

    private class TunnelTracker implements Runnable {
        BuildLogger buildLogger;
        Process p;

        public TunnelTracker(BuildLogger log, Process p){
            this.buildLogger = log;
            this.p=p;
        }

        @Override
        public  void run() {
            int exitValue =0;
            while(true) {
                try {
                    //Read out dir output
                    InputStream is = p.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        // logger.println(line);
                    }
                    try {
                        exitValue = p.exitValue();
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                } catch (Exception e) {
                    buildLogger.addBuildLogEntry(e.getMessage());
                }
                //Wait to get exit value
                try {
                    p.waitFor();
                    buildLogger.addBuildLogEntry("\n\nTunnel exit value is " + exitValue);
                    return;
                } catch (final InterruptedException e) {
                    p.destroy();
                    return;
                }
            }
        }
    }
}