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

using System.Security;

namespace HpToolsLauncher.Utils
{
    public class RunAsUser
    {
        private readonly string _username;
        private readonly string _encodedPwd;
        private readonly SecureString _pwd;

        public RunAsUser(string username, string encodedPwd)
        {
            _username = username;
            _encodedPwd = encodedPwd;
            _pwd =  Encoder.Decode(_encodedPwd).ToSecureString();
        }
        public RunAsUser(string username, SecureString pwd)
        {
            _username = username;
            _pwd = pwd;
            _encodedPwd = Encoder.Encode(_pwd.ToPlainString());
        }
        public string Username
        {
            get { return _username; }
        }

        public string EncodedPassword
        {
            get { return _encodedPwd; }
        }

        public SecureString Password
        {
            get { return _pwd; }
        }
    }
}