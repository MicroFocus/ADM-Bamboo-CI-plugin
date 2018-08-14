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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpRequestDecorator {
    /**
     *
     * @param headers
     *            headrs to decorate with user info depending on the resource access level.
     * @param userName
     * @param resourceAccessLevel
     */
    public static void decorateHeaderWithUserInfo(
            final Map<String, String> headers,
            String userName,
            ResourceAccessLevel resourceAccessLevel) {

        if (headers == null) {
            throw new IllegalArgumentException("header must not be null");
        }
        //attach encrypted user name for protected and public resources
        if (resourceAccessLevel.equals(ResourceAccessLevel.PROTECTED)
                || resourceAccessLevel.equals(ResourceAccessLevel.PRIVATE)) {
            String userHeaderName = resourceAccessLevel.getUserHeaderName();
            String encryptedUserName = getDigestString("MD5", userName);
            if (userHeaderName != null) {
                headers.put(userHeaderName, encryptedUserName);
            }
        }
    }

    private static String getDigestString(String algorithmName, String dataToDigest) {

        try {
            MessageDigest md = MessageDigest.getInstance(algorithmName);
            byte[] digested = md.digest(dataToDigest.getBytes());

            return digestToString(digested);
        } catch (NoSuchAlgorithmException ex) {
            throw new SSEException(ex);
        }
    }

    /**
     * This method convert byte array to string regardless the charset
     *
     * @param b
     *            byte array input
     * @return the corresponding string
     */
    private static String digestToString(byte[] b) {

        StringBuilder result = new StringBuilder(128);
        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }

        return result.toString();
    }

}
