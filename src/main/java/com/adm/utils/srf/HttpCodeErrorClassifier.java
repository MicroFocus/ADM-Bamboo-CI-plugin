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

package com.adm.utils.srf;

import com.atlassian.bamboo.build.logger.BuildLogger;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import java.io.IOException;

public class HttpCodeErrorClassifier {
    public static void throwError(BuildLogger buildLogger, int statusCode, String responseMessage) throws IOException, AuthorizationException, SrfException {
        buildLogger.addBuildLogEntry(String.format("Received http status code %d with response message %s", statusCode, responseMessage));

        switch (statusCode) {
            case 401: throw new AuthenticationException("Login failed, possibly wrong credentials supplied");
            case 403: throw new AuthorizationException("Operation is forbidden");
            default:
                JSONObject srfError = JSONObject.fromObject(responseMessage);
                if (SrfException.isSrfException(srfError))
                    throw new SrfException(srfError.getString("message"), srfError.getString("code"));

                throw new SrfException(String.format("Request failed with http code %d and http response %s", statusCode, responseMessage));
        }

    }
}
