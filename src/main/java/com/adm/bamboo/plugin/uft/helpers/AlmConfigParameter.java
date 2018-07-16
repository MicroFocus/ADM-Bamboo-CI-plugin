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

package com.adm.bamboo.plugin.uft.helpers;

import java.io.Serializable;

public class AlmConfigParameter implements Serializable {
    private String almParamSourceType;
    private String almParamName;
    private String almParamValue;
    private Boolean almParamOnlyFirst;

    public AlmConfigParameter(String almParamSourceType, String almParamName, String almParamValue, String almParamOnlyFirst) {
        this.almParamSourceType = almParamSourceType;
        this.almParamName = almParamName;
        this.almParamValue = almParamValue;
        this.almParamOnlyFirst = Boolean.parseBoolean(almParamOnlyFirst);
    }

    public AlmConfigParameter(AlmConfigParameter configParams) {
        this.almParamSourceType = configParams.getAlmParamSourceType();
        this.almParamName = configParams.getAlmParamName();
        this.almParamValue = configParams.getAlmParamValue();
    }

    public AlmConfigParameter() {
    }

    public String getAlmParamSourceType() {
        return almParamSourceType;
    }

    public void setAlmParamSourceType(String almParamSourceType) {
        this.almParamSourceType = almParamSourceType;
    }

    public String getAlmParamName() {
        return almParamName;
    }

    public void setAlmParamName(String almParamName) {
        this.almParamName = almParamName;
    }

    public String getAlmParamValue() {
        return almParamValue;
    }

    public void setAlmParamValue(String almParamValue) {
        this.almParamValue = almParamValue;
    }

    public Boolean getAlmParamOnlyFirst() {
        return almParamOnlyFirst;
    }

    public void setAlmParamOnlyFirst(Boolean almParamOnlyFirst) {
        this.almParamOnlyFirst = almParamOnlyFirst;
    }
}
