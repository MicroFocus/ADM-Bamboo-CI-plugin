/*
 *
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

package com.adm.bamboo.plugin.performancecenter;

import com.microfocus.adm.performancecenter.plugins.common.pcEntities.*;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bemh on 7/23/2017.
 */
public class TaskConfigurator extends AbstractTaskConfigurator {

    public static final String PC_SERVER ="PC Server";
    public static final String USER ="User name";
    public static final String HTTPS ="https";
    public static final String PASSWORD ="Password";
    public static final String DOMAIN ="Domain";
    public static final String PROJECT ="PC Project";
    public static final String TEST_ID ="Test ID";
    public static final String TEST_INSTANCE_ID ="Test Instance ID";
    public static final String TEST_INSTANCE_ID_RADIO ="TestInstanceIDRadio";
    public static final String LOCAL_PROXY ="Local Proxy";
    public static final String PROXY_USER ="ProxyUser";
    public static final String PROXY_PASSWORD ="ProxyPassword";
    public static final String POST_RUN_ACTION = "postRunAction";
    public static final String TRENDING_RADIO ="trendingRadio";
    public static final String TREND_REPORT_ID ="Trend Report ID";
    public static final String TIMESLOT_HOURS ="Hours";
    public static final String TIMESLOT_MINUTES ="Minutes";
    public static final String VUDS ="vuds";
    public static final String SLA ="sla";


    public Map<String,String> postRunActionMap = new LinkedHashMap<String, String>();
    public Map<String,String> testInstanceMap = new LinkedHashMap<String, String>();
    public Map<String,String> trendReportMap = new LinkedHashMap<String, String>();


    public static final String    COLLATE         = "Collate Results";
    public static final String    COLLATE_ANALYZE = "Collate and Analyze";
    public static final String    DO_NOTHING      = "Do Not Collate";
    private static final String REGEX =  "^\\$\\{.*\\}$|^[0-9]*$"; // regex for parameter or numeric

    ArrayList<String> localDAtaArray = new ArrayList<String>();




    // Convert the params from the ui into a config map to be stored in the database for being used by the task.
    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(PC_SERVER, params.getString(PC_SERVER));
        config.put(USER, params.getString(USER));
        config.put(HTTPS, params.getString(HTTPS));
        config.put(PASSWORD, params.getString(PASSWORD));
        config.put(DOMAIN, params.getString(DOMAIN));
        config.put(PROJECT, params.getString(PROJECT));
        config.put(TEST_ID, params.getString(TEST_ID));
        config.put(TEST_INSTANCE_ID, params.getString(TEST_INSTANCE_ID));
        config.put(TEST_INSTANCE_ID_RADIO, params.getString(TEST_INSTANCE_ID_RADIO));
        config.put(LOCAL_PROXY, params.getString(LOCAL_PROXY));
        config.put(PROXY_USER, params.getString(PROXY_USER));
        config.put(PROXY_PASSWORD, params.getString(PROXY_PASSWORD));
        config.put(POST_RUN_ACTION, params.getString(POST_RUN_ACTION));
        config.put(TREND_REPORT_ID, params.getString(TREND_REPORT_ID));
        config.put(TRENDING_RADIO, params.getString(TRENDING_RADIO));
        config.put(TIMESLOT_HOURS, params.getString(TIMESLOT_HOURS));
        config.put(TIMESLOT_MINUTES, params.getString(TIMESLOT_MINUTES));
        config.put(VUDS, params.getString(VUDS));
        config.put(SLA, params.getString(SLA));


