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

package com.adm.bamboo.plugin.loadrunner.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
//import com.hpe.bamboo.plugin.loadrunner.results.SLATestResultsReportCollector;
import com.adm.utils.loadrunner.LRConsts;
import com.adm.utils.loadrunner.LoadRunnerExecutor;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by habash on 15/05/2017.
 */
public class LoadRunnerTestTask implements TaskType {

    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
        //Build and environment related values
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        String workingDirectory = taskContext.getWorkingDirectory().getAbsolutePath();
        int buildNumber = taskContext.getBuildContext().getBuildNumber();
        String jobName = taskContext.getBuildContext().getBuildResultKey();
        String buildDirectoryPath = workingDirectory + "\\" + jobName + "_TR_" + buildNumber;
        File buildDirectory = new File(buildDirectoryPath);
        //Build parameters that were configured in the task
        String tests = taskContext.getConfigurationMap().get(LRConsts.TESTS);
        String timeout = taskContext.getConfigurationMap().get(LRConsts.TIMEOUT);
        String pollingInterval = "".equals(taskContext.getConfigurationMap().get(LRConsts.POLLING_INTERVAL)) ?
                LRConsts.DEFAULT_POLLING_INTERVAL :
                taskContext.getConfigurationMap().get(LRConsts.POLLING_INTERVAL);
        String execTimeout = "".equals(taskContext.getConfigurationMap().get(LRConsts.EXEC_TIMEOUT)) ?
                LRConsts.DEFAULT_EXEC_TIMEOUT :
                taskContext.getConfigurationMap().get(LRConsts.EXEC_TIMEOUT);
        String ignoreErrors = taskContext.getConfigurationMap().get(LRConsts.IGNORE_ERRORS);

        buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
        buildLogger.addBuildLogEntry("**********************  Load Runner Test User Input  *********************" + "\n");
        buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
        buildLogger.addBuildLogEntry(LRConsts.LABEL_TESTS + ": " + tests);
        buildLogger.addBuildLogEntry(LRConsts.LABEL_TIMEOUT + ": " + timeout);
        buildLogger.addBuildLogEntry(LRConsts.LABEL_POLLING_INTERVAL + ": " + pollingInterval);
        buildLogger.addBuildLogEntry(LRConsts.LABEL_EXEC_TIMEOUT + ": " + execTimeout);
        buildLogger.addBuildLogEntry(LRConsts.LABEL_IGNORE_ERRORS + ": " + ignoreErrors);
        buildLogger.addBuildLogEntry("**************************************************************************" + "\n");


