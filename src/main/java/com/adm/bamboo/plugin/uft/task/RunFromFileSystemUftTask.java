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

package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.bamboo.plugin.uft.helpers.LauncherParamsBuilder;
import com.adm.utils.uft.integration.HttpConnectionException;
import com.adm.utils.uft.integration.JobOperation;
import com.adm.utils.uft.enums.RunType;
import com.adm.bamboo.plugin.uft.results.ResultInfoItem;
import com.adm.bamboo.plugin.uft.results.TestResultHelperFileSystem;
import com.adm.utils.uft.enums.ResultTypeFilter;
import com.adm.utils.uft.enums.UFTConstants;
import com.amazonaws.util.StringUtils;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static com.adm.bamboo.plugin.uft.results.TestResultHelper.collateTestResults;
import static com.adm.utils.uft.FilesHandler.getOutputFilePath;
import static com.adm.utils.uft.FilesHandler.zipResult;

public class RunFromFileSystemUftTask implements AbstractLauncherTask {
    private final I18nBean i18nBean;
    private final TestCollationService testCollationService;

    public RunFromFileSystemUftTask(@NotNull final TestCollationService testCollationService, @NotNull final I18nBeanFactory i18nBeanFactory) {
        this.i18nBean = i18nBeanFactory.getI18nBean();
        this.testCollationService = testCollationService;
    }

    /**
     * Get task properties
     *
     * @param taskContext
     * @return
     * @throws Exception
     */
    public Properties getTaskProperties(final TaskContext taskContext) throws Exception {
        final String splitMarker = "\n";
        final ConfigurationMap map = taskContext.getConfigurationMap();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        LauncherParamsBuilder builder = new LauncherParamsBuilder();
        builder.setRunType(RunType.FILE_SYSTEM);

        String timeout = map.get(UFTConstants.TIMEOUT.getValue());
        builder.setPerScenarioTimeOut(timeout);

        boolean useMC = BooleanUtils.toBoolean(map.get(UFTConstants.USE_MC_SETTINGS.getValue()));
        if (useMC) {
            addMobileCenterSettings(builder, map, buildLogger);
        }

        String tests = map.get(UFTConstants.TESTS_PATH.getValue());
        String[] testNames = (tests == null) ? new String[0] : tests.split(splitMarker);
        for (int i = 0; i < testNames.length; i++) {
            builder.setTest(i + 1, testNames[i]);
        }

        return builder.getProperties();
    }

    /**
     * If "use mobile center" option is checked add the mobile center settings to task properties set
     *
     * @param builder
     * @param map
     * @param buildLogger
     */
    private void addMobileCenterSettings(final LauncherParamsBuilder builder, final ConfigurationMap map, final BuildLogger buildLogger) {
        String proxyAddress = null;
        String proxyUserName = null;
        String proxyPassword = null;

        String mcServerUrl = map.get(UFTConstants.MC_SERVER_URL.getValue());
        String mcUserName = map.get(UFTConstants.MC_USERNAME.getValue());
        String mcPassword = map.get(UFTConstants.MC_PASSWORD.getValue());

        boolean useSSL = BooleanUtils.toBoolean(map.get(UFTConstants.USE_SSL.getValue()));
        builder.setMobileUseSSL(useSSL ? 1 : 0);

        if (useSSL) {
            buildLogger.addBuildLogEntry("********** Use SSL ********** ");
        }

        boolean useProxy = BooleanUtils.toBoolean(map.get(UFTConstants.USE_PROXY.getValue()));
        builder.setMobileUseProxy(useProxy ? 1 : 0);

        if (useProxy) {
            buildLogger.addBuildLogEntry("********** Use Proxy ********** ");
            builder.setMobileProxyType(2);
            proxyAddress = map.get(UFTConstants.PROXY_ADDRESS.getValue());

            //proxy info
            if (proxyAddress != null) {
                builder.setMobileProxySetting_Address(proxyAddress);
            }

            Boolean specifyAuthentication = BooleanUtils.toBoolean(UFTConstants.SPECIFY_AUTHENTICATION.getValue());
            builder.setMobileProxySetting_Authentication(specifyAuthentication ? 1 : 0);

            if (specifyAuthentication) {
                proxyUserName = map.get(UFTConstants.PROXY_USERNAME.getValue());
                proxyPassword = map.get(UFTConstants.PROXY_PASSWORD.getValue());

                if (proxyUserName != null && proxyPassword != null) {
                    builder.setMobileProxySetting_UserName(proxyUserName);
                    builder.setMobileProxySetting_Password(proxyPassword);
                }
            }
        } else {
            builder.setMobileProxyType(0);
        }

        if (!mcInfoCheck(mcServerUrl, mcUserName, mcPassword)) {
            //url name password
            builder.setServerUrl(mcServerUrl);
            builder.setUserName(mcUserName);
            builder.setFileSystemPassword(mcPassword);

            //write the specified job info(json type) to properties
            JobOperation operation = new JobOperation(mcServerUrl, mcUserName, mcPassword, proxyAddress, proxyUserName, proxyPassword);
            builder.setMobileInfo(addMCJobInfoToTaskProperties(operation, map, buildLogger));
        }
    }