        return config;
    }

    // Validate the params submitted from the UI for this task definition
    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
        // array with parameters we want to validate
        updateLocalArray();

        super.validate(params, errorCollection);



        //final String PCServerValue = params.getString(PC_SERVER);
        for (String p : localDAtaArray) {
            String val = params.getString(p);
            if ((StringUtils.equals(p, TEST_INSTANCE_ID) && !StringUtils.equals(params.getString("TestInstanceIDRadio"), "AUTO"))
                    || StringUtils.equals(p, TEST_ID)
                    || (StringUtils.equals(p, TREND_REPORT_ID) && StringUtils.equals(params.getString("trendingRadio"), "USE_ID"))){
                if (StringUtils.isEmpty(val)) {
                    errorCollection.addError(p, "Required!");
                }else if(!val.matches(REGEX)){
                    errorCollection.addError(p, "Must be numeric or a variable (e.g. ${value}).");
                }
            }else {
                if (StringUtils.isEmpty(val) && !StringUtils.equals(p, PASSWORD) && !StringUtils.equals(p, TEST_INSTANCE_ID) && !StringUtils.equals(p, TREND_REPORT_ID)) {
                    errorCollection.addError(p, "Required!");
                }
            }
        }

    }



    // array with parameters we want to validate
    private void updateLocalArray(){
        localDAtaArray.clear();

        localDAtaArray.add(PC_SERVER);
        localDAtaArray.add(USER);
        localDAtaArray.add(PASSWORD);
        localDAtaArray.add(DOMAIN);
        localDAtaArray.add(PROJECT);
        localDAtaArray.add(TEST_ID);
        localDAtaArray.add(TEST_INSTANCE_ID_RADIO);
        localDAtaArray.add(TEST_INSTANCE_ID);
        localDAtaArray.add(TIMESLOT_HOURS);
        localDAtaArray.add(TIMESLOT_MINUTES);
        localDAtaArray.add(TREND_REPORT_ID);
//        localDAtaArray.add(POST_RUN_ACTION);


        postRunActionMap.put(PostRunAction.DO_NOTHING.getValue().replaceAll(" ","_"), PostRunAction.DO_NOTHING.getValue()); //"Do Not Collate"
        postRunActionMap.put(PostRunAction.COLLATE.getValue().replaceAll(" ","_"), PostRunAction.COLLATE.getValue()); // "Collate Results"
        postRunActionMap.put(PostRunAction.COLLATE_AND_ANALYZE.getValue().replaceAll(" ","_"), PostRunAction.COLLATE_AND_ANALYZE.getValue()); //"Collate and Analyze")

        testInstanceMap.put("AUTO","Automatically select existing or create new if none exists (Performance Center 12.55 or later)");
        testInstanceMap.put("MANUAL","Manual selection");

        trendReportMap.put("NO_TREND","Do Not Trend");
        trendReportMap.put("ASSOCIATED","Use trend report associated with the test - Performance Center 12.55 or later");
        trendReportMap.put("USE_ID","Add run to trend report with ID");


    }


    // Fill the saved data of the task when opening it after the last save
    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition)
    {
        updateLocalArray();



        context.put("postRunActionList", postRunActionMap);
        context.put("testInstanceList",testInstanceMap);
        context.put("trendReporList",trendReportMap);

  //      context.put("selectedPostRunAction",taskDefinition.getConfiguration().get(POST_RUN_ACTION));


        context.put(PC_SERVER, taskDefinition.getConfiguration().get(PC_SERVER));
        context.put(USER, taskDefinition.getConfiguration().get(USER));
        context.put(HTTPS, taskDefinition.getConfiguration().get(HTTPS));
        context.put(PASSWORD, taskDefinition.getConfiguration().get(PASSWORD));
        context.put(DOMAIN, taskDefinition.getConfiguration().get(DOMAIN));
        context.put(PROJECT, taskDefinition.getConfiguration().get(PROJECT));
        context.put(TEST_ID, taskDefinition.getConfiguration().get(TEST_ID));
        context.put(TEST_INSTANCE_ID, taskDefinition.getConfiguration().get(TEST_INSTANCE_ID));
        context.put(TEST_INSTANCE_ID_RADIO, taskDefinition.getConfiguration().get(TEST_INSTANCE_ID_RADIO));
        context.put(LOCAL_PROXY, taskDefinition.getConfiguration().get(LOCAL_PROXY));
        context.put(PROXY_USER, taskDefinition.getConfiguration().get(PROXY_USER));
        context.put(PROXY_PASSWORD, taskDefinition.getConfiguration().get(PROXY_PASSWORD));
        context.put(POST_RUN_ACTION, taskDefinition.getConfiguration().get(POST_RUN_ACTION));
        context.put(TRENDING_RADIO, taskDefinition.getConfiguration().get(TRENDING_RADIO));
        context.put(TREND_REPORT_ID, taskDefinition.getConfiguration().get(TREND_REPORT_ID));
        context.put(TIMESLOT_HOURS, taskDefinition.getConfiguration().get(TIMESLOT_HOURS));
        context.put(TIMESLOT_MINUTES, taskDefinition.getConfiguration().get(TIMESLOT_MINUTES));
        context.put(VUDS, taskDefinition.getConfiguration().get(VUDS));
        context.put(SLA, taskDefinition.getConfiguration().get(SLA));



        //config.put(POST_RUN_ACTION, params.getString(POST_RUN_ACTION));

    }

    // Fill the data of the task when opening it at the first time
    @Override
    public void populateContextForCreate(final Map<String, Object> context)
    {
        updateLocalArray();
        super.populateContextForCreate(context);
        context.put("postRunActionList", postRunActionMap);
        context.put("testInstanceList",testInstanceMap);
        context.put("trendReporList",trendReportMap);

        context.put(TIMESLOT_HOURS,"0");
        context.put(TIMESLOT_MINUTES,"30");
    }

}
