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
