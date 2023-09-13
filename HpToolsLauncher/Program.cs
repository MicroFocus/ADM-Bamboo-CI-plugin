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
 * its affiliates and licensors (“Open Text”) are as may be set forth
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
using System.Linq;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public enum TestStorageType
    {
        Alm,
        AlmLabManagement,
        FileSystem,
        LoadRunner,
        Unknown
    }

    class Program
    {
        private static readonly Dictionary<string, string> argsDictionary = new();

        static void Main(string[] args)
        {
            ConsoleWriter.WriteLine(Resources.GeneralStarted);
            
            if (args.Count() == 0 || args.Contains("/?"))
            {
                ShowHelp();
                return;
            }
            for (int i = 0; i < args.Count(); i += 2)
            {
                string key = args[i].StartsWith("-") ? args[i].Substring(1) : args[i];
                string val = i + 1 < args.Count() ? args[i + 1].Trim() : string.Empty;
                argsDictionary[key] = val;
            }
            string failOnTestFailed = "N";
            argsDictionary.TryGetValue("runtype", out string runtype);
            argsDictionary.TryGetValue("paramfile", out string paramFileName);
            if (!Enum.TryParse(runtype, true, out TestStorageType enmRuntype))
                enmRuntype = TestStorageType.Unknown;

            if (paramFileName.IsNullOrEmpty())
            {
                ShowHelp();
                return;
            }
            var apiRunner = new Launcher(failOnTestFailed, paramFileName, enmRuntype);

            apiRunner.Run();
        }

        private static void ShowHelp()
        {
            Console.WriteLine("HP Automation Tools Command Line Executer");
            Console.WriteLine();
            Console.Write("Usage: HpToolsLauncher.exe");
            Console.Write("  -paramfile ");
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.Write("<a file in key=value format> ");
            Console.ResetColor();
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine("-paramfile is required in for the program to run");
            Console.WriteLine("the parameter file may contain the following fields:");
            Console.WriteLine("\trunType=<Alm/FileSystem/LoadRunner>");
            Console.WriteLine("\talmServerUrl=http://<server>:<port>/qcbin");
            Console.WriteLine("\talmUserName=<user>");
            Console.WriteLine("\talmPassword=<password>");
            Console.WriteLine("\talmDomain=<domain>");
            Console.WriteLine("\talmProject=<project>");
            Console.WriteLine("\talmRunMode=<RUN_LOCAL/RUN_REMOTE/RUN_PLANNED_HOST>");
            Console.WriteLine("\talmTimeout=<-1>/<numberOfSeconds>");
            Console.WriteLine("\talmRunHost=<hostname>");
            Console.WriteLine("\tTestSet<number starting at 1>=<testSet>/<AlmFolder>");
            Console.WriteLine("\tTest<number starting at 1>=<testFolderPath>/<a Path ContainingTestFolders>/<mtbFilePath>");
            Console.WriteLine("* the last two fields may recur more than once with different index numbers");
            Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
        }
    }
}
