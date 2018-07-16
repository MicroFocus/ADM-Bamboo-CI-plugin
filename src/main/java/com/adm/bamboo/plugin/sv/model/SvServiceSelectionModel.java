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
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConstants;
import org.apache.commons.lang.StringUtils;

public class SvServiceSelectionModel {
    private SelectionType selectionType;
    private String service;
    private String projectPath;
    private String projectPassword;

    public SvServiceSelectionModel(String service, String projectPath, String projectPassword) {
        this.service = StringUtils.trim(service);
        this.projectPath = StringUtils.trim(projectPath);
        this.projectPassword = projectPassword;
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(String serviceSelection) {
        if(SVConstants.ALL_SERVICES_FROM_PROJECT.equals(serviceSelection)){
            this.selectionType = SvServiceSelectionModel.SelectionType.PROJECT;
        } else if(SVConstants.ALL_SERVICES_DEPLOYED_ON_SERVER.equals(serviceSelection)){
            this.selectionType = SvServiceSelectionModel.SelectionType.ALL_DEPLOYED;
        } else if(SVConstants.SELECTED_SERVICE_ONLY.equals(serviceSelection)){
            this.selectionType = SvServiceSelectionModel.SelectionType.SERVICE;
        }
    }

    public String getService() {
        return (StringUtils.isNotBlank(service)) ? service : null;
    }

    public String getProjectPath() {
        return (StringUtils.isNotBlank(projectPath)) ? projectPath : null;
    }

    public String getProjectPassword() {
        return (projectPassword != null) ? projectPassword : null;
    }

    public enum SelectionType {
        /**
         * Select service by name or id
         */
        SERVICE,
        /**
         * Select all services from project
         */
        PROJECT,
        /**
         * Select all deployed services
         */
        ALL_DEPLOYED,
        /**
         * Specific case for deployment. Uses project & optionally service names.
         */
        DEPLOY
    }
}
