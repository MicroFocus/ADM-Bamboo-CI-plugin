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
 * its affiliates and licensors ("Open Text") are as may be set forth
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

package com.adm.utils.uft;

import com.adm.bamboo.plugin.uft.results.ResultInfoItem;
import com.adm.utils.uft.sdk.DirectoryZipHelper;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.adm.utils.uft.enums.UFTConstants.TASK_NAME;

public final class FilesHandler {
    private static final String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
    private static final String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";
    private static final String HP_UFT_PREFIX = "UFT_Build_";
    private static final String FORMATTER_PATTERN = "ddMMyyyyHHmmssSSS";
    private static final String BAMBOO_BUILD_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private FilesHandler() {
    }

    /**
     * Archive test reports
     *
     * @param resultItem contains info about one result
     * @param logger
     */
    public static void zipResult(final ResultInfoItem resultItem, final BuildLogger logger) {
        try {
            DirectoryZipHelper.zipFolder(resultItem.getSourceDir().getPath(), resultItem.getZipFile().getPath());
        } catch (Exception ex) {
            logger.addBuildLogEntry(ex.getMessage());
        }
    }

    /**
     * Build the properties file that contains the parameters needed by the launcher tool
     *
     * @param taskContext
     * @param workingDirectory
     * @param mergedProperties
     * @param buildLogger
     * @return the properties file
     */
    public static File buildPropertiesFile(final TaskContext taskContext, final File workingDirectory,
                                           final Properties mergedProperties, String buildTimeStamp, final BuildLogger buildLogger) {

        DateFormat sourceDateFormat = new SimpleDateFormat(BAMBOO_BUILD_TIMESTAMP_PATTERN);
        try {
            Date buildDateTime = sourceDateFormat.parse(buildTimeStamp);
            DateFormat destDateFormat = new SimpleDateFormat(FORMATTER_PATTERN);
            buildTimeStamp = destDateFormat.format(buildDateTime);
        } catch (ParseException e) {
            buildLogger.addBuildLogEntry("Unable to parse object");
            e.printStackTrace();
        }

        String paramFileName = "props" + buildTimeStamp + ".txt";
        String resultsFileName = "Results" + buildTimeStamp + ".xml";
        mergedProperties.put("resultsFilename", resultsFileName);

        File resultFile = new File(workingDirectory, resultsFileName);
        File paramsFile = new File(workingDirectory, paramFileName);
        if (paramsFile.exists()) {
            paramsFile.delete();
        }
        try {
            paramsFile.createNewFile();
            FileOutputStream outputFile = new FileOutputStream(paramsFile);
            mergedProperties.store(outputFile, "");
            outputFile.close();
        } catch (Exception e) {
            TaskUtils.logErrorMessage(e, buildLogger, taskContext);
        }

        return paramsFile;
    }

    /**
     * Copies a resource file of the plugin to the provided build directory path. A resource file can be one of the following:
     * HpToolsLauncher.exe
     * HpToolsAborter.exe
     * LRAnalysisLauncher.exe
     *
     * @param pathToExtract
     * @param resourceName  name of the resource file
     * @return empty string if no error occurs or the corresponding error message
     * @throws IOException
     */
    public static String extractBinaryResource(final InputStream stream, final File pathToExtract, final String resourceName) throws IOException {
        OutputStream resStreamOut = null;
        String resourcePath = UFTConstants.TOOLS_PATH.getValue() + "/" + resourceName;
        try {
            if (stream == null) {
                return "Cannot get resource \"" + resourcePath + "\" from Jar file.";
            }

            byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream(new File(pathToExtract, resourceName));
            Integer readBytes;
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            return ex.getMessage();
        } finally {
            if (stream != null) {
                stream.close();
                resStreamOut.close();
            }
        }
        return "";
    }

    /**
     * Return launcher/aborter tool path
     *
     * @param taskContext
     * @param workingDirectory
     * @param useLauncherTool
     * @return
     * @throws IOException
     */
    public static String getToolPath(final TaskContext taskContext, final File workingDirectory, final Boolean useLauncherTool) throws IOException {
        String toolScriptName = HpToolsLauncher_SCRIPT_NAME;
        if (!useLauncherTool) {
            toolScriptName = HpToolsAborter_SCRIPT_NAME;
        }
        String toolPath = (new File(workingDirectory, toolScriptName)).getAbsolutePath();
        String resourcePath = UFTConstants.TOOLS_PATH.getValue() + "/" + toolScriptName;
        copyResource(taskContext, workingDirectory, resourcePath, toolScriptName);

        return toolPath;
    }

    /**
     * Copy executable file
     *
     * @param workingDirectory
     * @param resourceName
     * @param taskContext
     * @return
     * @throws IOException
     */
    public static TaskResult copyResource(final TaskContext taskContext, final File workingDirectory,
                                          final String resourcePath, final String resourceName) throws IOException {
        TaskResult result = null;
        BuildLogger buildLogger = taskContext.getBuildLogger();
        InputStream resourceStream = FilesHandler.class.getClassLoader().getResourceAsStream(resourcePath);
        String error = FilesHandler.extractBinaryResource(resourceStream, workingDirectory, resourceName);
        if (!error.isEmpty()) {
            buildLogger.addErrorLogEntry(error);
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        }
        return result;
    }

    public static String getOutputFilePath(final TaskContext taskContext) {
        StringBuilder fileName = new StringBuilder(taskContext.getWorkingDirectory().toString());
        String taskName = taskContext.getConfigurationMap().get(TASK_NAME.getValue());
        fileName.append("\\").append(HP_UFT_PREFIX).append(taskContext.getBuildContext().getBuildNumber())
                .append("\\").append(String.format("%03d", taskContext.getId())).append("_")
                .append(taskName).append("\\");

        return fileName.toString();
    }

    public static boolean isMtbxContent(String testContent) {
        return StringUtils.isNotEmpty(testContent) && testContent.toLowerCase().contains("<mtbx>");
    }

    /**
     * Save mtbx content to file in working directory
     *
     * @param taskContext
     * @param mtbxContent
     * @return return name of created file
     */
    public static String saveMtbxContent(TaskContext taskContext, String mtbxContent) {

        String testsFileName = "tests_build_" + taskContext.getBuildContext().getResultKey().getResultNumber() + ".mtbx";

        try {
            File testFile = new File(taskContext.getWorkingDirectory(), testsFileName);
            FileUtils.writeStringToFile(testFile, mtbxContent);
            return testFile.getPath();
        } catch (IOException e) {
            TaskUtils.logErrorMessage("Failed to save mtbx file : " + e.getMessage(), taskContext.getBuildLogger(), taskContext);
            return "";
        }
    }
}
