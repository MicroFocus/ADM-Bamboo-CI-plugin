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

package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConstants;
import org.apache.commons.lang.StringUtils;

public class SvDataModelSelection {
    private SelectionType selectionType;
    private String dataModel;

    public SvDataModelSelection(SelectionType selectionType, String dataModel) {
        this.selectionType = selectionType;
        this.dataModel = StringUtils.trim(dataModel);
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(String selectionType) {
        if(SVConstants.DM_SPECIFIC.equals(selectionType)){
            this.selectionType = SelectionType.BY_NAME;
        } else if(SVConstants.NONE_DATA_MODEL.equals(selectionType)){
            this.selectionType = SelectionType.NONE;
        } else if(SVConstants.DM_DEFAULT.equals(selectionType)){
            this.selectionType = SelectionType.DEFAULT;
        }
    }

    public String getDataModel() {
        return (StringUtils.isNotBlank(dataModel)) ? dataModel : null;
    }

    @SuppressWarnings("unused")
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
                return dataModel;
            case NONE:
                return "<none>";
            case DEFAULT:
            default:
                return "<default>";
        }
    }

    public String getSelectedModelName() {
        switch (selectionType) {
            case BY_NAME:
                return dataModel;
            default:
                return null;
        }
    }

    public enum SelectionType {
        BY_NAME,
        NONE,
        /**
         * Default means first model in alphabetical order by model name
         */
        DEFAULT,
    }
}
