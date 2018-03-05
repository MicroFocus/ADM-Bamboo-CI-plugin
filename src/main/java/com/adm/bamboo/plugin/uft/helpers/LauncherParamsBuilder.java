/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents (“Material”) accessible here may contain branding from
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

package com.adm.bamboo.plugin.uft.helpers;

import com.adm.utils.uft.EncryptionUtils;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.enums.AlmRunMode;
import com.adm.utils.uft.enums.RunType;


import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class LauncherParamsBuilder {

    private final List<String> requiredParameters = Arrays.asList("almRunHost");

    private Properties properties;

    public LauncherParamsBuilder() {
        properties = new Properties();
    }

    private void setParamValue(final String paramName, final String paramValue) {
        if (StringUtils.isNullOrEmpty(paramValue)) {
            if (!requiredParameters.contains(paramName)) {
                properties.remove(paramName);
            } else {
                properties.put(paramName, "");
            }
        } else {
            properties.put(paramName, paramValue);
        }
    }

    public void setMobileUseSSL(int type) {
        setParamValue("MobileUseSSL", String.valueOf(type));
    }

    public void setMobileUseProxy(int proxy) {
        setParamValue("MobileUseProxy", String.valueOf(proxy));
    }

    public void setMobileProxyType(int type) {
        setParamValue("MobileProxyType", String.valueOf(type));
    }

    public void setMobileProxySetting_Address(String proxyAddress) {
        setParamValue("MobileProxySetting_Address", proxyAddress);
    }

    public void setMobileProxySetting_Authentication(int authentication) {
        setParamValue("MobileProxySetting_Authentication", String.valueOf(authentication));
    }


    public void setProxyHost(String proxyHost) {
        setParamValue("proxyHost", proxyHost);
    }

    public void setProxyPort(String proxyPort) {
        setParamValue("proxyPort", proxyPort);
    }

    public void setMobileProxySetting_UserName(String proxyUserName) {
        setParamValue("MobileProxySetting_UserName", proxyUserName);
    }

    public void setMobileProxySetting_Password(String proxyPassword) {
        String proxyPass;
        try {
            proxyPass = EncryptionUtils.Encrypt(proxyPassword, EncryptionUtils.getSecretKey());
            properties.put("MobileProxySetting_Password", proxyPass);
        } catch (Exception e) {
        }
    }

    public void setRunType(RunType runType) {
        setParamValue("runType", runType.getValue());
    }

    public void setAlmServerUrl(String almServerUrl) {
        setParamValue("almServerUrl", almServerUrl);
    }

    public void setAlmUserName(String almUserName) {
        setParamValue("almUserName", almUserName);
    }

    public void setAlmPassword(String almPassword) {
        try {
            String encAlmPass = EncryptionUtils.Encrypt(almPassword, EncryptionUtils.getSecretKey());
            properties.put("almPassword", encAlmPass);
        } catch (Exception e) {
        }
    }

    public void setAlmDomain(String almDomain) {
        setParamValue("almDomain", almDomain);
    }

    public void setAlmProject(String almProject) {
        setParamValue("almProject", almProject);
    }

    public void setAlmRunMode(AlmRunMode almRunMode) {
        properties.put("almRunMode", almRunMode != null ? almRunMode.toString() : "");
    }

    public void setAlmTimeout(String almTimeout) {
        setParamValue("almTimeout", almTimeout);
    }

    public void setTestSet(int index, String testSet) {
        setParamValue("TestSet" + index, testSet);
    }

    public void setAlmTestSet(String testSets) {
        setParamValue("almTestSets", testSets);
    }

    public void setAlmRunHost(String host) {
        setParamValue("almRunHost", host);
    }

    public void setTest(int index, String test) {
        setParamValue("Test" + index, test);
    }

    public void setPerScenarioTimeOut(String perScenarioTimeOut) {
        setParamValue("PerScenarioTimeOut", perScenarioTimeOut);
    }

    public void setFileSystemPassword(String oriPass) {
        try {
            String encPass = EncryptionUtils.Encrypt(oriPass, EncryptionUtils.getSecretKey());
            properties.put("MobilePassword", encPass);
        } catch (Exception e) {
        }
    }

    public void setMobileInfo(String mobileInfo) {
        setParamValue("mobileinfo", mobileInfo);
    }

    public void setServerUrl(String serverUrl) {
        setParamValue("MobileHostAddress", serverUrl);
    }

    public void setUserName(String username) {
        setParamValue("MobileUserName", username);
    }

    public Properties getProperties() {
        return properties;
    }

}
