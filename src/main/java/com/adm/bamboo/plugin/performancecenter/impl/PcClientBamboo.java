package com.adm.bamboo.plugin.performancecenter.impl;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.atlassian.bamboo.task.TaskContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;

import java.beans.IntrospectionException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//import org.hibernate.cfg.beanvalidation.IntegrationException;


/**
 * Created by bemh on 8/6/2017.
 */
public class PcClientBamboo {


    private static final String artifactsDirectoryName = "archive";
    public static final String artifactsResourceName = "artifact";
    public static final String runReportStructure = "%s/%s/performanceTestsReports/pcRun";
    public static final String trendReportStructure = "%s/%s/performanceTestsReports/TrendReports";
    public static final String pcReportArchiveName = "Reports.zip";
    public static final String pcReportFileName = "Report.html";
    private static final String RUNID_BUILD_VARIABLE = "HP_RUN_ID";

    public static final String    TRENDED         = "Trended";
    public static final String    PENDING         = "Pending";
    public static final String    PUBLISHING      = "Publishing";
    public static final String    ERROR           = "Error";

    private PcModelBamboo model;
    private TaskContext taskContext;
    private PcRestProxy restProxy;
    private boolean loggedIn;
    private BuildLogger buildLogger;

    private PrintStream ps;

    public static final String    COLLATE         = "COLLATE";
    public static final String    COLLATE_ANALYZE = "COLLATE_AND_ANALYZE";
    public static final String    DO_NOTHING      = "DO_NOTHING";

    public PcClientBamboo(PcModelBamboo pcModel, TaskContext taskContext, BuildLogger buildLogger) {
        try {
            model = pcModel;
            this.taskContext = taskContext;

            if(model.getProxyOutURL() != null && !model.getProxyOutURL().isEmpty()){
                buildLogger.addBuildLogEntry("Using proxy: " + model.getProxyOutURL());
            }
            restProxy = new PcRestProxy(model.isHTTPSProtocol(),model.getPcServerName(), model.getAlmDomain(), model.getAlmProject(), model.getProxyOutURL(),model.getProxyOutUser(),model.getProxyOutPassword());
            this.buildLogger = buildLogger;
        }catch (PcException e){
            buildLogger.addBuildLogEntry(e.getMessage());
        }

    }

    public boolean login() {
        try {
            String user = model.getAlmUserName();
            buildLogger.addBuildLogEntry(String.format("Trying to login\n[PCServer='%s://%s', User='%s']",model.isHTTPSProtocol(), model.getPcServerName(), user));
            loggedIn = restProxy.authenticate(user, model.getAlmPassword().toString());
        } catch (PcException e) {
            buildLogger.addBuildLogEntry(e.getMessage());
        } catch (Exception e) {
            buildLogger.addBuildLogEntry(String.valueOf(e));
        }
        buildLogger.addBuildLogEntry(String.format("Login %s", loggedIn ? "succeeded" : "failed"));
        return loggedIn;
    }


    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int startRun() throws NumberFormatException, ClientProtocolException, PcException, IOException {
        int testID = Integer.parseInt(model.getTestId());
        int testInstance = getCorrectTestInstanceID(testID);
        setCorrectTrendReportID();

        PcRunResponse response = restProxy.startRun(testID,
                testInstance,
                model.getTimeslotDuration(),
                model.getPostRunAction().getValue(),
                model.isVudsMode());
       buildLogger.addBuildLogEntry(String.format("\nRun started (TestID: %s, RunID: %s, TimeslotID: %s)\n",
                response.getTestID(), response.getID(), response.getTimeslotID()));
        return response.getID();
    }

