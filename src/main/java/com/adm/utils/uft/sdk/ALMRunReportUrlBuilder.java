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

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.ALMRESTVersionUtils;
import com.adm.utils.uft.sdk.request.GetALMVersionRequest;
import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.model.ALMVersion;

public class ALMRunReportUrlBuilder {
    public String build(Client client, String serverUrl, String domain, String project, String runId) {

        String ret = "NA";
        try {
            if (isNewReport(client)) {
                ret = String.format("%sui/?redirected&p=%s/%s&execution-report#/test-set-report/%s",
                        serverUrl,
                        domain,
                        project,
                        runId);
            } else {
                ret = client.buildWebUIRequest(String.format("lab/index.jsp?processRunId=%s", runId));
            }
        } catch (Exception e) {
            // result url will be NA (in case of failure like getting ALM version, convert ALM version to number)
        }

        return ret;
    }

    public boolean isNewReport(Client client) {

        ALMVersion version = getALMVersion(client);

        return toInt(version.getMajorVersion()) >= 12 && toInt(version.getMinorVersion()) >= 2;
    }

    private int toInt(String str) {

        return Integer.parseInt(str);
    }

    private ALMVersion getALMVersion(Client client) {

        ALMVersion ret = null;
        Response response = new GetALMVersionRequest(client).execute();
        if(response.isOk()) {
            ret = ALMRESTVersionUtils.toModel(response.getData());
        } else {
            throw new SSEException(
                    String.format("Failed to get ALM version. HTTP status code: %d", response.getStatusCode()),
                    response.getFailure());
        }

        return ret;
    }
}
