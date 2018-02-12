package com.adm.utils.uft.sdk.request;

import com.adm.utils.uft.Pair;
import com.adm.utils.uft.RestXmlUtils;
import com.adm.utils.uft.rest.RESTConstants;
import com.adm.utils.uft.sdk.Client;
import com.adm.utils.uft.sdk.ResourceAccessLevel;
import com.adm.utils.uft.sdk.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralPostRequest  extends GeneralRequest {

    protected GeneralPostRequest(Client client) {
        super(client);
    }

    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }

    @Override
    public Response perform() {

        return _client.httpPost(
                getUrl(),
                getDataBytes(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }

    private byte[] getDataBytes() {

        StringBuilder builder = new StringBuilder("<Entity><Fields>");
        for (Pair<String, String> currPair : getDataFields()) {
            builder.append(RestXmlUtils.fieldXml(currPair.getFirst(), currPair.getSecond()));
        }

        return builder.append("</Fields></Entity>").toString().getBytes();
    }

    protected List<Pair<String, String>> getDataFields() {

        return new ArrayList<Pair<String, String>>(0);
    }
}

