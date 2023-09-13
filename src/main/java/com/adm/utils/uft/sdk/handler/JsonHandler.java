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

package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.sdk.Logger;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONArray;

public class JsonHandler {
    private Logger logger;

    public JsonHandler(Logger logger) {
        this.logger = logger;
    }

    public Object load(String path) {

        logger.log(String.format("Loading JSON file from: [%s]", path));
        Object parsedJson;
        try {
            InputStream is = new FileInputStream(path);
            String jsonTxt;
            jsonTxt = IOUtils.toString(is, "UTF-8");
            parsedJson =
                    Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST).jsonProvider().parse(
                            jsonTxt);
        } catch (Throwable e) {
            throw new SSEException(String.format("Failed to load JSON from: [%s]", path), e);
        }

        return parsedJson;
    }

    public String getValueFromJsonAsString(
            Object jsonObject,
            String pathToRead,
            boolean shouldGetSingleValueOnly) {

        String value = "";
        try {
            Object extractedObject = JsonPath.read(jsonObject, pathToRead);
            while (extractedObject instanceof JSONArray && shouldGetSingleValueOnly) {
                extractedObject = ((JSONArray) extractedObject).get(0);
            }
            value = extractedObject.toString();
        } catch (Throwable e) {
            logger.log(String.format(
                    "Failed to get the value of [%s] from the JSON file.\n\tError was: %s",
                    pathToRead,
                    e.getMessage()));
        }
        return value;

    }
}
