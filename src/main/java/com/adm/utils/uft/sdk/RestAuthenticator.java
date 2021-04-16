/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.rest.HttpHeaders;
import com.adm.utils.uft.rest.RestClient;
import com.microfocus.adm.performancecenter.plugins.common.rest.RESTConstants;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class RestAuthenticator {
    public static final String IS_AUTHENTICATED = "rest/is-authenticated";
    public static String AUTHENTICATE_HEADER = "WWW-Authenticate";

    private static final String APIKEY_LOGIN_API = "rest/oauth2/login";
    private static final String CLIENT_TYPE = "ALM-CLIENT-TYPE";

    /**
     * Logins to ALM server with SSO configured
     * @param client
     * @param clientId
     * @param secret
     * @param clientType
     * @param logger
     * @return
     */
    public boolean loginWithApiKey(Client client, String clientId, String secret, String clientType, Logger logger) {
        logger.log("Start login to ALM server with APIkey...");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CLIENT_TYPE, clientType);
        headers.put(RESTConstants.ACCEPT, "application/json");
        headers.put(RESTConstants.CONTENT_TYPE, "application/json");

        Response response =
                client.httpPost(
                        client.build(APIKEY_LOGIN_API),
                        String.format("{clientId:%s, secret:%s}", clientId, secret).getBytes(),
                        headers,
                        ResourceAccessLevel.PUBLIC);
        boolean result = response.isOk();
        logger.log(
                result ? String.format(
                        "Logged in successfully to ALM Server %s using %s",
                        client.getServerUrl(),
                        clientId)
                        : String.format(
                        "Login to ALM Server at %s failed. Status Code: %s",
                        client.getServerUrl(),
                        response.getStatusCode()));
        return result;
    }

    public boolean login(Client client, String username, String password, Logger logger) {
        boolean ret = true;
        String authenticationPoint = isAuthenticated(client, logger);
        if (authenticationPoint != null) {
            Response response = login(client, authenticationPoint, username, password);
            if (response.isOk()) {
                logLoggedInSuccessfully(username, client.getServerUrl(), logger);
            } else {
                logger.log(String.format(
                        "Login to ALM Server at %s failed. Status Code: %s",
                        client.getServerUrl(),
                        response.getStatusCode()));
                ret = false;
            }
        } else{
            logger.log("not authenticated");
        }

        return ret;
    }

    /**
     * @param loginUrl
     *            to authenticate at
     * @return true on operation success, false otherwise Basic authentication (must store returned
     *         cookies for further use)
     */
    private Response login(Client client, String loginUrl, String username, String password) {

        // create a string that looks like:
        // "Basic ((username:password)<as bytes>)<64encoded>"
        byte[] credBytes = (username + ":" + password).getBytes();
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.AUTHORIZATION, credEncodedString);

        return client.httpGet(loginUrl, null, headers, ResourceAccessLevel.PUBLIC);
    }

    /**
     * @return true if logout successful
     * @throws Exception
     *             close session on server and clean session cookies on client
     */
    public boolean logout(RestClient client, String username) {

        // note the get operation logs us out by setting authentication cookies to:
        // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        Response response =
                client.httpGet(
                        client.build("authentication-point/logout"),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);

        return response.isOk();

    }

    /**
     * @return null if authenticated.<br>
     *         a URL to authenticate against if not authenticated.
     * @throws Exception
     *             if error such as 404, or 500
     */
    public String isAuthenticated(Client client, Logger logger) {
        logger.log("isAuthenticated method");
        String ret;
        Response response =
                client.httpGet(
                        client.build(IS_AUTHENTICATED),
                        null,
                        null,
                        ResourceAccessLevel.PUBLIC);
        int responseCode = response.getStatusCode();

        // already authenticated
        if (responseCode == HttpURLConnection.HTTP_OK) {
            ret = null;
            logLoggedInSuccessfully(client.getUsername(), client.getServerUrl(), logger);
        }
        // if not authenticated - get the address where to authenticate via WWW-Authenticate
        else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String newUrl = response.getHeaders().get(AUTHENTICATE_HEADER).get(0).split("=")[1];
            newUrl = newUrl.replace("\"", "");
            newUrl += "/authenticate";
            ret = newUrl;
        }
        // error such as 404, or 500
        else {
            try {
                throw response.getFailure();
            } catch (Throwable cause) {
                throw new SSEException(cause);
            }
        }

        return ret;
    }

    private void logLoggedInSuccessfully(String username, String loginServerUrl, Logger logger) {
        logger.log(String.format(
                "Logged in successfully to ALM Server %s using %s",
                loginServerUrl,
                username));
    }
}