    /**
     * Add job info for MC to task properties set
     *
     * @param operation
     * @param map
     * @param buildLogger
     * @return
     */
    private String addMCJobInfoToTaskProperties(final JobOperation operation, final ConfigurationMap map, final BuildLogger buildLogger) {
        String mobileInfo = null;
        String jobUUID = map.get(UFTConstants.JOB_UUID.getValue());

        if (jobUUID != null) {
            JSONObject jobJSON = null;
            try {
                jobJSON = operation.getJobById(jobUUID);
            } catch (HttpConnectionException e) {
                buildLogger.addErrorLogEntry("********** Fail to connect mobile center, please check URL, UserName, Password, and Proxy Configuration ********** ");
            }

            if (jobJSON != null) {
                JSONObject dataJSON = (JSONObject) jobJSON.get("data");
                if (dataJSON != null) {
                    JSONObject applicationJSONObject = (JSONObject) dataJSON.get("application");
                    if (applicationJSONObject != null) {
                        applicationJSONObject.remove(UFTConstants.ICON.getValue());
                    }
                    JSONArray extArr = (JSONArray) dataJSON.get("extraApps");
                    if (extArr != null) {
                        Iterator<Object> iterator = extArr.iterator();
                        while (iterator.hasNext()) {
                            JSONObject extAppJSONObject = (JSONObject) iterator.next();
                            extAppJSONObject.remove(UFTConstants.ICON.getValue());
                        }
                    }
                }
                mobileInfo = dataJSON.toJSONString();
            }
        }
        return mobileInfo;
    }

