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

package com.adm.utils.uft;

import com.adm.bamboo.plugin.uft.results.ResultInfoItem;
import com.adm.utils.uft.sdk.DirectoryZipHelper;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static com.adm.utils.uft.enums.UFTConstants.TASK_NAME;

public final class FilesHandler {
    private static final String HpToolsLauncher_SCRIPT_NAME = "HpToolsLauncher.exe";
    private static final String HpToolsAborter_SCRIPT_NAME = "HpToolsAborter.exe";
    private static final String HP_UFT_PREFIX = "UFT_Build_";
    private static final String FORMATTER_PATTERN = "ddMMyyyyHHmmssSSS";

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
                                           final Properties mergedProperties, final BuildLogger buildLogger) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATTER_PATTERN);
        String paramFileName = "props" + dateTime.format(formatter) + ".txt";
        String resultsFileName = "Results" + dateTime.format(formatter) + ".xml";
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
}
