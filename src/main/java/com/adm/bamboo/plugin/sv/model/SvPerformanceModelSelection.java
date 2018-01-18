package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConsts;
import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
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
        if(SVConsts.PM_SPECIFIC.equals(selectionType)){
            this.selectionType = SelectionType.BY_NAME;
        } else if(SVConsts.NONE_PERFORMANCE_MODEL.equals(selectionType)){
            this.selectionType = SelectionType.NONE;
        } else if(SVConsts.NONE_PERFORMANCE_MODEL.equals(selectionType)){
            this.selectionType = SelectionType.DEFAULT;
        } else if(SVConsts.OFFLINE.equals(selectionType)){
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