    private int getCorrectTestInstanceID(int testID) throws IOException, PcException {
        if("AUTO".equals(model.getAutoTestInstanceID())){
            try {


               buildLogger.addBuildLogEntry("Searching for available Test Instance");
                PcTestInstances pcTestInstances = restProxy.getTestInstancesByTestId(testID);
                int testInstanceID = 0;
                if (pcTestInstances != null && pcTestInstances.getTestInstancesList() != null){
                    PcTestInstance pcTestInstance = pcTestInstances.getTestInstancesList().get(pcTestInstances.getTestInstancesList().size()-1);
                    testInstanceID = pcTestInstance.getInstanceId();
                   buildLogger.addBuildLogEntry("Found testInstanceId: " + testInstanceID);
                }else{
                   buildLogger.addBuildLogEntry("Could not find available TestInstanceID, Creating Test Instance.");
                   buildLogger.addBuildLogEntry("Searching for available TestSet");
                    // Get a random TestSet
                    PcTestSets pcTestSets = restProxy.GetAllTestSets();
                    if (pcTestSets !=null && pcTestSets.getPcTestSetsList() !=null){
                        PcTestSet pcTestSet = pcTestSets.getPcTestSetsList().get(pcTestSets.getPcTestSetsList().size()-1);
                        int testSetID = pcTestSet.getTestSetID();
                       buildLogger.addBuildLogEntry(String.format("Creating Test Instance with testID: %s and TestSetID: %s", testID,testSetID));
                        testInstanceID = restProxy.createTestInstance(testID,testSetID);
                       buildLogger.addBuildLogEntry(String.format("Test Instance with ID : %s has been created successfully.", testInstanceID));
                    }else{
                        String msg = "No TestSetID available in project, please create a testset from Performance Center UI";
                       buildLogger.addBuildLogEntry(msg);
                        throw new PcException(msg);
                    }
                }
                return testInstanceID;
            } catch (Exception e){
               buildLogger.addBuildLogEntry(String.format("getCorrectTestInstanceID failed, reason: %s",e));
                return Integer.parseInt(null);
            }
        }
        return Integer.parseInt(model.getTestInstanceId());
    }

    private void setCorrectTrendReportID() throws IOException, PcException {
        // If the user selected "Use trend report associated with the test" we want the report ID to be the one from the test
        if (("ASSOCIATED").equals(model.getAddRunToTrendReport())){
            PcTest pcTest = restProxy.getTestData(Integer.parseInt(model.getTestId()));
            if (pcTest.getTrendReportId() > -1)
                model.setTrendReportId(String.valueOf(pcTest.getTrendReportId()));
            else{
                String msg = "No trend report ID is associated with the test.\n" +
                        "Please turn Automatic Trending on for the test through Performance Center UI.\n" +
                        "Alternatively you can check 'Add run to trend report with ID' on configuration dialog.";
                throw new PcException(msg);
            }
        }

    }

    public String getTestName()  throws IOException, PcException{
        PcTest pcTest = restProxy.getTestData(Integer.parseInt(model.getTestId()));
        return pcTest.getTestName();
    }

    public PcRunResponse waitForRunCompletion(int runId) throws InterruptedException, ClientProtocolException, PcException, IOException {

        return waitForRunCompletion(runId, 5000);
    }

    public PcRunResponse waitForRunCompletion(int runId, int interval) throws InterruptedException, ClientProtocolException, PcException, IOException {
        RunState state = RunState.UNDEFINED;
        if (model.getPostRunAction().toString().equals(DO_NOTHING)) {
            state = RunState.BEFORE_COLLATING_RESULTS;

        } else if (model.getPostRunAction().toString().equals(COLLATE)) {
            state = RunState.BEFORE_CREATING_ANALYSIS_DATA;

        } else if (model.getPostRunAction().toString().equals(COLLATE_ANALYZE)) {
            state = RunState.FINISHED;

        }
        return waitForRunState(runId, state, interval);
    }


    private PcRunResponse waitForRunState(int runId, RunState completionState, int interval) throws InterruptedException,
            ClientProtocolException, PcException, IOException {

        int counter = 0;
        RunState[] states = {RunState.BEFORE_COLLATING_RESULTS,RunState.BEFORE_CREATING_ANALYSIS_DATA};
        PcRunResponse response = null;
        RunState lastState = RunState.UNDEFINED;
        do {
            response = restProxy.getRunData(runId);
            RunState currentState = RunState.get(response.getRunState());
            if (lastState.ordinal() < currentState.ordinal()) {
                lastState = currentState;
               buildLogger.addBuildLogEntry(String.format("RunID: %s - State = %s", runId, currentState.value()));
            }

            // In case we are in state before collate or before analyze, we will wait 1 minute for the state to change otherwise we exit
            // because the user probably stopped the run from PC or timeslot has reached the end.
            if (Arrays.asList(states).contains(currentState)){
                counter++;
                Thread.sleep(1000);
                if(counter > 60 ){
                   buildLogger.addBuildLogEntry(String.format("RunID: %s  - Stopped from Performance Center side with state = %s", runId, currentState.value()));
                    break;
                }
            }else{
                counter = 0;
                Thread.sleep(interval);
            }
        } while (lastState.ordinal() < completionState.ordinal());
        return response;
    }

