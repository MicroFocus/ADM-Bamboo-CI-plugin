package com.hpe.bamboo.plugin.loadrunner.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.*;
import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.task.*;
import com.hpe.utils.loadrunner.LRConsts;
import com.hpe.utils.loadrunner.LoadRunnerExecutor;

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
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        String workingDirectory = taskContext.getWorkingDirectory().getAbsolutePath();
        int buildNumber = taskContext.getBuildContext().getBuildNumber();
        String buildDirectoryPath = workingDirectory + "\\" + buildNumber;
        File buildDirectory = new File(buildDirectoryPath);

        String tests = taskContext.getConfigurationMap().get(LRConsts.TESTS);
        String timeout = "".equals(taskContext.getConfigurationMap().get(LRConsts.TIMEOUT)) ?
                LRConsts.DEFAULT_TIMEOUT :
                taskContext.getConfigurationMap().get(LRConsts.TIMEOUT);
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
            String paramFileName, resultsZip;
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
            buildLogger.addBuildLogEntry("************************* Collating test results *************************" + "\n");
            resultsZip = zipResultsFolder(buildDirectory);
            //createArtifact(resultsZip, buildLogger, taskContext.getBuildContext().getPlanResultKey(), 1);
            buildLogger.addBuildLogEntry("**************************************************************************" + "\n");
        }
        catch(Exception e) {
            buildLogger.addBuildLogEntry(e.toString());
            return TaskResultBuilder.create(taskContext).failed().build();
        }
        return TaskResultBuilder.create(taskContext).success().build();
    }

    private void createArtifact() throws Exception {
        ArtifactDefinition artifactDef = new ArtifactDefinitionImpl(LRConsts.LR_ARTIFACT_DEFINITION_NAME,
                LRConsts.LR_ARTIFACT_DEFINITION_LOCATION, LRConsts.LR_ARTIFACT_DEFINITION_COPY_PATTERN);
        artifactDef.setSharedArtifact(LRConsts.LR_ARTIFACT_DEFINITION_IS_SHARED);

    }

    private String zipResultsFolder(File buildDirectory) throws Exception{
        String srcFolder, destZipFile = buildDirectory.getAbsolutePath() + "\\" + LRConsts.RESULTS_ZIP_NAME;
        srcFolder = buildDirectory.getAbsolutePath() + "\\" + buildDirectory.list()[0] + "\\LRR";
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
        return destZipFile;
    }

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
