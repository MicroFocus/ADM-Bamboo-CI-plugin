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
using System;
using System.Linq;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class McConnectionInfo
    {
        private const string PAIR_SEPARATOR = "=";
        private const string TOKEN_SEPARATOR = ";";
        private const string YES = "Yes";
        private const string NO = "No";
        private const string SYSTEM = "System";
        private const string HTTP = "Http";
        private const string HTTPS = "Https";
        private const string PORT_8080 = "8080";
        private const string PORT_443 = "443";
        private const string CLIENT = "client";
        private const string SECRET = "secret";
        private const string TENANT = "tenant";
        private const int ZERO = 0;
        private const int ONE = 1;
        private static readonly char[] SLASH = new char[] {'/'};
        private static readonly char[] COMMA = new char[] { ':' };
        private static readonly char[] DBL_QUOTE = new char[] { '"' };

        private const string MOBILEHOSTADDRESS = "MobileHostAddress";
        private const string MOBILEUSESSL = "MobileUseSSL"; 
        private const string MOBILEUSERNAME = "MobileUserName";
        private const string MOBILEPASSWORD = "MobilePassword";
        private const string MOBILETENANTID = "MobileTenantId";
        private const string MOBILEEXECTOKEN = "MobileExecToken";
        private const string MOBILEUSEPROXY = "MobileUseProxy";
        private const string MOBILEPROXYTYPE = "MobileProxyType";
        private const string MOBILEPROXYSETTING_ADDRESS = "MobileProxySetting_Address";
        private const string MOBILEPROXYSETTING_AUTHENTICATION = "MobileProxySetting_Authentication";
        private const string MOBILEPROXYSETTING_USERNAME = "MobileProxySetting_UserName";
        private const string MOBILEPROXYSETTING_PASSWORD = "MobileProxySetting_Password";

        // auth types for MC
        public enum AuthType
        {
            UsernamePassword,
            AuthToken
        }

        public struct AuthTokenInfo
        {
            public string ClientId { get; set; }
            public string SecretKey { get; set; }
        }

        public McConnectionInfo()
        {
            HostPort = PORT_8080;
            UserName = string.Empty;
            ExecToken = string.Empty;
            Password = string.Empty;
            HostAddress = string.Empty;
            TenantId = string.Empty;
            UseSslAsInt = ZERO;

            UseProxyAsInt = ZERO;
            ProxyType = ZERO;
            ProxyAddress = string.Empty;
            ProxyPort = ZERO;
            ProxyAuth = ZERO;
            ProxyUserName = string.Empty;
            ProxyPassword = string.Empty;
        }

        // if token auth was specified this is populated
        private AuthTokenInfo _tokens;
        private string _execToken;
        private AuthType _authType = AuthType.UsernamePassword;

        public string UserName { get; set; }
        public string Password { get; set; }
        public string ClientId { get; set; }
        public string SecretKey { get; set; }

        public string ExecToken
        {
            get
            {
                return _execToken;
            }
            set
            {
                _execToken = value;
                _tokens = ParseExecToken();
            }
        }

        public AuthType MobileAuthType
        {
            get
            {
                return _authType;
            }
            private set
            {
                _authType = value;
            }
        }

        public string HostAddress { get; set; }
        public string HostPort { get; set; }
        public string TenantId { get; set; }
        public int UseSslAsInt { get; set; }
        public bool UseProxy => UseProxyAsInt == ONE;
        public int UseProxyAsInt { get; set; }
        public int ProxyType { get; set; }
        public string ProxyAddress { get; set; }
        public int ProxyPort { get; set; }
        public int ProxyAuth { get; set; }
        public string ProxyUserName { get; set; }
        public string ProxyPassword { get; set; }

        public McConnectionInfo(JavaProperties ciParams) : this()
        {
            if (ciParams.ContainsKey(MOBILEHOSTADDRESS))
            {
                string mcServerUrl = ciParams[MOBILEHOSTADDRESS].Trim();
                if (!string.IsNullOrEmpty(mcServerUrl))
                {
                    //ssl
                    bool useSSL = false;
                    if (ciParams.ContainsKey(MOBILEUSESSL))
                    {
                        int.TryParse(ciParams[MOBILEUSESSL], out int mcUseSslAsInt);
                        UseSslAsInt = mcUseSslAsInt;
                        useSSL = mcUseSslAsInt == ONE;
                    }

                    //url is something like http://xxx.xxx.xxx.xxx:8080
                    string[] arr = mcServerUrl.Split(COMMA, StringSplitOptions.RemoveEmptyEntries);
                    if (arr.Length == 1)
                    {
                        if (arr[0].Trim().In(true, HTTP, HTTPS))
                            throw new ArgumentException(string.Format(Resources.McInvalidUrl, mcServerUrl));
                        HostAddress = arr[0].TrimEnd(SLASH);
                        HostPort = useSSL ? PORT_443 : PORT_8080;
                    }
                    else if (arr.Length == 2)
                    {
                        if (arr[0].Trim().In(true, HTTP, HTTPS))
                        {
                            HostAddress = arr[1].Trim(SLASH);
                            HostPort = useSSL ? PORT_443 : PORT_8080;
                        }
                        else
                        {
                            HostAddress = arr[0].Trim(SLASH);
                            HostPort = arr[1].Trim();
                        }
                    }
                    else if (arr.Length == 3)
                    {
                        HostAddress = arr[1].Trim(SLASH);
                        HostPort = arr[2].Trim();
                    }

                    if (HostAddress.Trim() == string.Empty)
                    {
                        throw new ArgumentException(Resources.McEmptyHostAddress);
                    }

                    //mc username
                    if (ciParams.ContainsKey(MOBILEUSERNAME))
                    {
                        string mcUsername = ciParams[MOBILEUSERNAME].Trim();
                        if (!string.IsNullOrEmpty(mcUsername))
                        {
                            UserName = mcUsername;
                        }
                    }

                    //mc password
                    if (ciParams.ContainsKey(MOBILEPASSWORD))
                    {
                        string mcPassword = ciParams[MOBILEPASSWORD];
                        if (!string.IsNullOrEmpty(mcPassword))
                        {
                            Password = Aes256Encryptor.Instance.Decrypt(mcPassword);
                        }
                    }

                    //mc tenantId
                    if (ciParams.ContainsKey(MOBILETENANTID))
                    {
                        string mcTenantId = ciParams[MOBILETENANTID];
                        if (!string.IsNullOrEmpty(mcTenantId))
                        {
                            TenantId = mcTenantId;
                        }
                    }

                    //mc exec token	
                    if (ciParams.ContainsKey(MOBILEEXECTOKEN))
                    {
                        var mcExecToken = ciParams[MOBILEEXECTOKEN];
                        if (!string.IsNullOrEmpty(mcExecToken))
                        {
                            ExecToken = Aes256Encryptor.Instance.Decrypt(mcExecToken);
                        }
                    }

                    //Proxy enabled flag
                    if (ciParams.ContainsKey(MOBILEUSEPROXY))
                    {
                        string useProxy = ciParams[MOBILEUSEPROXY];
                        int.TryParse(useProxy, out int mcUseProxyAsInt);
                        UseProxyAsInt = mcUseProxyAsInt;
                    }

                    //Proxy type
                    if (ciParams.ContainsKey(MOBILEPROXYTYPE))
                    {
                        string proxyType = ciParams[MOBILEPROXYTYPE];
                        if (!string.IsNullOrEmpty(proxyType))
                        {
                            ProxyType = int.Parse(proxyType);
                        }
                    }

                    //proxy address
                    string proxyAddr = ciParams.GetOrDefault(MOBILEPROXYSETTING_ADDRESS);
                    if (!string.IsNullOrEmpty(proxyAddr))
                    {
                        // data is something like "16.105.9.23:8080"
                        string[] arrProxyAddr = proxyAddr.Split(new char[] { ':' });
                        if (arrProxyAddr.Length == 2)
                        {
                            ProxyAddress = arrProxyAddr[0];
                            ProxyPort = int.Parse(arrProxyAddr[1]);
                        }
                    }


                    //Proxy authentication
                    if (ciParams.ContainsKey(MOBILEPROXYSETTING_AUTHENTICATION))
                    {
                        string strProxyAuth = ciParams[MOBILEPROXYSETTING_AUTHENTICATION];
                        if (!string.IsNullOrEmpty(strProxyAuth))
                        {
                            ProxyAuth = int.Parse(strProxyAuth);
                        }
                    }

                    //Proxy username
                    if (ciParams.ContainsKey(MOBILEPROXYSETTING_USERNAME))
                    {
                        string proxyUsername = ciParams[MOBILEPROXYSETTING_USERNAME].Trim();
                        if (!string.IsNullOrEmpty(proxyUsername))
                        {
                            ProxyUserName = proxyUsername;
                        }
                    }

                    //Proxy password
                    if (ciParams.ContainsKey(MOBILEPROXYSETTING_PASSWORD))
                    {
                        string proxyPassword = ciParams[MOBILEPROXYSETTING_PASSWORD];
                        if (!string.IsNullOrEmpty(proxyPassword))
                        {
                            ProxyPassword = Aes256Encryptor.Instance.Decrypt(proxyPassword);
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Parses the execution token and separates into three parts: clientId, secretKey and tenantId
        /// </summary>
        /// <returns></returns>
        /// <exception cref="ArgumentException"></exception>
        private AuthTokenInfo ParseExecToken()
        {
            // exec token consists of three parts:
            // 1. client id
            // 2. secret key
            // 3. optionally tenant id
            // separator is ;
            // key-value pairs are separated with =

            // e.g., "client=oauth2-QHxvc8bOSz4lwgMqts2w@microfocus.com; secret=EHJp8ea6jnVNqoLN6HkD; tenant=999999999;"
            // "client=oauth2-OuV8k3snnGp9vJugC1Zn@microfocus.com; secret=6XSquF1FUD4CyQM7fb0B; tenant=999999999;"
            // "client=oauth2-OuV8k3snnGp9vJugC1Zn@microfocus.com; secret=6XSquF1FUD7CyQM7fb0B; tenant=999999999;"
            var execToken = ExecToken.Trim().Trim(DBL_QUOTE);

            var ret = new AuthTokenInfo();

            if (execToken.Length == 0) return ret; // empty string was given as token, may semnalize that it wasn't specified

            var tokens = execToken.Split(TOKEN_SEPARATOR.ToCharArray(), StringSplitOptions.RemoveEmptyEntries);

            if (tokens.Length != 3) throw new ArgumentException(Resources.McInvalidToken);
            if (!tokens.All(token => token.Contains(PAIR_SEPARATOR)))
                throw new ArgumentException(string.Format(Resources.McMalformedTokenInvalidKeyValueSeparator, PAIR_SEPARATOR));

            // key-values are separated by =, we need its value, the key is known
            foreach (var token in tokens)
            {
                var parts = token.Split(PAIR_SEPARATOR.ToCharArray());

                if (parts.Length != 2)
                    throw new ArgumentException(Resources.McMalformedTokenMissingKeyValuePair);

                var key = parts[0].Trim();
                var value = parts[1].Trim();

                if (CLIENT.EqualsIgnoreCase(key))
                {
                    ret.ClientId = value;
                }
                else if (SECRET.EqualsIgnoreCase(key))
                {
                    ret.SecretKey = value;
                }
                else if (TENANT.EqualsIgnoreCase(key))
                {
                    TenantId = value;
                }
                else
                {
                    throw new ArgumentException(string.Format(Resources.McMalformedTokenInvalidKey, key));
                }
            }

            _authType = AuthType.AuthToken;
            return ret;
        }

        /// <summary>
        /// Returns the parsed tokens from the execution token.
        /// </summary>
        /// <returns></returns>
        public AuthTokenInfo GetAuthToken()
        {
            return _tokens;
        }

        private string UseSslAsStr => UseSslAsInt == ONE ? YES : NO; 

        private string UseProxyAsStr => UseProxyAsInt == ONE ? YES : NO;

        private string ProxyTypeAsStr => ProxyType == ONE ? SYSTEM : HTTP;

        private string ProxyAuthAsStr => ProxyAuth == ONE ? YES : NO;

        public override string ToString()
        {
            string usernameOrClientId = string.Empty;
            if (MobileAuthType == AuthType.AuthToken)
            {
                usernameOrClientId = $"ClientId: {ClientId}";
            }
            else if (MobileAuthType == AuthType.UsernamePassword)
            {
                usernameOrClientId = $"Username: {UserName}" ;
            }
            string strProxy = $"UseProxy: {UseProxyAsStr}";
            if (UseProxy)
            {
                strProxy += $", ProxyType: {ProxyTypeAsStr}, ProxyAddress: {ProxyAddress}, ProxyPort: {ProxyPort}, ProxyAuth: {ProxyAuthAsStr}, ProxyUser: {ProxyUserName}";
            }
            return $"HostAddress: {HostAddress}, Port: {HostPort}, AuthType: {MobileAuthType}, {usernameOrClientId}, TenantId: {TenantId}, UseSSL: {UseSslAsStr}, {strProxy}";
        }
    }

}
