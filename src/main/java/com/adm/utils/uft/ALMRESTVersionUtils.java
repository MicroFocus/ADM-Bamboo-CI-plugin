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

package com.adm.utils.uft;

import com.adm.utils.uft.model.ALMVersion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

public class ALMRESTVersionUtils {
    public static ALMVersion toModel(byte[] xml) {

        ALMVersion ret = null;
        try {
            JAXBContext context;
            Thread t = Thread.currentThread();
            ClassLoader orig = t.getContextClassLoader();
            t.setContextClassLoader(ALMRESTVersionUtils.class.getClassLoader());
            try {
                context = JAXBContext.newInstance(ALMVersion.class);
            } finally {
                t.setContextClassLoader(orig);
            }
            Unmarshaller unMarshaller = context.createUnmarshaller();
            ret = (ALMVersion) unMarshaller.unmarshal(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new SSEException("Failed to convert XML to ALMVersion", e);
        }

        return ret;
    }
}
