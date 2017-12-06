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
