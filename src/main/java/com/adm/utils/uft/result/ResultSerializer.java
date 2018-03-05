/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents (“Material”) accessible here may contain branding from
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
