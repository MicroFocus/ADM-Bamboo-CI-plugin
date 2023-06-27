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

package com.adm.bamboo.plugin.uft.helpers.locator.windows;

import com.adm.bamboo.plugin.uft.helpers.WindowsRegistry;
import com.adm.bamboo.plugin.uft.helpers.locator.UFTLocatorService;
import com.adm.utils.uft.StringUtils;

import java.io.File;

public class WindowsUFTLocatorService implements UFTLocatorService {

    private static final String UFT_REGISTRY_KEY = "SOFTWARE\\Mercury Interactive\\QuickTest Professional\\CurrentVersion";
    private static final String UFT_REGISTRY_INSTALL_VALUE = "QuickTest Professional";
    private static final String UFT_REGISTRY_VERSION_MAJOR_VALUE = "Major";
    private static final String UFT_EXE_NAME = "bin\\UFT.exe";

    @Override
    public boolean isInstalled() {
        return !StringUtils.isNullOrEmpty(WindowsRegistry.readHKLMString(UFT_REGISTRY_KEY, UFT_REGISTRY_VERSION_MAJOR_VALUE));
    }

    @Override
    public String getPath() {
        final String installPath = WindowsRegistry.readHKLMString(UFT_REGISTRY_KEY, UFT_REGISTRY_INSTALL_VALUE);

        if (StringUtils.isNullOrEmpty(installPath)) {
            return "";
        }

        File f = new File(installPath);
        if (f.exists() && f.isDirectory()) {
            f = new File(f, UFT_EXE_NAME);

            if (f.exists() && f.isFile()) {
                return f.getAbsolutePath();
            }
        }

        return "";
    }

    @Override
    public String getPathFromManualPoint(String startingPathPoint) {
        return startingPathPoint + "\\" + UFT_EXE_NAME;
    }

    @Override
    public boolean validateUFTPath(String path) {
        // the registry entry is always created when the UFT is installed, therefore
        // we can validate the given path by comparing to the registry specified path
        // at this part we will be connected to the remote machine's file system

        final File f = new File(path);

        if (f.exists() && f.isFile()) {
            final File registryPathFile = new File(WindowsRegistry.readHKLMString(UFT_REGISTRY_KEY, UFT_REGISTRY_INSTALL_VALUE), UFT_EXE_NAME);

            // the UFT is not installed, according to the registry
            if (!registryPathFile.exists()) {
                return false;
            }

            return registryPathFile.getAbsolutePath().equals(f.getAbsolutePath());
        }

        return false;
    }

}
