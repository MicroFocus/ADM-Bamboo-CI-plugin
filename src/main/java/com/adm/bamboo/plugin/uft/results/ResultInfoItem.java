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
