package com.adm.tools.common.sdk.request;

import com.adm.tools.common.RestXmlUtils;
import com.adm.tools.common.rest.RESTConstants;
import com.adm.tools.common.sdk.Client;
import com.adm.tools.common.sdk.ResourceAccessLevel;
import com.adm.tools.common.sdk.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneralPutBulkRequest extends GeneralRequest {

    protected GeneralPutBulkRequest(Client client) {
        super(client);
    }

    protected abstract List<Map<String, String>> getFields();

    @Override
    protected Map<String, String> getHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML_BULK);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }

    @Override
    protected Response perform() {
        return _client.httpPut(
                getUrl(),
                getDataBytes(),
                getHeaders(),
                ResourceAccessLevel.PROTECTED);
    }

    private byte[] getDataBytes() {

        StringBuilder builder = new StringBuilder("<Entities>");
        for (Map<String, String> values : getFields()) {
            builder.append("<Entity><Fields>");
            for (String key : values.keySet()) {
                builder.append(RestXmlUtils.fieldXml(key, values.get(key)));
            }
            builder.append("</Fields></Entity>");
        }

        return builder.append("</Entities>").toString().getBytes();

    }
}

