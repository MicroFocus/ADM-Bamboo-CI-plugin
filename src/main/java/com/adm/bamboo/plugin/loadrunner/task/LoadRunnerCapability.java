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
 * (c) Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */
package com.adm.bamboo.plugin.loadrunner.task;

import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityImpl;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import com.adm.utils.loadrunner.LRConsts;
import java.io.File;

/**
 * This class is used by Bamboo to make sure that the agent is capable of running task of type LoadRunner Test
 *
 * Created by habash on 15/7/2017.
 */
public class LoadRunnerCapability implements CapabilityDefaultsHelper {


    @Override
    public CapabilitySet addDefaultCapabilities(CapabilitySet capabilitySet) {
        File wlrun = new File(LRConsts.WLRUN_EXE_ABSOLUTE_NAME);
        if(wlrun.exists()) {
            CapabilityImpl capability = new CapabilityImpl(LRConsts.CAPABILITY_KEY, LRConsts.WLRUN_EXE_ABSOLUTE_NAME);
            capabilitySet.addCapability(capability);
        }
        else
        {
            capabilitySet.removeCapability(LRConsts.CAPABILITY_KEY);
        }
        return capabilitySet;
    }

}
