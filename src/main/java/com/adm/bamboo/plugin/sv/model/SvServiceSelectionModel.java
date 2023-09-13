/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
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
