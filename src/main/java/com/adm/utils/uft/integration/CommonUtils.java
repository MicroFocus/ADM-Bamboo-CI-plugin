package com.adm.utils.uft.integration;

public class CommonUtils {
    private CommonUtils(){

    }

    public static boolean doCheck(String... args) {

        for (String arg : args) {
            if (arg == null || arg == "" || arg.length() == 0) {
                return false;
            }
        }
        return true;
    }
}
