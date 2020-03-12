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

package com.adm.utils.uft.result;

import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.result.model.junit.Testsuites;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultSerializer {
    private ResultSerializer(){}

    public static String saveResults(Testsuites testsuites, String workingDirectory, Logger logger)
            throws SSEException
    {
        String filePath = getFullFilePath(workingDirectory, getFileName());

        try
        {
            if (testsuites != null)
            {
                StringWriter writer = new StringWriter();
                JAXBContext context = JAXBContext.newInstance(Testsuites.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(testsuites, writer);

                PrintWriter resultWriter = new PrintWriter(filePath);
                resultWriter.print(writer.toString());
                resultWriter.close();

                return filePath;
            }
            else
            {
                String message ="Empty Results";
                logger.log(message);
                throw new SSEException(message);
            }
        }
        catch (Throwable cause)
        {
            String message=String.format(
                    "Failed to create run results, Exception: %s",
                    cause.getMessage());
            logger.log(message);

            throw new SSEException(message);
        }
    }

    public static Testsuites Deserialize(File file) throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(Testsuites.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Testsuites testsuites = (Testsuites)jaxbUnmarshaller.unmarshal(file);

        return testsuites;
    }

    private static String getFullFilePath(String workingDirectoryPath, String fileName)
    {
        return Paths.get(workingDirectoryPath, fileName).toString();
    }

    private static String getFileName() {

        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String time = formatter.format(new Date());
        return String.format("Results%s.xml", time);
    }
}
