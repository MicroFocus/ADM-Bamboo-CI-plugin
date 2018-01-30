package com.adm.utils.uft.sdk;

import java.util.Map;

public interface Client {
    Response httpGet(
            String url,
            String queryString,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    Response httpPost(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    Response httpPut(
            String url,
            byte[] data,
            Map<String, String> headers,
            ResourceAccessLevel resourceAccessLevel);

    String build(String suffix);

    String buildRestRequest(String suffix);

    String buildWebUIRequest(String suffix);

    String getServerUrl();

    String getUsername();
}