    public String publishRunReport(int runId, String reportDirectory) throws IOException, PcException, InterruptedException {


        PcRunResults runResultsList = restProxy.getRunResults(runId);
        if (runResultsList.getResultsList() != null){
            for (PcRunResult result : runResultsList.getResultsList()) {
                if (result.getName().equals(pcReportArchiveName)) {
                    File dir = new File(reportDirectory  + File.separator + "Reports");// taskContext.getBuildContext().getBuildNumber());
                    dir.mkdirs();
                    String reportArchiveFullPath = dir.getCanonicalPath() + IOUtils.DIR_SEPARATOR + pcReportArchiveName;
                    buildLogger.addBuildLogEntry("Publishing analysis report");
                    restProxy.GetRunResultData(runId, result.getID(), reportArchiveFullPath);
                    File fp = new File(reportArchiveFullPath);
                    unzip(reportArchiveFullPath,fp.getParent().toString());
                    String reportFile = dir.getPath() + File.separator + pcReportFileName;
                    publishHTMLReportToArtifact();
                    //Deleting the unziped report file
                  //  FileUtils.deleteDirectory(dir);
                    return reportFile;
                }
            }
        }
       buildLogger.addBuildLogEntry("Failed to get run report");
        return null;
    }

    public void publishHTMLReportToArtifact(){
        Map<String, String> config = new HashMap<>();

        ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl("Build_" + String.valueOf(taskContext.getBuildContext().getBuildNumber()) + "_reports",false,null);
        artifact.setCopyPattern("**/*");
        taskContext.getBuildContext().getArtifactContext().getDefinitionContexts().add(artifact);
    }


    public boolean logout() {
        if (!loggedIn)
            return true;

        boolean logoutSucceeded = false;
        try {
            logoutSucceeded = restProxy.logout();
            loggedIn = !logoutSucceeded;
        } catch (PcException e) {
           buildLogger.addBuildLogEntry(e.getMessage());
        } catch (Exception e) {
           buildLogger.addBuildLogEntry(String.valueOf(e));
        }
       buildLogger.addBuildLogEntry(String.format("Logout %s", logoutSucceeded ? "succeeded" : "failed"));
        return logoutSucceeded;
    }

    public boolean stopRun(int runId) {
        boolean stopRunSucceeded = false;
        try {
           buildLogger.addBuildLogEntry("Stopping run");
            stopRunSucceeded = restProxy.stopRun(runId, "stop");
        } catch (PcException e) {
           buildLogger.addBuildLogEntry(e.getMessage());
        } catch (Exception e) {
           buildLogger.addBuildLogEntry(String.valueOf(e));
        }
       buildLogger.addBuildLogEntry(String.format("Stop run %s", stopRunSucceeded ? "succeeded" : "failed"));
        return stopRunSucceeded;
    }

    public PcRunEventLog getRunEventLog(int runId){
        try {
            return restProxy.getRunEventLog(runId);
        } catch (PcException e) {
           buildLogger.addBuildLogEntry(e.getMessage());
        } catch (Exception e) {
           buildLogger.addBuildLogEntry(String.valueOf(e));
        }
        return null;
    }

    public void addRunToTrendReport(int runId, String trendReportId)
    {

        TrendReportRequest trRequest = new TrendReportRequest(model.getAlmProject(), runId, null);
       buildLogger.addBuildLogEntry("Adding run: " + runId + " to trend report: " + trendReportId);
        try {
            restProxy.updateTrendReport(trendReportId, trRequest);
           buildLogger.addBuildLogEntry("Publishing run: " + runId + " on trend report: " + trendReportId);
        }
        catch (PcException e) {
           buildLogger.addBuildLogEntry("Failed to add run to trend report: " + e.getMessage());
        }
        catch (IOException e) {
           buildLogger.addBuildLogEntry("Failed to add run to trend report: Problem connecting to PC Server");
        }
    }

