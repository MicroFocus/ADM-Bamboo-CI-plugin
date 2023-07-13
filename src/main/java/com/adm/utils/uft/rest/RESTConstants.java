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

package com.adm.utils.uft.rest;

public interface RESTConstants {

    // HttpHeaders
    String PtaL = "PtAL";
    String PvaL = "PvAL";
    String CONTENT_TYPE = "Content-Type";
    String SET_COOKIE = "Set-Cookie";
    String ACCEPT = "Accept";
    String AUTHORIZATION = "Authorization";
    String APP_XML = "application/xml";
    String TEXT_PLAIN = "text/plain";
    String APP_XML_BULK = "application/xml;type=collection";

    String REST_PROTOCOL = "rest";

    String GET = "GET";
    String POST = "POST";
    String PUT = "PUT";
    String COOKIE = "Cookie";
    String XSRF_TOKEN = "XSRF-TOKEN";
    String X_XSRF_TOKEN = "X-XSRF-TOKEN";
}
