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