    public void waitForRunToPublishOnTrendReport(int runId, String trendReportId) throws PcException,IOException,InterruptedException{

        ArrayList<PcTrendedRun> trendReportMetaDataResultsList;
        boolean publishEnded = false;
        int counter = 0;

        do {
            trendReportMetaDataResultsList = restProxy.getTrendReportMetaData(trendReportId);

            if (trendReportMetaDataResultsList.isEmpty())  break;

            for (PcTrendedRun result : trendReportMetaDataResultsList) {

                if (result.getRunID() != runId) continue;

                if (result.getState().equals(TRENDED) || result.getState().equals(ERROR)){
                    publishEnded = true;
                   buildLogger.addBuildLogEntry("Run: " + runId + " publishing status: "+ result.getState());
                    break;
                }else{
                    Thread.sleep(5000);
                    counter++;
                    if(counter >= 120){
                        String msg = "Error: Publishing didn't ended after 10 minutes, aborting...";
                        throw new PcException(msg);
                    }
                }
            }

        }while (!publishEnded && counter < 120);
    }



    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }



    public boolean downloadTrendReportAsPdf(String trendReportId, String directory) throws PcException {


        try {
           buildLogger.addBuildLogEntry("Downloading trend report: " + trendReportId + " in PDF format");
            InputStream in = restProxy.getTrendingPDF(trendReportId);
            File dir = new File(directory);
            if(!dir.exists()){
                dir.mkdirs();
            }
            String filePath = directory + IOUtils.DIR_SEPARATOR + "trendReport" + trendReportId + ".pdf";
            Path destination = Paths.get(filePath);
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
           buildLogger.addBuildLogEntry("Trend report: " + trendReportId + " was successfully downloaded");
           buildLogger.addBuildLogEntry("View trend report in the Artifacts Tab.");

        }
        catch (Exception e) {

           buildLogger.addBuildLogEntry("Failed to download trend report: " + e.getMessage());
            throw new PcException(e.getMessage());
        }

        return true;

    }




    // This method will return a map with the following structure: <transaction_name:selected_measurement_value>
    // for example:
    // <Action_Transaction:0.001>
    // <Virtual transaction 2:0.51>
    // This function uses reflection since we know only at runtime which transactions data will be reposed from the rest request.
    public Map<String, String> getTrendReportByXML(String trendReportId, int runId, TrendReportTypes.DataType dataType, TrendReportTypes.PctType pctType, TrendReportTypes.Measurement measurement) throws IOException, PcException, IntrospectionException, NoSuchMethodException {

        Map<String, String> measurmentsMap = new LinkedHashMap<String, String>();
        measurmentsMap.put("RunId","_" + runId + "_");
        measurmentsMap.put("Trend Measurement Type",measurement.toString() + "_" + pctType.toString());



        TrendReportTransactionDataRoot res = restProxy.getTrendReportByXML(trendReportId, runId);

        List<Object> RowsListObj = res.getTrendReportRoot();

        for (int i=0; i< RowsListObj.size();i++){
            try {

                java.lang.reflect.Method rowListMethod = RowsListObj.get(i).getClass().getMethod("getTrendReport" + dataType.toString() + "DataRowList");

                for ( Object DataRowObj : (ArrayList<Object>)rowListMethod.invoke(RowsListObj.get(i)))
                {
                    if (DataRowObj.getClass().getMethod("getPCT_TYPE").invoke(DataRowObj).equals(pctType.toString()))
                    {
                        java.lang.reflect.Method method;
                        method = DataRowObj.getClass().getMethod("get" + measurement.toString());
                        measurmentsMap.put(DataRowObj.getClass().getMethod("getPCT_NAME").invoke(DataRowObj).toString(),method.invoke(DataRowObj)==null?"":method.invoke(DataRowObj).toString());
                    }
                }
            }catch (NoSuchMethodException e){
                // buildLogger.addBuildLogEntry("No such method exception: " + e);
            }
            catch (Exception e){
               buildLogger.addBuildLogEntry("Error on getTrendReportByXML: " + e);
            }
        }

        return measurmentsMap;


    }
}
