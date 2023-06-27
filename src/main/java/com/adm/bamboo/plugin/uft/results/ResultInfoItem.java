/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.uft.results;

import java.io.File;

public class ResultInfoItem {
    private String testName;
    private String resultName;
    private File sourceDir;
    private File zipFile;

    public ResultInfoItem(final String testName, final File sourceDir, final File zipFile, final String resultName) {
        this.testName = testName;
        this.resultName = resultName;
        this.sourceDir = sourceDir;
        this.zipFile = zipFile;
    }

    public String getTestName()
    {
        return testName;
    }
    public String getResultName()
    {
        return resultName;
    }
    public File getSourceDir()
    {
        return sourceDir;
    }
    public File getZipFile()
    {
        return zipFile;
    }
}
