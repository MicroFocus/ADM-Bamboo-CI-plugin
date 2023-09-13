/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.adm.utils.uft.result.model.junit;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "properties", "testcase", "systemOut", "systemErr" })
@XmlRootElement(name = "testsuite")
public class Testsuite {
    protected Properties properties;
    protected List<Testcase> testcase;
    @XmlElement(name = "system-out")
    protected String systemOut;
    @XmlElement(name = "system-err")
    protected String systemErr;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String tests;
    @XmlAttribute
    protected String failures;
    @XmlAttribute
    protected String errors;
    @XmlAttribute
    protected String time;
    @XmlAttribute
    protected String disabled;
    @XmlAttribute
    protected String skipped;
    @XmlAttribute
    protected String timestamp;
    @XmlAttribute
    protected String hostname;
    @XmlAttribute
    protected String id;
    @XmlAttribute(name = "package")
    protected String _package;

    /**
     * Gets the value of the properties property.
     *
     * @return possible object is {@link Properties }
     *
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     *
     * @param value
     *            allowed object is {@link Properties }
     *
     */
    public void setProperties(Properties value) {
        this.properties = value;
    }

    /**
     * Gets the value of the testcase property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the testcase property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getTestcase().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Testcase }
     *
     *
     */
    public List<Testcase> getTestcase() {
        if (testcase == null) {
            testcase = new ArrayList<Testcase>();
        }
        return this.testcase;
    }

    /**
     * Gets the value of the systemOut property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSystemOut() {
        return systemOut;
    }

    /**
     * Sets the value of the systemOut property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSystemOut(String value) {
        this.systemOut = value;
    }

    /**
     * Gets the value of the systemErr property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSystemErr() {
        return systemErr;
    }

    /**
     * Sets the value of the systemErr property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSystemErr(String value) {
        this.systemErr = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the tests property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTests() {
        return tests;
    }

    /**
     * Sets the value of the tests property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setTests(String value) {
        this.tests = value;
    }

    /**
     * Gets the value of the failures property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFailures() {
        return failures;
    }

    /**
     * Sets the value of the failures property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFailures(String value) {
        this.failures = value;
    }

    /**
     * Gets the value of the errors property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Sets the value of the errors property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setErrors(String value) {
        this.errors = value;
    }

    /**
     * Gets the value of the time property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the disabled property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDisabled() {
        return disabled;
    }

    /**
     * Sets the value of the disabled property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setDisabled(String value) {
        this.disabled = value;
    }

    /**
     * Gets the value of the skipped property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSkipped() {
        return skipped;
    }

    /**
     * Sets the value of the skipped property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSkipped(String value) {
        this.skipped = value;
    }

    /**
     * Gets the value of the timestamp property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the hostname property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setHostname(String value) {
        this.hostname = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the package property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setPackage(String value) {
        this._package = value;
    }

}

