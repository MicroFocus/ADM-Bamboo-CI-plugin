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

package com.adm.bamboo.plugin.srf;

import com.adm.utils.srf.SrfConfigParameter;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskConfigurator extends AbstractTaskConfigurator {

    public static final String SRF_ADDRESS = "Srf address";
    public static final String TENANT_ID = "Tenant id";
    public static final String SRF_CLIENT_ID = "Client id";
    public static final String SRF_CLIENT_SECRET = "Client secret";
    public static final String TEST_ID = "Test Id";
    public static final String PROXY = "Proxy";
    public static final String BUILD = "Test build";
    public static final String RELEASE = "Test release";
    public static final String TAGS = "Tags";
    public static final String SRF_PARAMETERS = "srfParams";
    public static final String SRF_PARAM_NAME = "srfParamName";
    public static final String SRF_PARAM_VALUE = "srfParamValue";
    public static final String TUNNEL = "Tunnel";
    public static final String SHOULD_CLOSE_TUNNEL = "shouldCloseTunnel";

    // Convert the params from the ui into a config map to be stored in the database for being used by the task.
    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(SRF_ADDRESS, "${bamboo.srfAddress}");
        config.put(TENANT_ID, "${bamboo.tenantId}");
        config.put(SRF_CLIENT_ID, "${bamboo.clientId}");
        config.put(SRF_CLIENT_SECRET, "${bamboo.clientSecret}");
        config.put(PROXY, "${bamboo.proxy}");

        config.put(TEST_ID, params.getString(TEST_ID));

        config.put(BUILD, params.getString(BUILD));
        config.put(RELEASE, params.getString(RELEASE));
        config.put(TAGS, params.getString(TAGS));
        config.put(TUNNEL, params.getString(TUNNEL));
        config.put(SHOULD_CLOSE_TUNNEL, params.getString(SHOULD_CLOSE_TUNNEL));

        //parse params
        String[] namesArr = params.getStringArray(SRF_PARAM_NAME);
        String[] valuesArr = params.getStringArray(SRF_PARAM_VALUE);

        if (namesArr != null && valuesArr != null) {
            for (int i = 1; i < namesArr.length; ++i) {
                if (StringUtils.isEmpty(namesArr[i]) || StringUtils.isEmpty(valuesArr[i]))
                    continue;

                String s = namesArr[i] + "&;" + valuesArr[i];
                config.put("srf_param_" + i, s);
            }
        }

        return config;
    }

    // Fill the saved data of the task when opening it after the last save
    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        context.put(TEST_ID, taskDefinition.getConfiguration().get(TEST_ID));
        context.put(BUILD, taskDefinition.getConfiguration().get(BUILD));
        context.put(RELEASE, taskDefinition.getConfiguration().get(RELEASE));
        context.put(TAGS, taskDefinition.getConfiguration().get(TAGS));
        context.put(TUNNEL, taskDefinition.getConfiguration().get(TUNNEL));
        context.put(SHOULD_CLOSE_TUNNEL, taskDefinition.getConfiguration().get(SHOULD_CLOSE_TUNNEL));

        List<SrfConfigParameter> srfParams = fetchSrfParametersFromContext(taskDefinition.getConfiguration());
        context.put(SRF_PARAMETERS, Lists.reverse(srfParams));
    }

    // Fill the data of the task when opening it at the first time
    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
    }

    public static List<SrfConfigParameter> fetchSrfParametersFromContext(@NotNull final Map<String, String> context) {
        List<SrfConfigParameter> srfParams = new ArrayList<>(context.size());

        for (String key : context.keySet()) {
            if (key.startsWith("srf_param_")) {
                String[] arr = context.get(key).split("&;");
                srfParams.add(new SrfConfigParameter(arr[0], arr[1]));
            }
        }

        return srfParams;
    }
}
