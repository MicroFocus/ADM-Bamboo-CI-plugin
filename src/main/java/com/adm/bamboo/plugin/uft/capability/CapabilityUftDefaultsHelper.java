/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
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

    public static final String CAPABILITY_HP_ROOT = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".Micro Focus";
    public static final String CAPABILITY_UFT = CAPABILITY_HP_ROOT + ".Unified Functional Testing";
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