/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
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
