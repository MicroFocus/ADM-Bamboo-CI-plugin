/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by OpenText, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 OpenText or one of its affiliates.
 *
 * The only warranties for products and services of OpenText and its affiliates
 * and licensors ("OpenText") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. OpenText shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.model;

import java.util.Arrays;
import java.util.List;

public class SseModel {
    public static final String TEST_SET = "TEST_SET";
    public static final String BVS = "BVS";
    public static final String PC = "PC";

    public static final String COLLATE = "Collate";
    public static final String COLLATE_ANALYZE = "CollateAndAnalyze";
    public static final String DO_NOTHING = "DoNothing";

    private final String _almServerName;
    private String _almServerUrl;
    private final String _almSSO;
    private final String _almClientID;
    private final String _almApiKeySecret;
    private final String _almUserName;
    private final String _almPassword;
    private final String _almDomain;
    private final String _almProject;
    private final String _timeslotDuration;
    private final String _description;
    private final String _runType;
    private final String _almEntityId;
    private final String _postRunAction;
    private final String _environmentConfigurationId;
    private final CdaDetails _cdaDetails;

    private final static EnumDescription _runTypeTestSet =
            new EnumDescription(TEST_SET, "Test Set");
    private final static EnumDescription _runTypeBVS = new EnumDescription(
            BVS,
            "Build Verification Suite");
    private final static List<EnumDescription> _runTypes = Arrays.asList(
            _runTypeTestSet,
            _runTypeBVS);

    private final static EnumDescription _postRunActionCollate = new EnumDescription(
            COLLATE,
            "Collate");
    private final static EnumDescription _postRunActionCollateAnalyze = new EnumDescription(
            COLLATE_ANALYZE,
            "CollateAndAnalyze");
    private final static EnumDescription _postRunActionDoNothing = new EnumDescription(
            DO_NOTHING,
            "DoNothing");
    private final static List<EnumDescription> _postRunActions = Arrays.asList(
            _postRunActionCollate,
            _postRunActionCollateAnalyze,
            _postRunActionDoNothing);

    //@DataBoundConstructor
    public SseModel(
            String almServerName,
            String almSSO,
            String almClientID,
            String almApiKeySecret,
            String almUserName,
            String almPassword,
            String almDomain,
            String almProject,
            String runType,
            String almEntityId,
            String timeslotDuration,
            String description,
            String postRunAction,
            String environmentConfigurationId,
            CdaDetails cdaDetails) {

        _almServerName = almServerName;
        _almSSO = almSSO;
        _almClientID = almClientID;
        _almApiKeySecret = almApiKeySecret;
        _almDomain = almDomain;
        _almProject = almProject;
        _timeslotDuration = timeslotDuration;
        _almEntityId = almEntityId;
        _almUserName = almUserName;
        _almPassword = almPassword;
        _runType = runType;
        _description = description;
        _postRunAction = postRunAction;
        _environmentConfigurationId = environmentConfigurationId;
        _cdaDetails = cdaDetails;

    }
    /*
    protected SecretContainer setPassword(String almPassword) {

        SecretContainer secretContainer = new SecretContainerImpl();
        secretContainer.initialize(almPassword);

        return secretContainer;
    }*/

    public String getAlmServerName() {

        return _almServerName;
    }

    public String getAlmServerUrl() {

        return _almServerUrl;
    }

    public void setAlmServerUrl(String almServerUrl) {

        _almServerUrl = almServerUrl;
    }

    public String getAlmSSO() {
        return _almSSO;
    }

    public String getAlmClientID(){
        return _almClientID;
    }

    public String getAlmApiKeySecret(){
        return _almApiKeySecret;
    }

    public String getAlmUserName() {

        return _almUserName;
    }

    public String getAlmPassword() {

        return _almPassword;
    }

    public String getAlmDomain() {

        return _almDomain;
    }

    public String getAlmProject() {

        return _almProject;
    }

    public String getTimeslotDuration() {

        return _timeslotDuration;
    }

    public String getAlmEntityId() {

        return _almEntityId;
    }

    public String getRunType() {
        return _runType;
    }

    public String getDescription() {

        return _description;
    }

    public String getEnvironmentConfigurationId() {

        return _environmentConfigurationId;
    }

    public static List<EnumDescription> getRunTypes() {

        return _runTypes;
    }

    public static List<EnumDescription> getPostRunActions() {

        return _postRunActions;
    }

    public CdaDetails getCdaDetails() {

        return _cdaDetails;
    }

    public boolean isCdaDetailsChecked() {

        return _cdaDetails != null;
    }

    public String getPostRunAction() {

        return _postRunAction;
    }
}
