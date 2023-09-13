/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.uft.helpers;

import com.adm.utils.uft.Aes256Encryptor;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.enums.AlmRunMode;
import com.adm.utils.uft.enums.RunType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class LauncherParamsBuilder {

    private final List<String> requiredParameters = Arrays.asList("almRunHost",
                                                                    "almServerUrl",
                                                                    "almUserName",
                                                                    "almPassword",
                                                                    "almDomain",
                                                                    "almProject",
                                                                    "almRunMode",
                                                                    "almTimeout",
                                                                    "almRunHost");

    private Properties properties;
    private final Aes256Encryptor aes256Encryptor;

    public LauncherParamsBuilder(@NotNull final Aes256Encryptor aes256Encryptor) {
        properties = new Properties();
        this.aes256Encryptor = aes256Encryptor;
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
            proxyPass = aes256Encryptor.Encrypt(proxyPassword);
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

    public void setAlmSSO(String almSSO) { setParamValue("almSSO", almSSO); }

    public void setAlmClientID(String almClientID){ setParamValue("clientID", almClientID);}

    public void setAlmApiKeySecret(String almApiKeySecret){
        try {
            setParamValue("apiKeySecret", aes256Encryptor.Encrypt(almApiKeySecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAlmUserName(String almUserName) {
        setParamValue("almUserName", almUserName);
    }

    public void setAlmPassword(String almPassword) {
        try {
            String encAlmPass = aes256Encryptor.Encrypt(almPassword);
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

    public void setFsTimeout(String fsTimeout) {
        setParamValue("fsTimeout", fsTimeout);
    }
    public void setFileSystemPassword(String oriPass) {
        try {
            String encPass = aes256Encryptor.Encrypt(oriPass);
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
