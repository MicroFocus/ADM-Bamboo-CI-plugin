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

package com.adm.bamboo.plugin.uft.helpers;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsRegistry {
    private static final String SOFTWARE_KEY = "SOFTWARE\\";
    private static final String WOW_6432_PREFIX = "SOFTWARE\\WOW6432NODE\\";

    private static boolean IsX64() {
        String arch = System.getProperty("os.arch");
        if (arch == null) {
            return false;
        }
        return arch.contains("64");
    }

    private static String GetArchKeyName(final String key) {
        String keyUpper = key.toUpperCase();
        if (IsX64() && !keyUpper.startsWith(WOW_6432_PREFIX) && keyUpper.startsWith(SOFTWARE_KEY)) {
            return WOW_6432_PREFIX + key.substring(SOFTWARE_KEY.length());
        } else {
            return key;
        }
    }

    public static String readHKLMString(final String key, final String value) {
        try {
            String newKey = GetArchKeyName(key);
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, newKey, value);
        } catch (Throwable e) {
            return "";
        }
    }
}
