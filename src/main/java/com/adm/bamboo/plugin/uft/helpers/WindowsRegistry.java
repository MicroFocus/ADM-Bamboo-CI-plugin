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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.uft.helpers;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsRegistry {
    private static final String SOFTWARE_KEY = "SOFTWARE\\";
    private static final String WOW_6432_PREFIX = "SOFTWARE\\WOW6432NODE\\";

    private static final boolean IsX64() {
        String arch = System.getProperty("os.arch");
        if (arch == null) {
            return false;
        }
        return arch.contains("64");
    }

    private static final String GetArchKeyName(final String key) {
        String keyUpper = key.toUpperCase();
        if (IsX64() && !keyUpper.startsWith(WOW_6432_PREFIX) && keyUpper.startsWith(SOFTWARE_KEY)) {
            String newKey = WOW_6432_PREFIX + key.substring(SOFTWARE_KEY.length());
            return newKey;
        } else {
            return key;
        }
    }

    public static final String readHKLMString(final String key, final String value) {
        try {
            String newKey = GetArchKeyName(key);
            String result = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, newKey, value);
            return result;
        } catch (Throwable e) {
            return "";
        }
    }
}
