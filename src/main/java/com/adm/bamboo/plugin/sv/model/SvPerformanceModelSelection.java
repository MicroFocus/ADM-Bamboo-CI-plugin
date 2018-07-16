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

package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConstants;
import org.apache.commons.lang.StringUtils;

public class SvPerformanceModelSelection {
    protected SelectionType selectionType;
    protected String performanceModel;

    public SvPerformanceModelSelection(SelectionType selectionType, String performanceModel) {
        this.selectionType = selectionType;
        this.performanceModel = StringUtils.trim(performanceModel);
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(String selectionType) {
        if(SVConstants.PM_SPECIFIC.equals(selectionType)){
            this.selectionType = SelectionType.BY_NAME;
        } else if(SVConstants.NONE_PERFORMANCE_MODEL.equals(selectionType)){
            this.selectionType = SelectionType.NONE;
        } else if(SVConstants.DEFAULT_PERFORMANCE_MODEL.equals(selectionType)){
            this.selectionType = SelectionType.DEFAULT;
        } else if(SVConstants.OFFLINE.equals(selectionType)){
            this.selectionType = SelectionType.OFFLINE;
        }
    }

    public String getPerformanceModel() {
        return (StringUtils.isNotBlank(performanceModel)) ? performanceModel : null;
    }

    public boolean isSelected(String type) {
        return SelectionType.valueOf(type) == this.selectionType;
    }

    public boolean isNoneSelected() {
        return selectionType == SelectionType.NONE;
    }

    public boolean isDefaultSelected() {
        return selectionType == SelectionType.DEFAULT;
    }

    @Override
    public String toString() {
        switch (selectionType) {
            case BY_NAME:
                return performanceModel;
            case NONE:
                return "<none>";
            case OFFLINE:
                return "<offline>";
            default:
                return "<default>";
        }
    }

    public String getSelectedModelName() {
        switch (selectionType) {
            case BY_NAME:
                return performanceModel;
            case OFFLINE:
                return "Offline";
            default:
                return null;
        }
    }

    public enum SelectionType {
        BY_NAME,
        NONE,
        OFFLINE,
        /**
         * Default means first model in alphabetical order by model name
         */
        DEFAULT,
    }
}