        try {
            LoadRunnerExecutor lre;
            String paramFileName;
            int lrTestExitCode;
            buildDirectory.mkdir();

            buildLogger.addBuildLogEntry("************ Copying the Load Runner executables to the agent ************" + "\n");
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
            copyExecutable(buildDirectoryPath, LRConsts.HP_TOOLS_LAUNCHER);
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_EXECUTABLE_COPIED, LRConsts.HP_TOOLS_LAUNCHER));
            copyExecutable(buildDirectoryPath, LRConsts.HP_TOOLS_ABORTER);
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_EXECUTABLE_COPIED, LRConsts.HP_TOOLS_ABORTER));
            copyExecutable(buildDirectoryPath, LRConsts.HP_TOOLS_ANALYSIS);
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_EXECUTABLE_COPIED, LRConsts.HP_TOOLS_ANALYSIS));
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");

            buildLogger.addBuildLogEntry("***************  Creating the param file for the launcher  ***************" + "\n");
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
            lre = new LoadRunnerExecutor(tests, timeout,
                    pollingInterval, execTimeout, ignoreErrors, buildDirectoryPath);
            paramFileName = lre.createParamFile();
            buildLogger.addBuildLogEntry(String.format(LRConsts.LOG_PARAM_FILE_CREATION,
                    buildDirectoryPath));
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");

            buildLogger.addBuildLogEntry("*************************  Running the load test  ************************" + "\n");
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
            lrTestExitCode = runLoadTest(lre, paramFileName, buildLogger, !LRConsts.ABORT_TEST);
            buildLogger.addBuildLogEntry("Load Runner Exit code = " + lrTestExitCode);
            if(lrTestExitCode == LRConsts.TEST_RUN_INTERRUPTED) {
                buildLogger.addBuildLogEntry(LRConsts.LOG_TEST_RUN_ABORTED);
                runLoadTest(lre, paramFileName, buildLogger, LRConsts.ABORT_TEST);
            }
            else if(lrTestExitCode != LRConsts.SUCCESS)
                throw new Exception(LRConsts.ERROR_LOAD_TEST_RUN_FAILED);
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
            buildLogger.addBuildLogEntry("************************* Preparing test results *************************" + "\n");
            zipResultsFolder(buildDirectory);
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
        }
        catch(Exception e) {
            buildLogger.addBuildLogEntry(e.toString());
            return TaskResultBuilder.create(taskContext).failed().build();
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    /**
     * Zips the provided LR results folder. The result of this method will be the destination of the zip file that will be uploaded
     * as an artifact in Bamboo.
     *
     * @param buildDirectory The LR results directory
     * @return The destination of the zip file
     * @throws Exception
     */
    private String zipResultsFolder(File buildDirectory) throws Exception{
        String srcFolder, destZipFile = buildDirectory.getAbsolutePath() + "\\" + LRConsts.RESULTS_ZIP_NAME;
        srcFolder = buildDirectory.getAbsolutePath() + "\\" + buildDirectory.list()[0] + "\\" + LRConsts.RAW_RESULTS_FOLDER;
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
        return destZipFile;
    }

    /**
     * Adds a file to the result zip file.
     *
     * @param path the path inside the zip file
     * @param srcFile the name of the file
     * @param zip zip output stream
     * @throws Exception
     */
    private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
            throws Exception {

        File folder = new File(srcFile);
        if(!folder.exists())
            throw new Exception(LRConsts.ERROR_RESULTS_DONT_EXIST);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    /**
     * Adds a folder to the result zip file.
     *
     * @param path the path inside the zip file
     * @param srcFolder the name of the folder
     * @param zip zip output stream
     * @throws Exception
     */
    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
            throws Exception {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }

    /**
     * Copies a resource file of the plugin to the provided build directory path. A resource file can be one of the following:
     *
     * HpToolsLauncher.exe
     * HpToolsAborter.exe
     * LRAnalysisLauncher.exe
     *
     * @param buildDirectoryPath the build directory
     * @param resourceName name of the resource file
     * @throws Exception
     */
    private void copyExecutable(String buildDirectoryPath, String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String error = "";
        try {
            String resourcePath = LRConsts.TOOLS_PATH + "/" + resourceName;
            stream = this.getClass().getResourceAsStream(resourcePath);

            if(stream == null) {
                throw new Exception(String.format(LRConsts.ERROR_RESOURCE_NOT_FOUND, resourcePath));
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            File resultPath = new File(buildDirectoryPath, resourceName);
            resStreamOut = new FileOutputStream(resultPath);

            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }

        } catch (Exception e) {
            error = e.toString();
        }

        finally {
            if (stream != null) {
                stream.close();
                resStreamOut.close();
            }
            if(!"".equals(error))
                throw new Exception(error);
        }
    }

    /**
     * Starts the Load Runner test, unless the value of <b>abort</b> is true, then it will abort the test in progress.
     *
     * @param lre The LoadRunnerExecutor object
     * @param paramFileName the param file containing the relevant values for the test
     * @param buildLogger buildLogger object for documentation
     * @param abort if true aborts the ongoing test run, otherwise starts a new test run
     * @return the status code of the HpToolsLauncher.exe process (Or the aborter process).
     * @throws IOException
     */
    private int runLoadTest(LoadRunnerExecutor lre, String paramFileName, BuildLogger buildLogger, boolean abort)
            throws IOException{
        InputStream processInputStream;
        BufferedReader buf;
        String lrOutput;
        int result;
        Process lrProcess = lre.run((abort ? LRConsts.HP_TOOLS_ABORTER : LRConsts.HP_TOOLS_LAUNCHER), paramFileName);
        processInputStream = lrProcess.getInputStream();
        buf = new BufferedReader(new InputStreamReader(processInputStream));
        while((lrOutput = buf.readLine()) != null)
            buildLogger.addBuildLogEntry(lrOutput);
        try {
            result = lrProcess.waitFor();
        } catch (InterruptedException e) {
            result = LRConsts.TEST_RUN_INTERRUPTED;
        }
        finally {
            buf.close();
        }
        return result;
    }


}
