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

import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.XPathUtils;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.Response;
import com.adm.utils.uft.sdk.request.GetLabRunEntityDataRequest;
import com.adm.utils.uft.sdk.request.PollTimeslotRequest;

import java.util.Arrays;
import java.util.List;

public class LabPollHandler extends PollHandler {

    private final static List<String> FINAL_STATES =
            Arrays.asList("Finished", "Aborted", "Stopped");
    private EventLogHandler _eventLogHandler;

    public LabPollHandler(Client client, String entityId) {

        super(client, entityId);
    }

    public LabPollHandler(Client client, String entityId, int interval) {

        super(client, entityId, interval);
    }

    @Override
    protected boolean doPoll(Logger logger) throws InterruptedException {

        boolean ret = false;

        Response runEntityResponse = getRunEntityData();
        if (isOk(runEntityResponse, logger)) {
            setTimeslotId(runEntityResponse, logger);
            _eventLogHandler = new EventLogHandler(_client, _timeslotId);
            if (!StringUtils.isNullOrEmpty(_timeslotId)) {
                ret = super.doPoll(logger);
            }
        }
        return ret;

    }

    @Override
    protected Response getResponse() {

        return new PollTimeslotRequest(_client, _timeslotId).execute();
    }

    @Override
    protected void log(Logger logger) {

        _eventLogHandler.log(logger);
    }

    @Override
    protected boolean isFinished(Response response, Logger logger) {

        boolean ret = false;
        try {
            String xml = response.toString();
            String currentRunState = XPathUtils.getAttributeValue(xml, "current-run-state");
            if (FINAL_STATES.contains(currentRunState)) {
                String startTime = XPathUtils.getAttributeValue(xml, "start-time");
                String endTime = XPathUtils.getAttributeValue(xml, "end-time");
                logger.log(String.format(
                        "Timeslot %s is %s.\nTimeslot start time: %s, Timeslot end time: %s",
                        _timeslotId,
                        currentRunState,
                        startTime,
                        endTime));
                ret = true;
            }
        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
            ret = true;
        }

        return ret;
    }

    @Override
    protected boolean logRunEntityResults(Response response, Logger logger) {

        boolean ret = false;
        try {
            String xml = response.toString();
            String state = XPathUtils.getAttributeValue(xml, "state");
            String completedSuccessfully =
                    XPathUtils.getAttributeValue(xml, "completed-successfully");
            logger.log(String.format(
                    "Run state of %s: %s, Completed successfully: %s",
                    _runId,
                    state,
                    completedSuccessfully));
            ret = true;

        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response: %s", response));
        }

        return ret;
    }

    private void setTimeslotId(Response runEntityResponse, Logger logger) {

        _timeslotId = getTimeslotId(runEntityResponse, logger);
        if (!StringUtils.isNullOrEmpty(_timeslotId)) {
            logger.log(String.format("Timeslot id: %s", _timeslotId));
        }
    }

    private Response getRunEntityData() {

        return new GetLabRunEntityDataRequest(_client, _runId).execute();
    }

    private String getTimeslotId(Response response, Logger logger) {

        String ret = StringUtils.EMPTY_STRING;
        try {
            String xml = response.toString();
            ret = XPathUtils.getAttributeValue(xml, "reservation-id");
        } catch (Throwable cause) {
            logger.log(String.format("Failed to parse response for timeslot ID: %s", response));
        }

        return ret;
    }

    @Override
    protected Response getRunEntityResultsResponse() {
        return new GetLabRunEntityDataRequest(_client, _runId).execute();
    }
}

