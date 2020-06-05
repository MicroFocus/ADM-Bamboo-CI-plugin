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

package com.adm.bamboo.plugin.uft.results;

import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.enums.UFTConstants;
import com.adm.utils.uft.sdk.ALMRunReportUrlBuilder;
import com.adm.utils.uft.sdk.Client;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.interceptors.StringMatchingInterceptor;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.adm.utils.uft.FilesHandler.getOutputFilePath;

public class TestResultHelperAlm {
    private static final String CAN_NOT_SAVE_RUN_LOG_MESSAGE = "RunFromAlmTask.error.canNotSaveTheRunLog";
    private static final String RUN_LOG_FILE_NAME = "RunLog";
    private static final String ALM_RUN_RESULTS_LINK_PARAMETER = "ALM_RUN_RESULTS_LINK_PARAMETER";

    private static final String RUN_LOG_HTML_TEXT =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "    <head>\n" +
                    "        <title>Test</title>\n" +
                    "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "        <script type=\"text/javascript\">\n" +
                    "        function codeAddress() {\n" +
                    "            window.location = ALM_RUN_RESULTS_LINK_PARAMETER;\n" +
                    "        }\n" +
                    "        window.onload = codeAddress;\n" +
                    "        </script>\n" +
                    "    </head>\n" +
                    "    <body>\n" +
                    "   \n" +
                    "    </body>\n" +
                    "</html>";

    private static List<String> savedALMRunLogPaths = new ArrayList<String>();
    private static int currentBuildNumber;


    public static void AddALMArtifacts(final TaskContext taskContext, final File resultFile,
                                       final String linkSearchFilter, final I18nBean i18nBean) {
        clearSavedALMRunLogPaths(taskContext);
        String taskName = taskContext.getConfigurationMap().get(UFTConstants.TASK_NAME.getValue());

        if (taskName.equals(i18nBean.getText(UFTConstants.ALM_LAB_TASK_NAME.getValue()))) {//Run from Alm Lab Management
            String taskRunLogPath = findRequiredStringFromLog(taskContext, linkSearchFilter);
            if (StringUtils.isNullOrEmpty(taskRunLogPath)) {
                taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText(CAN_NOT_SAVE_RUN_LOG_MESSAGE));
                return;
            }

            if(taskRunLogPath.contains("processRunId")) {//old version of ALM
                createResultFile(taskContext, taskRunLogPath, ".*processRunId=", i18nBean);
            } else {//new version of ALM
                createResultFile(taskContext, taskRunLogPath, ".*/", i18nBean);
            }

        } else if (taskName.equals(i18nBean.getText(UFTConstants.ALM_TASK_NAME.getValue()))) {//Run from ALM
            List<String> links = null;
            if (resultFile != null && resultFile.exists()) {
                links = findRequiredStringsFromFile(taskContext.getBuildLogger(), resultFile);
            }
            if (links == null || links.size() < 1) {
                links = findRequiredStringsFromLog(taskContext.getBuildLogger(), linkSearchFilter);
                taskContext.getBuildLogger().addBuildLogEntry("link is: " + links.get(0));
            }
            Integer linksAmount = links.size();
            if (linksAmount.equals(0)) {
                taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText("ERROR ****** " + CAN_NOT_SAVE_RUN_LOG_MESSAGE));
                return;
            }

            for (String link : links) {
                createResultFile(taskContext, link, ".*EntityID=", i18nBean);
            }
        }
    }

    private static void clearSavedALMRunLogPaths(TaskContext taskContext) {
        int taskBuildNumber = taskContext.getBuildContext().getBuildNumber();

        if (savedALMRunLogPaths.size() > 0 && taskBuildNumber != currentBuildNumber) {
            savedALMRunLogPaths.clear();
        }
        currentBuildNumber = taskBuildNumber;
    }

    //is used for Run from Alm Lab Management task
    private static String findRequiredStringFromLog(TaskContext taskContext, String searchFilter) {
        BuildLogger logger = taskContext.getBuildLogger();

        StringMatchingInterceptor interceptor = new StringMatchingInterceptor(searchFilter, true);
        List<LogEntry> buildLog = Lists.reverse(logger.getLastNLogEntries(100));
        for (LogEntry logEntry : buildLog) {
            interceptor.intercept(logEntry);
            if(interceptor.hasMatched()){
                String log = logEntry.getLog();
                int pathBegin = log.indexOf("http");
                if (pathBegin > -1) {
                    log = log.substring(pathBegin);
                    if (!savedALMRunLogPaths.contains(log)) {
                        return log;
                    }
                }
            }
        }

        return null;
    }

    //is used for Run from Alm task
    private static List<String> findRequiredStringsFromLog(BuildLogger logger, String searchFilter) {
        StringMatchingInterceptor interceptor = new StringMatchingInterceptor(searchFilter, true);
        List<LogEntry> buildLog = Lists.reverse(logger.getLastNLogEntries(100));
        List<String> results = new ArrayList<String>();
        for (LogEntry logEntry : buildLog) {
            interceptor.intercept(logEntry);
            if(interceptor.hasMatched()){
                String log = logEntry.getLog();
                int pathBegin = log.indexOf("td:");
                if (pathBegin > -1) {
                    String result = log.substring(pathBegin);
                    if (!results.contains(result) && !savedALMRunLogPaths.contains(result)) {
                        results.add(result);
                    }
                }
            }
        }

        return results;
    }

    private static List<String> findRequiredStringsFromFile(BuildLogger logger, File resultFile) {
        List<String> results = new ArrayList<String>();
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(resultFile.getAbsoluteFile()));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                }
            } finally {
                in.close();
            }
            //report link example: td://Automation.AUTOMATION.mydph0271.hpswlabs.adapps.hp.com:8080/qcbin/TestLabModule-000000003649890581?EntityType=IRun&amp;EntityID=1195091
            String sp = "td://.+?;EntityID=[0-9]+";
            Pattern p = Pattern.compile(sp);
            Matcher m = p.matcher(sb.toString());
            while (m.find()) {
                results.add(m.group());
            }
        } catch (Exception e) {
            logger.addBuildLogEntry(e.getMessage());
        }
        return results;
    }

    private static void createResultFile(TaskContext taskContext, String link, String idFilter, I18nBean i18nBean) {
        savedALMRunLogPaths.add(link);
        String RunReportFileId = link.replaceAll(idFilter, "");
        if (StringUtils.isNullOrEmpty(RunReportFileId)) {
            return;
        }
        String RunReportFileName = RUN_LOG_FILE_NAME + RunReportFileId + ".html";
        String workingDirectory = getOutputFilePath(taskContext);
        File resultFile = new File(workingDirectory + "/" + RunReportFileName);
        link = "\"" + link + "\"";

        String parameterizedResultsHtmlText = RUN_LOG_HTML_TEXT.replaceAll(ALM_RUN_RESULTS_LINK_PARAMETER, link);
        try {
            FileUtils.writeStringToFile(resultFile, parameterizedResultsHtmlText);
        } catch (Exception ex) {
            taskContext.getBuildLogger().addErrorLogEntry(i18nBean.getText(CAN_NOT_SAVE_RUN_LOG_MESSAGE));
        }
    }
}
