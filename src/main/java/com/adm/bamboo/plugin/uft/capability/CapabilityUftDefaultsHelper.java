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