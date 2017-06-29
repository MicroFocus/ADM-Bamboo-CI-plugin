package com.hpe.utils.loadrunner;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public boolean execute() throws IOException {

        createParamFile();
        //runLauncher();
        return true;
    }

    private void createParamFile() throws IOException {
        Properties props = new Properties();
        String[] testsArr = tests.split("\n");
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

        Date now = new Date();
        Format formatter = new SimpleDateFormat(LRConsts.PARAM_FILE_DATE_FORMAT);
        String time = formatter.format(now);

        // get a unique filename for the params file
        String paramFileName = "props" + time + ".txt";
        BufferedWriter buf;
        buf = new BufferedWriter(new FileWriter(new File(workingDirectory + "\\" + paramFileName)));
//
//        BufferedOutputStream buf;
//        buf = new BufferedOutputStream(new FileOutputStream(
//                new File(workingDirectory + "\\" + paramFileName)));
        props.store(buf, null);

    }
}
