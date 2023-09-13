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

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Xml.Linq;
using System.Xml.Schema;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class MtbxManager
    {
        //the xml format of an mtbx file below:
        /*
         <Mtbx>
            <Test Name="test1" path="${workspace}\test1">
                <Parameter Name="mee" Value="12" Type="Integer"/>
                <Parameter Name="mee1" Value="12.0" Type="Double"/>
                <Parameter Name="mee2" Value="abc" Type="String"/>
                <DataTable path="c:\tables\my_data_table.xls"/>
            </Test>
            <Test Name="test2" path="${workspace}\test2">
                <Parameter Name="mee" Value="12" Type="Integer"/>
                <Parameter Name="mee1" Value="12.0" Type="Double"/>
                <Parameter Name="mee2" Value="abc" Type="String"/>
            </Test>
         </Mtbx>
        */
        public static List<TestInfo> LoadMtbx(string mtbxContent, string testGroup)
        {
            return LoadMtbx(mtbxContent, null, testGroup);
        }

        public static List<TestInfo> Parse(string mtbxFileName)
        {
            string xmlContent = File.ReadAllText(mtbxFileName);
            return Parse(xmlContent, null, mtbxFileName);
        }

        private static XAttribute GetAttribute(XElement x, XName attributeName)
        {
            return x.Attributes().FirstOrDefault(a => a.Name.Namespace == attributeName.Namespace && a.Name.LocalName.EqualsIgnoreCase(attributeName.LocalName));
        }

        private static XElement GetElement(XElement x, XName eName)
        {
            return x.Elements().FirstOrDefault(a => a.Name.Namespace == eName.Namespace && a.Name.LocalName.EqualsIgnoreCase(eName.LocalName));
        }

        private static IEnumerable<XElement> GetElements(XElement x, XName eName)
        {
            return x.Elements().Where(a => a.Name.Namespace == eName.Namespace && a.Name.LocalName.EqualsIgnoreCase(eName.LocalName));
        }

        public static List<TestInfo> Parse(string mtbxFileName, Dictionary<string, string> jankinsEnvironmentVars, string testGroupName)
        {
            return LoadMtbx(File.ReadAllText(mtbxFileName), jankinsEnvironmentVars, testGroupName);
        }
        private static string ReplaceString(string str, string oldValue, string newValue, StringComparison comparison)
        {
            StringBuilder sb = new();

            int previousIndex = 0;
            int index = str.IndexOf(oldValue, comparison);
            while (index != -1)
            {
                sb.Append(str.Substring(previousIndex, index - previousIndex));
                sb.Append(newValue);
                index += oldValue.Length;

                previousIndex = index;
                index = str.IndexOf(oldValue, index, comparison);
            }
            sb.Append(str.Substring(previousIndex));

            return sb.ToString();
        }

        public static List<TestInfo> LoadMtbx(string xmlContent, Dictionary<string, string> jankinsEnvironmentVars, string testGroupName)
        {
            var localEnv = Environment.GetEnvironmentVariables();

            foreach (string varName in localEnv.Keys)
            {
                string value = (string)localEnv[varName];
                xmlContent = ReplaceString(xmlContent, "%" + varName + "%", value, StringComparison.OrdinalIgnoreCase);
                xmlContent = ReplaceString(xmlContent, "${" + varName + "}", value, StringComparison.OrdinalIgnoreCase);
            }

            if (jankinsEnvironmentVars != null)
            {
                foreach (string varName in jankinsEnvironmentVars.Keys)
                {
                    string value = jankinsEnvironmentVars[varName];
                    xmlContent = ReplaceString(xmlContent, "%" + varName + "%", value, StringComparison.OrdinalIgnoreCase);
                    xmlContent = ReplaceString(xmlContent, "${" + varName + "}", value, StringComparison.OrdinalIgnoreCase);
                }
            }

            List<TestInfo> retval = new();
            XDocument doc = XDocument.Parse(xmlContent);

            XmlSchemaSet schemas = new();

            var assembly = Assembly.GetExecutingAssembly();

            var schemaStream = assembly.GetManifestResourceStream("HpToolsLauncher.MtbxSchema.xsd");

            XmlSchema schema = XmlSchema.Read(schemaStream, null);

            schemas.Add(schema);

            string validationMessages = string.Empty;
            doc.Validate(schemas, (o, e) =>
            {
                validationMessages += e.Message + Environment.NewLine;
            });

            if (!validationMessages.IsNullOrWhiteSpace())
                ConsoleWriter.WriteLine("mtbx schema validation errors: " + validationMessages);
            try
            {
                var root = doc.Root;
                foreach (var test in GetElements(root, "Test"))
                {
                    string path = GetAttribute(test, "path").Value;

                    if (!Directory.Exists(path))
                    {
                        string line = string.Format(Resources.GeneralFileNotFound, path);
                        ConsoleWriter.WriteLine(line);
                        ConsoleWriter.ErrorSummaryLines.Add(line);
                        Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                        continue;
                    }

                    XAttribute xname = GetAttribute(test, "name");

                    string name = "<None>";
                    if (xname != null)
                        name = xname.Value;
                    var testName = path.Substring(path.LastIndexOf("\\") + 1);

                    string currentFolder = AppDomain.CurrentDomain.BaseDirectory.ToLower();
                    if (path.ToLower().Contains(currentFolder))
                    {
                        testName = path.Substring(currentFolder.Length);
                    }

                    TestInfo col = new(path, testName, testGroupName);
                    HashSet<string> paramNames = new();

                    foreach (var param in GetElements(test, "Parameter"))
                    {
                        string pname = GetAttribute(param, "name").Value;
                        string pval = GetAttribute(param, "value").Value;
                        XAttribute xptype = GetAttribute(param, "type");
                        string ptype = "string";

                        if (xptype != null)
                            ptype = xptype.Value;

                        var testParam = new TestParameterInfo() { Name = pname, Type = ptype, Value = pval };
                        if (!paramNames.Contains(testParam.Name))
                        {
                            paramNames.Add(testParam.Name);
                            col.ParameterList.Add(testParam);
                        }
                        else
                        {
                            string line = string.Format(Resources.GeneralDuplicateParameterWarning, pname, path);
                            ConsoleWriter.WriteLine(line);
                        }
                    }

                    XElement dataTable = GetElement(test, "DataTable");
                    if (dataTable != null)
                    {
                        col.DataTablePath = GetAttribute(dataTable, "path").Value;
                    }

                    retval.Add(col);
                }
            }
            catch (Exception ex)
            {
                ConsoleWriter.WriteException("Problem while parsing Mtbx file", ex);
            }
            return retval;
        }
    }
}
