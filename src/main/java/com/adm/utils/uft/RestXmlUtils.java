package com.adm.utils.uft;

import com.adm.utils.uft.rest.RESTConstants;

import java.util.HashMap;
import java.util.Map;

public class RestXmlUtils {
    public static String fieldXml(String field, String value) {

        return String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", field, value);
    }

    public static Map<String, String> getAppXmlHeaders() {

        Map<String, String> ret = new HashMap<String, String>();
        ret.put(RESTConstants.CONTENT_TYPE, RESTConstants.APP_XML);
        ret.put(RESTConstants.ACCEPT, RESTConstants.APP_XML);

        return ret;
    }
}
