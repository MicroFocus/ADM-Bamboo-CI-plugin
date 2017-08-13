/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
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
