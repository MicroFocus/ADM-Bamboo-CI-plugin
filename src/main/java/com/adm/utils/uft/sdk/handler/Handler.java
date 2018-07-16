/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.utils.uft.sdk.handler;

import com.adm.utils.uft.StringUtils;
import com.adm.utils.uft.sdk.Client;

public abstract class Handler {
    protected final Client _client;
    protected final String _entityId;
    protected String _runId = StringUtils.EMPTY_STRING;
    protected String _timeslotId = StringUtils.EMPTY_STRING;

    public Handler(Client client, String entityId) {

        _client = client;
        _entityId = entityId;
    }

    public Handler(Client client, String entityId, String runId) {

        this(client, entityId);
        _runId = runId;
    }

    public String getRunId() {

        return _runId;
    }

    public String getEntityId() {

        return _entityId;
    }

    public void setRunId(String runId) {
        _runId = runId;
    }

    public String getTimeslotId() {

        return _timeslotId;
    }

    public void setTimeslotId(String timeslotId) {

        _timeslotId = timeslotId;
    }
}
