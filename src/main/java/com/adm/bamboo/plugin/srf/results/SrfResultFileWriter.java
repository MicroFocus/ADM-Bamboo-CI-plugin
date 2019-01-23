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

package com.adm.bamboo.plugin.srf.results;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SrfResultFileWriter {
    private File reportDir;

    public SrfResultFileWriter(TaskContext taskContext, BuildLogger buildLogger){
        reportDir = new File(taskContext.getRootDirectory().getPath()  + File.separator + "Report_" + taskContext.getBuildContext().getBuildNumber());
        reportDir.mkdirs();
    }

    public void writeHtmlReport(JSONArray tests, String tenant, String srfAddress, String workspaceId) throws IOException {
        String path = reportDir.getPath().concat(File.separator).concat("report.html");
        File htmlReportFile = new File(path);
        Boolean fileCreated = htmlReportFile.createNewFile();

        if (!fileCreated)
            throw new IOException(String.format("Failed to create file: %s", path));

        int testsCount = tests.size();

        List<String> testsName = new ArrayList<>();
        List<String> testsStatus = new ArrayList<>();
        List<String> testsDuration = new ArrayList<>();
        List<String> testsLink = new ArrayList<>();
        List<String> testsEnvs = new ArrayList<>();

        for (int i = 0; i < testsCount; i++) {

            JSONObject test = (JSONObject) (tests.get(i));

            testsName.add(test.getString("name"));
            testsStatus.add(test.getString("status").toUpperCase());
            testsDuration.add(test.getString("durationMs"));
            testsEnvs.add(getTestEnvironments(test));

            String testRunYac = test.getString("yac");
            String srfTestRunResultUrl = String.format("%s/workspace/%s/results/%s/details?TENANTID=%s\n",srfAddress, workspaceId, testRunYac, tenant);
            testsLink.add(srfTestRunResultUrl);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("<html>" +
                "<style> " +
                "body: {width: 100%; height: 100%; margin: 0; padding: 0;}" +
                ".main-container {width: 100%; max-width: 1366px; height:100%; margin: 0 auto; padding-top: 5px}" +
                "table {width: 80%;}" +
                "table, th, td { border: 1px solid #f3f3f3; border-collapse: collapse; padding: 6px}" +
                "thead { background-color: #eee; font-size: 20px; color: #3b3b3b; text-align: left;}" +
                "tbody { text-align: left; }" +
                "h1 {font-size: 34px}" +
                "</style>" +
                "<body>" +
                    "<div class=main-container>" +
                        "<h1>StormRunner Functional Result</h1>" +
                        "<table>" +
                            "<thead>" +
                                "<tr>" +
                                    "<th>Test Name</th>" +
                                    "<th>Duration</th>" +
                                    "<th>Environment</th>" +
                                    "<th>Status</th>" +
                                "</tr>" +
                            "</thead>"

                );
                for (int i = 0; i < testsName.size(); i++) {
                    buf.append("<tr><td>")
                            .append(testsName.get(i))
                            .append("</td><td>")
                            .append(testsDuration.get(i) + " ms")
                            .append("</td><td>")
                            .append(testsEnvs.get(i))
                            .append("</td><td>")
                            .append(String.format("<a href=%s target=_blank>%s</a>", testsLink.get(i), testsStatus.get(i)))
                            .append("</td></tr>");
                }

        buf.append("</table>" +
                "</div>" +
                "</body>" +
                "</html>");

        String html = buf.toString();

        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(htmlReportFile);
            fs.write(html.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            cleanUp(fs);
        }
    }

    private String getTestEnvironments(JSONObject test) {
        String envs = "";
        JSONArray scriptRunsJson = test.getJSONArray("scriptRuns");

        for (int i = 0; i< scriptRunsJson.size(); i++){
            JSONObject scriptRun = (JSONObject) (scriptRunsJson.get(i));
            JSONObject environment = scriptRun.getJSONObject("environment");
            JSONObject os = environment.getJSONObject("os");
            JSONObject browser = environment.getJSONObject("browser");
            envs += String.format("%1s %1s %1s %1s <br>", os.getString("name"), os.getString("version"), browser.getString("name"), browser.getString("version"));
        }

        return envs;
    }

    private static void cleanUp(FileOutputStream fs) {
        if (fs == null)
            return;

        try {
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}

