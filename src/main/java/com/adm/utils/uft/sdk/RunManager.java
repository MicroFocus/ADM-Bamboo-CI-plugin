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

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.rest.RestClient;
import com.adm.utils.uft.result.PublisherFactory;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.adm.utils.uft.sdk.handler.PollHandler;
import com.adm.utils.uft.sdk.handler.PollHandlerFactory;
import com.adm.utils.uft.sdk.handler.RunHandler;
import com.adm.utils.uft.sdk.handler.RunHandlerFactory;
import com.microfocus.adm.performancecenter.plugins.common.rest.RESTConstants;

import java.util.HashMap;
import java.util.Map;

public class RunManager {
    private RunHandler _runHandler;
    private PollHandler _pollHandler;
    private Logger _logger;
    private boolean _running = false;
    private boolean _polling = false;

    private static final String CLIENT_TYPE = "ALM-CLIENT-TYPE";

    public Testsuites execute(RestClient client, Args args, Logger logger)
            throws InterruptedException {

        Testsuites ret = null;
        _logger = logger;
        _running = true;
        if (login(client, args)) {
            initialize(args, client);
            if (start(args)) {
                _polling = true;
                if (poll()) {
                    ret =
                            new PublisherFactory().create(
                                    client,
                                    args.getRunType(),
                                    args.getEntityId(),
                                    _runHandler.getRunId()).publish(
                                    _runHandler.getNameSuffix(),
                                    args.getUrl(),
                                    args.getDomain(),
                                    args.getProject(),
                                    logger);
                }
                _polling = false;
            }
        }

        return ret;
    }

    private void initialize(Args args, RestClient client) {

        String entityId = args.getEntityId();
        appendQCSessionCookies(client);
        _runHandler = new RunHandlerFactory().create(client, args.getRunType(), entityId);
        _pollHandler = new PollHandlerFactory().create(client, args.getRunType(), entityId);
    }

    private void appendQCSessionCookies(RestClient client) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        headers.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        // issue a post request so that cookies relevant to the QC Session will be added to the RestClient
        Response response =
                client.httpPost(
                        client.build("rest/site-session"),
                        generateClientTypeData(),
                        headers,
                        ResourceAccessLevel.PUBLIC);
        if (!response.isOk()) {
            throw new SSEException("Cannot append QCSession cookies", response.getFailure());
        }
    }

    private byte[] generateClientTypeData() {
        String data = String.format("<session-parameters><client-type>%s</client-type></session-parameters>", CLIENT_TYPE);
        return data.getBytes();
    }

    private boolean poll() throws InterruptedException {

        return _pollHandler.poll(_logger);
    }

    public void stop() {

        _logger.log("Stopping run...");
        if (_runHandler != null) {
            _runHandler.stop();
            _running = false;
        }
        if (_pollHandler != null) {
            _polling = false;
        }
    }

    private boolean login(Client client, Args args) {
        boolean ret = true;
        try {
            if(Boolean.valueOf(args.getAlmSSO()).equals(true)){
                ret =
                        new RestAuthenticator().loginWithApiKey(
                        client,
                        args.getClientId(),
                        args.getApiKeySecret(),
                        CLIENT_TYPE,
                        _logger);
            } else {
                ret =
                        new RestAuthenticator().login(
                                client,
                                args.getUsername(),
                                args.getPassword(),
                                _logger);
            }
        } catch (Throwable cause) {
            ret = false;
            _logger.log(String.format(
                    "Failed login to ALM Server URL: %s. Exception: %s",
                    args.getUrl(),
                    cause.getMessage()));
        }

        return ret;
    }

    private boolean start(Args args) {

        boolean ret = false;
        Response response =
                _runHandler.start(
                        args.getDuration(),
                        args.getPostRunAction(),
                        args.getEnvironmentConfigurationId(),
                        args.getCdaDetails());
        if (isOk(response, args)) {
            RunResponse runResponse = getRunResponse(response);
            setRunId(runResponse);
            if (runResponse.isSucceeded()) {
                ret = true;
            }
        }
        logReportUrl(ret, args);

        return ret;
    }

    private void setRunId(RunResponse runResponse) {

        String runId = runResponse.getRunId();
        if (StringUtils.isNullOrEmpty(runId)) {
            _logger.log("No run ID");
            throw new SSEException("No run ID");
        } else {
            _runHandler.setRunId(runId);
            _pollHandler.setRunId(runId);
        }
    }

    private void logReportUrl(boolean isSucceeded, Args args) {

        if (isSucceeded) {
            _logger.log(String.format(
                    "%s run report for run id %s is at: %s",
                    args.getRunType(),
                    _runHandler.getRunId(),
                    _runHandler.getReportUrl(args)));
        } else {
            _logger.log(String.format(
                    "Failed to start %s ID:%s, run id: %s "
                            + "\nNote: You can run only functional test sets and build verification suites using this plugin. "
                            + "Check to make sure that the configured ID is valid "
                            + "(and that it is not a performance test ID).",
                    args.getRunType(),
                    args.getEntityId(),
                    _runHandler.getRunId()));
        }
    }

    private RunResponse getRunResponse(Response response) {

        return _runHandler.getRunResponse(response);
    }

    private boolean isOk(Response response, Args args) {

        boolean ret = false;
        if (response.isOk()) {
            _logger.log(String.format(
                    "Executing %s ID: %s in %s/%s %sDescription: %s",
                    args.getRunType(),
                    args.getEntityId(),
                    args.getDomain(),
                    args.getProject(),
                    StringUtils.NEW_LINE,
                    args.getDescription()));
            ret = true;
        } else {
            Throwable cause = response.getFailure();
            if (cause != null) {
                _logger.log(String.format(
                        "Failed to start %s ID: %s, ALM Server URL: %s (Exception: %s)",
                        args.getRunType(),
                        args.getEntityId(),
                        args.getUrl(),
                        cause.getMessage()));
            } else {
                _logger.log(String.format(
                        "Failed to execute %s ID: %s, ALM Server URL: %s (Response: %s)",
                        args.getRunType(),
                        args.getEntityId(),
                        args.getUrl(),
                        response.getStatusCode()));
            }
        }

        return ret;
    }

    public boolean getRunning() {

        return _running;
    }

    public boolean getPolling() {

        return _polling;
    }
}
