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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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

package com.adm.bamboo.plugin.uft.helpers;

import com.adm.bamboo.plugin.uft.results.TestResultHelper;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.util.RequestCacheThreadLocal;
import com.atlassian.bamboo.utils.XsrfUtils;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.jetbrains.annotations.NotNull;

public class HpTasksArtifactRegistrator {
    private static final String HP_UFT_PREFIX = "UFT_Build_";

    public void registerCommonArtifact(@NotNull Job job, @NotNull I18nBean i18nBean, @NotNull ArtifactDefinitionManager artifactDefinitionManager) {
        if (job == null || i18nBean == null || artifactDefinitionManager == null) {
            return;
        }

        String name = i18nBean.getText("OpenText Tasks Artifact Definition");
        String ARTIFACT_COPY_PATTERN = HP_UFT_PREFIX + "${bamboo.buildNumber}/**";
        if (artifactDefinitionManager.findArtifactDefinition(job, name) == null) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(job);

            //workaround, if request is not mutative - saveArtifactDefinition will fail on XSRF exception
            boolean isMutativeKeyWasChanged = false;
            String HTTP_REQUEST_IS_MUTATIVE_KEY = "bamboo.http.request.isMutative";
            if (XsrfUtils.areMutativeGetsForbiddenByConfig() && !XsrfUtils.noRequestOrRequestCanMutateState() && !RequestCacheThreadLocal.canRequestMutateState()) {
                isMutativeKeyWasChanged = true;
                RequestCacheThreadLocal.getRequestCache().put(HTTP_REQUEST_IS_MUTATIVE_KEY, true);
            }
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);

            //revert workaround
            if (isMutativeKeyWasChanged) {
                RequestCacheThreadLocal.getRequestCache().put(HTTP_REQUEST_IS_MUTATIVE_KEY, false);
            }
        }
    }
}