    /**
     * Execute task
     *
     * @param taskContext
     * @return
     * @throws TaskException
     */
    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        return AbstractLauncherTask.super.execute(taskContext);
    }

    /**
     * Run build task
     *
     * @param workingDirectory
     * @param launcherPath
     * @param paramFile
     * @param logger
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public Integer runTask(final File workingDirectory, final String launcherPath, final String paramFile,
                           final BuildLogger logger) throws IOException, InterruptedException {
        return AbstractLauncherTask.super.runTask(workingDirectory, launcherPath, paramFile, logger);
    }

    /**
     * Collate results
     *
     * @param taskContext
     * @return
     */
    @Override
    public TaskResult collateResults(@NotNull final TaskContext taskContext, final Properties mergedProperties) {
        try {
            collateTestResults(testCollationService, taskContext);
            prepareArtifacts(taskContext, mergedProperties);
            return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        } catch (Exception ex) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }

    /**
     * Prepare results reports as archive
     *
     * @param taskContext
     */
    private void prepareArtifacts(final TaskContext taskContext, final Properties mergedProperties) {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        File resultFile = new File(taskContext.getWorkingDirectory(), mergedProperties.get("resultsFilename").toString());

        ResultTypeFilter resultsFilter = getResultTypeFilter(taskContext);
        if (resultsFilter == null) {
            return;
        }

        final String resultNameFormat = i18nBean.getText(UFTConstants.ARTIFACT_NAME_FORMAT.getValue());
        Collection<ResultInfoItem> resultsPaths = TestResultHelperFileSystem.getTestResults(resultFile, resultsFilter, resultNameFormat, taskContext, buildLogger);

        for (ResultInfoItem resultItem : resultsPaths) {
            String dir = resultItem.getSourceDir().getPath();
            File fileReport = new File(dir, UFTConstants.RESULT_HTML_REPORT_FILE_NAME.getValue());
            if (fileReport.exists()) {
                prepareHtmlArtifact(resultItem, taskContext, buildLogger);
            } else {
                zipResult(resultItem, buildLogger);
            }
        }

        for (ResultInfoItem resultItem : resultsPaths) {
            zipResult(resultItem, buildLogger);
        }
    }

    /**
     * Prepare results reports as HTML report
     *
     * @param resultItem
     * @param taskContext
     * @param logger
     */
    private void prepareHtmlArtifact(final ResultInfoItem resultItem, final TaskContext taskContext, final BuildLogger logger) {
        File contentDir = resultItem.getSourceDir();
        if (contentDir == null || !contentDir.isDirectory()) {
            return;
        }
        File destPath = new File(getOutputFilePath(taskContext), resultItem.getTestName());
        if (!destPath.exists() && !destPath.isDirectory()) {
            destPath.mkdirs();
        }

        try {
            FileUtils.copyDirectoryToDirectory(contentDir, destPath);
        } catch (Exception e) {
            logger.addBuildLogEntry(e.getMessage());
            return;
        }

        String content = getContent(contentDir.getName(), UFTConstants.RESULT_HTML_REPORT_FILE_NAME.getValue(), UFTConstants.HTML_REPORT_FILE_NAME.getValue());

        try {
            FileUtils.writeStringToFile(new File(destPath, UFTConstants.HTML_REPORT_FILE_NAME.getValue()), content);
        } catch (IOException e) {
        }
    }


    /**
     * Choose a publish mode (either publish only failed tests, either all tests)
     *
     * @param taskContext
     * @return
     */
    private ResultTypeFilter getResultTypeFilter(final TaskContext taskContext) {
        String publishMode = taskContext.getConfigurationMap().get(UFTConstants.PUBLISH_MODE_PARAM.getValue());

        if (publishMode.equals(UFTConstants.PUBLISH_MODE_FAILED_VALUE.getValue())) {
            return ResultTypeFilter.FAILED;
        }

        return ResultTypeFilter.All;
    }

    private boolean mcInfoCheck(final String mcUrl, final String mcUserName, final String mcPassword) {
        return StringUtils.isNullOrEmpty(mcUrl) || StringUtils.isNullOrEmpty(mcUserName) || StringUtils.isNullOrEmpty(mcPassword);
    }

    private String getContent(final String contentDirectoryName, final String resultFileName, final String reportFileName) {
        String content =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Test</title>\n" +
                        "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                        "        <script type=\"text/javascript\">\n" +
                        "        function codeAddress() {\n" +
                        "		 	var currentUrl = window.location.toString();\n" +
                        "			var replaceString = '" + contentDirectoryName + "/" + resultFileName + "';\n" +
                        "		 	currentUrl = currentUrl.replace('" + reportFileName + "', replaceString);\n" +
                        "        	window.location = currentUrl;\n" +
                        "        }\n" +
                        "        window.onload = codeAddress;\n" +
                        "        </script>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "   \n" +
                        "    </body>\n" +
                        "</html>";

        return content;
    }
}

