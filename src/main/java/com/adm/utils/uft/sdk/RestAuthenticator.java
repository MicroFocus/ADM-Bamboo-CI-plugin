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
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.adm.utils.uft.sdk;

import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.rest.HttpHeaders;
import com.adm.utils.uft.rest.RestClient;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class RestAuthenticator {
    public static final String IS_AUTHENTICATED = "rest/is-authenticated";
    public static String AUTHENTICATE_HEADER = "WWW-Authenticate";

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
