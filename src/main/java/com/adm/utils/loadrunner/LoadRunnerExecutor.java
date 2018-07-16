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
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.utils.loadrunner;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * This class is responsible for handling pure Load Runner related actions.
 *
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

    /**
     * Creates and returns a Process object that either runs the load Runner test according to the specified paramFile or aborts the ongoing
     * run
     *
     * @param launcher the Launcher to be run
     * @param paramFileName the name of the paramFile
     * @return A Process object that runs the LR task
     * @throws IOException
     */
    public Process run(String launcher, String paramFileName) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(workingDirectory + "\\" + launcher,
                LRConsts.PARAM_FILE_COMMAND_FLAG, workingDirectory + "\\" + paramFileName);
        processBuilder.directory(new File(workingDirectory));
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    /**
     * Creates a paramFile with the values needed for a LR test run
     *
     * @return name of the paramFile
     * @throws IOException
     */
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
        if(timeout != null && !"".equals(timeout))
            props.put(LRConsts.PARAM_FILE_TIMEOUT, timeout);
        if(pollingInterval != null && !"".equals(pollingInterval))
            props.put(LRConsts.PARAM_FILE_POLLING_INTERVAL, pollingInterval);
        else props.put(LRConsts.PARAM_FILE_POLLING_INTERVAL, LRConsts.DEFAULT_POLLING_INTERVAL);
        if(execTimeout != null && !"".equals(execTimeout))
            props.put(LRConsts.PARAM_FILE_EXEC_TIMEOUT, execTimeout);
        else props.put(LRConsts.PARAM_FILE_EXEC_TIMEOUT, LRConsts.DEFAULT_EXEC_TIMEOUT);
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
