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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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
