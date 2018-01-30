package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.sdk.Client;

public class PollTimeslotRequest extends GetRequest {

    private final String _timeslotId;

    public PollTimeslotRequest(Client client, String timeslotId) {

        super(client, timeslotId);
        _timeslotId = timeslotId;
    }

    @Override
    protected String getSuffix() {

        return String.format("reservations/%s", _timeslotId);
    }
}

