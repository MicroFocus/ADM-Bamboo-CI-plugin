package com.hpe.utils.loadrunner;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by habash on 27/06/2017.
 */
public class LoadRunnerExecutor {

    private String tests;
    private String timeout;
    private String pollingInterval;
    private String execTimeout;
    private String ignoreErrors;
    private String workingDirectory;

    public LoadRunnerExecutor(String tests, String timeout, String pollingInterval,
                              String execTimeout, String ignoreErrors, String workingDirectory) {
        this.tests = tests;
        this.timeout = timeout;
        this.pollingInterval = pollingInterval;
        this.execTimeout = execTimeout;
        this.ignoreErrors = ignoreErrors;
        this.workingDirectory = workingDirectory;
    }

    public Process run(String launcher, String paramFileName) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(workingDirectory + "\\" + launcher,
                LRConsts.PARAM_FILE_COMMAND_FLAG, workingDirectory + "\\" + paramFileName);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public String createParamFile() throws IOException {
        Properties props = new Properties();
        Date now = new Date();
        Format formatter = new SimpleDateFormat(LRConsts.PARAM_FILE_DATE_FORMAT);
        String time = formatter.format(now);
        //Create a unique filename for the params and results files using date and time
        String paramFileName = String.format(LRConsts.PARAM_FILE_NAME, time);
        String resultsFileName = String.format(LRConsts.PARAM_FILE_RESULT_FILE_NAME, time);
        //Set the params file values
        props.put(LRConsts.PARAM_FILE_RUNTYPE_KEY, LRConsts.PARAM_FILE_RUNTYPE_VALUE);
        String[] testsArr = tests.split(LRConsts.TESTS_DELIMITER);
        int i = 1;
        for(String test: testsArr) {
            test.trim();
            props.put(LRConsts.PARAM_FILE_TEST + i, test);
            ++i;
        }
        props.put(LRConsts.PARAM_FILE_TIMEOUT, timeout);
        props.put(LRConsts.PARAM_FILE_POLLING_INTERVAL, pollingInterval);
        props.put(LRConsts.PARAM_FILE_EXEC_TIMEOUT, execTimeout);
        if(!"".equals(ignoreErrors))
            props.put(LRConsts.PARAM_FILE_IGNORE_ERRORS, ignoreErrors);
        props.put(LRConsts.PARAM_FILE_RESULT_FILE, workingDirectory + "\\" + resultsFileName);

        BufferedWriter buf;
        buf = new BufferedWriter(new FileWriter(new File(workingDirectory + "\\" + paramFileName)));
        props.store(buf, null);
        buf.close();
        return paramFileName;
    }
}
