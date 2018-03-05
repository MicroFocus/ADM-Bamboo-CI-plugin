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

package com.adm.bamboo.plugin.uft.capability;

import com.adm.bamboo.plugin.uft.helpers.WindowsRegistry;
import com.adm.utils.uft.StringUtils;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CapabilityUftDefaultsHelper implements CapabilityDefaultsHelper {

    public static final String CAPABILITY_HP_ROOT = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".HP";
    public static final String CAPABILITY_UFT = CAPABILITY_HP_ROOT + ".HP Unified Functional Testing";
    private static final String UFT_REGISTRY_KEY = "SOFTWARE\\Mercury Interactive\\QuickTest Professional\\CurrentVersion";
    private static final String UFT_REGISTRY_VALUE = "QuickTest Professional";
    private static final String UFT_EXE_NAME = "bin\\UFT.exe";

    @NotNull
    @Override
    public CapabilitySet addDefaultCapabilities(final CapabilitySet capabilitySet) {
        String uftPath = getUftExePath();
        if (!StringUtils.isNullOrEmpty(uftPath)) {
            CapabilityImpl capability = new CapabilityImpl(CAPABILITY_UFT, uftPath);
            capabilitySet.addCapability(capability);
        } else {
            capabilitySet.removeCapability(CAPABILITY_UFT);
        }
        return capabilitySet;
    }

    private static String getUftExePath() {
        String installPath = WindowsRegistry.readHKLMString(UFT_REGISTRY_KEY, UFT_REGISTRY_VALUE);
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
}