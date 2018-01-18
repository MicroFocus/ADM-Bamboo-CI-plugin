package com.adm.bamboo.plugin.sv.model;

import com.adm.utils.sv.SVConsts;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
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
        if(SVConsts.ALL_SERVICES_FROM_PROJECT.equals(serviceSelection)){
            this.selectionType = SvServiceSelectionModel.SelectionType.PROJECT;
        } else if(SVConsts.ALL_SERVICES_DEPLOYED_ON_SERVER.equals(serviceSelection)){
            this.selectionType = SvServiceSelectionModel.SelectionType.ALL_DEPLOYED;
        } else if(SVConsts.SELECTED_SERVICE_ONLY.equals(serviceSelection)){
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
