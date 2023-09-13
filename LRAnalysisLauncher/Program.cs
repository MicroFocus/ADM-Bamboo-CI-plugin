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
using System.IO;
using System.Xml;
using Analysis.Api;
//using Analysis.ApiLib;
using Analysis.ApiLib.Sla;
using LRAnalysisLauncher.Properties;
using HpToolsLauncher;
using Analysis.ApiSL;
using Analysis.Api.Dictionaries;
using System.Diagnostics;

namespace LRAnalysisLauncher
{
    class Program
    {

        static Program()
        {
            AppDomain.CurrentDomain.AssemblyResolve += new ResolveEventHandler(CurrentDomain_AssemblyResolve);
        }
        //args: lrr location, lra location, html report location
        static int Main(string[] args)
        {
            Console.WriteLine("starting analysis launcher");
            int iPassed = (int)Launcher.ExitCodeEnum.Passed;//variable to keep track of whether all of the SLAs passed
            try
            {
                if (args.Length != 3)
                {
                    ShowHelp();
                    return (int)Launcher.ExitCodeEnum.Aborted;
                }

                string lrrlocation = args[0];
                string lralocation = args[1];
                string htmlLocation = args[2];

                LrAnalysis analysis = new LrAnalysis();

                Session session = analysis.Session;
                if (session.Create(lralocation, lrrlocation))
                {
                    HtmlReportMaker reportMaker = session.CreateHtmlReportMaker();
                    reportMaker.CreateDefaultHtmlReport(Path.Combine(Path.GetDirectoryName(htmlLocation), "IE", Path.GetFileName(htmlLocation)), ApiBrowserType.IE);
                    reportMaker.CreateDefaultHtmlReport(Path.Combine(Path.GetDirectoryName(htmlLocation), "Netscape", Path.GetFileName(htmlLocation)), ApiBrowserType.Netscape);


                    XmlDocument xmlDoc = new XmlDocument();

                    session.ErrorMessages.LoadValuesIfNeeded();
                    if (session.ErrorMessages.Count != 0)
                    {
                        if (session.ErrorMessages.Count > 1000)
                        {
                            Console.WriteLine("more then 1000 error during scenario run, analyzing only the first 1000.");
                        }
                        Console.WriteLine(Resources.ErrorsReportTitle);
                        XmlElement errorRoot = xmlDoc.CreateElement("Errors");
                        xmlDoc.AppendChild(errorRoot);
                        int limit = 1000;
                        ErrorMessage[] errors = session.ErrorMessages.ToArray();
                        //foreach (ErrorMessage err in session.ErrorMessages)
                        for (int i = 0; i < limit && i < errors.Length; i++)
                        {
                            ErrorMessage err = errors[i];
                            XmlElement elem = xmlDoc.CreateElement("Error");
                            elem.SetAttribute("ID", err.ID.ToString());
                            elem.AppendChild(xmlDoc.CreateTextNode(err.Name));
                            Console.WriteLine("ID: " + err.ID + " Name: " + err.Name);
                            errorRoot.AppendChild(elem);
                        }
                        xmlDoc.Save(Path.Combine(Path.GetDirectoryName(Path.GetDirectoryName(lrrlocation)), "Errors.xml"));

                        xmlDoc.RemoveAll();
                        Console.WriteLine("");
                    }
                    session.Close();
                    Console.WriteLine(Resources.SLAReportTitle);
                    SlaResult slaResult = Session.CalculateSla(lralocation, true);
                    XmlElement root = xmlDoc.CreateElement("SLA");
                    xmlDoc.AppendChild(root);

                    int iCounter = 0; // set counter
                    foreach (SlaWholeRunRuleResult a in slaResult.WholeRunRules)
                    {
                        Console.WriteLine(Resources.DoubleLineSeperator);
                        XmlElement elem;
                        if (a.Measurement.Equals(SlaMeasurement.PercentileTRT))
                        {
                            SlaPercentileRuleResult b = slaResult.TransactionRules.PercentileRules[iCounter];
                            elem = xmlDoc.CreateElement(b.RuleName);
                            Console.WriteLine("Transaction Name : " + b.TransactionName);
                            elem.SetAttribute("TransactionName", b.TransactionName.ToString());
                            Console.WriteLine("Percentile : " + b.Percentage);
                            elem.SetAttribute("Percentile", b.Percentage.ToString());
                            elem.SetAttribute("FullName", b.RuleUiName);
                            Console.WriteLine("Full Name : " + b.RuleUiName);
                            Console.WriteLine("Measurement : " + b.Measurement);
                            elem.SetAttribute("Measurement", b.Measurement.ToString());
                            Console.WriteLine("Goal Value : " + b.GoalValue);
                            elem.SetAttribute("GoalValue", b.GoalValue.ToString());
                            Console.WriteLine("Actual value : " + b.ActualValue);
                            elem.SetAttribute("ActualValue", b.ActualValue.ToString());
                            Console.WriteLine("status : " + b.Status);
                            elem.AppendChild(xmlDoc.CreateTextNode(b.Status.ToString()));

                            if (!b.Status.Equals(SlaRuleStatus.Passed)) // 0 = failed
                            {
                                iPassed = (int)Launcher.ExitCodeEnum.Failed;
                            }
                            iCounter++;
                        }
                        else
                        {
                            elem = xmlDoc.CreateElement(a.RuleName);
                            elem.SetAttribute("FullName", a.RuleUiName);
                            Console.WriteLine("Full Name : " + a.RuleUiName);
                            Console.WriteLine("Measurement : " + a.Measurement);
                            elem.SetAttribute("Measurement", a.Measurement.ToString());
                            Console.WriteLine("Goal Value : " + a.GoalValue);
                            elem.SetAttribute("GoalValue", a.GoalValue.ToString());
                            Console.WriteLine("Actual value : " + a.ActualValue);
                            elem.SetAttribute("ActualValue", a.ActualValue.ToString());
                            Console.WriteLine("status : " + a.Status);
                            elem.AppendChild(xmlDoc.CreateTextNode(a.Status.ToString()));

                            if (!a.Status.Equals(SlaRuleStatus.Passed)) // 0 = failed
                            {
                                iPassed = (int)Launcher.ExitCodeEnum.Failed;
                            }
                        }
                        root.AppendChild(elem);
                        Console.WriteLine(Resources.DoubleLineSeperator);
                    }

                    iCounter = 0; // reset counter
                    foreach (SlaTimeRangeRuleResult a in slaResult.TimeRangeRules)
                    {
                        
                        Console.WriteLine(Resources.DoubleLineSeperator);
                        XmlElement rule;
                        if (a.Measurement.Equals(SlaMeasurement.AverageTRT))
                         {
                        SlaTransactionTimeRangeRuleResult b = slaResult.TransactionRules.TimeRangeRules[iCounter];
                            rule = xmlDoc.CreateElement(b.RuleName);
                            Console.WriteLine("Transaction Name: " + b.TransactionName);
                            rule.SetAttribute("TransactionName", b.TransactionName);
                            Console.WriteLine("Full Name : " + b.RuleUiName);
                            rule.SetAttribute("FullName", b.RuleUiName);
                            Console.WriteLine("Measurement : " + b.Measurement);
                            rule.SetAttribute("Measurement", b.Measurement.ToString());
                            Console.WriteLine("SLA Load Threshold Value : " + b.CriteriaMeasurement);
                            rule.SetAttribute("SLALoadThresholdValue", b.CriteriaMeasurement.ToString());
 
                            foreach (SlaLoadThreshold slat in b.LoadThresholds)
                            {
                                XmlElement loadThr = xmlDoc.CreateElement("SlaLoadThreshold");
                                loadThr.SetAttribute("StartLoadValue", slat.StartLoadValue.ToString());
                                loadThr.SetAttribute("EndLoadValue", slat.EndLoadValue.ToString());
                                loadThr.SetAttribute("ThresholdValue", slat.ThresholdValue.ToString());
                                rule.AppendChild(loadThr);

                            }
                            XmlElement timeRanges = xmlDoc.CreateElement("TimeRanges");
                            foreach (SlaTimeRangeInfo slatri in b.TimeRanges)
                            {
                                XmlElement subsubelem = xmlDoc.CreateElement("TimeRangeInfo");
                                subsubelem.SetAttribute("StartTime", slatri.StartTime.ToString());
                                subsubelem.SetAttribute("EndTime", slatri.EndTime.ToString());
                                subsubelem.SetAttribute("GoalValue", slatri.GoalValue.ToString());
                                subsubelem.SetAttribute("ActualValue", slatri.ActualValue.ToString());
                               subsubelem.SetAttribute("LoadValue", slatri.LoadValue.ToString());
                                subsubelem.InnerText = slatri.Status.ToString();
                                timeRanges.AppendChild(subsubelem);
                            }
                            rule.AppendChild(timeRanges);
                           Console.WriteLine("status : " + b.Status);
                            rule.AppendChild(xmlDoc.CreateTextNode(b.Status.ToString()));
                            if (!b.Status.Equals(SlaRuleStatus.Passed)) // 0 = failed
                            {
                                iPassed = (int)Launcher.ExitCodeEnum.Failed;
                            }
                            iCounter++;
                         }
                        else
                        {
                            rule = xmlDoc.CreateElement(a.RuleName);
                            Console.WriteLine("Full Name : " + a.RuleUiName);
                            rule.SetAttribute("FullName", a.RuleUiName);
                            Console.WriteLine("Measurement : " + a.Measurement);
                            rule.SetAttribute("Measurement", a.Measurement.ToString());
                            Console.WriteLine("SLA Load Threshold Value : " + a.CriteriaMeasurement);
                            rule.SetAttribute("SLALoadThresholdValue", a.CriteriaMeasurement.ToString());

                            foreach (SlaLoadThreshold slat in a.LoadThresholds)
                            {
                                XmlElement loadThr = xmlDoc.CreateElement("SlaLoadThreshold");
                                loadThr.SetAttribute("StartLoadValue", slat.StartLoadValue.ToString());
                                loadThr.SetAttribute("EndLoadValue", slat.EndLoadValue.ToString());
                                loadThr.SetAttribute("ThresholdValue", slat.ThresholdValue.ToString());
                                rule.AppendChild(loadThr);

                            }
                            XmlElement timeRanges = xmlDoc.CreateElement("TimeRanges");
                            foreach (SlaTimeRangeInfo slatri in a.TimeRanges)
                            {
                                XmlElement subsubelem = xmlDoc.CreateElement("TimeRangeInfo");
                                subsubelem.SetAttribute("StartTime", slatri.StartTime.ToString());
                                subsubelem.SetAttribute("EndTime", slatri.EndTime.ToString());
                                subsubelem.SetAttribute("GoalValue", slatri.GoalValue.ToString());
                                subsubelem.SetAttribute("ActualValue", slatri.ActualValue.ToString());
                                subsubelem.SetAttribute("LoadValue", slatri.LoadValue.ToString());
                                subsubelem.InnerText = slatri.Status.ToString();
                                timeRanges.AppendChild(subsubelem);
                            }
                            rule.AppendChild(timeRanges);
                            Console.WriteLine("status : " + a.Status);
                            rule.AppendChild(xmlDoc.CreateTextNode(a.Status.ToString()));
                            if (!a.Status.Equals(SlaRuleStatus.Passed))
                            {
                                iPassed = (int)Launcher.ExitCodeEnum.Failed;
                            }
                        
                        }
                        root.AppendChild(rule);

                        Console.WriteLine(Resources.DoubleLineSeperator);
                    }

                    //write XML to location:
                    xmlDoc.Save(Path.Combine(Path.GetDirectoryName(Path.GetDirectoryName(lrrlocation)), "SLA.xml"));

                }
                else
                {

                    Console.WriteLine(Resources.CannotCreateSession);
                    return (int)Launcher.ExitCodeEnum.Aborted;
                }

                session.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                return (int)Launcher.ExitCodeEnum.Aborted;
            }

            // return SLA status code, if any SLA fails return a fail here.
            return iPassed;

        }

        static System.Reflection.Assembly CurrentDomain_AssemblyResolve(object sender, ResolveEventArgs args)
        {
            System.Reflection.AssemblyName name = new System.Reflection.AssemblyName(args.Name);
            if (name.Name.ToLowerInvariant().EndsWith(".resources")) return null;
            string installPath = HpToolsLauncher.Utils.Helper.getLRInstallPath();
            if (installPath == null)
            {
                Console.WriteLine(Resources.CannotLocateInstallDir);
                Environment.Exit((int)Launcher.ExitCodeEnum.Aborted);
            }
            //Console.WriteLine(Path.Combine(installPath, "bin", name.Name + ".dll"));
            return System.Reflection.Assembly.LoadFrom(Path.Combine(installPath, "bin", name.Name + ".dll"));
        }

        private static void ShowHelp()
        {
            Console.WriteLine("HP LoadRunner Analysis Command Line Executer");
            Console.WriteLine();
            Console.Write("Usage: LRAnalysisLauncher.exe");
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.Write("[.lrr file location] [.lra output location] [html report output folder]");
            Console.ResetColor();
            Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
        }
    }
}
