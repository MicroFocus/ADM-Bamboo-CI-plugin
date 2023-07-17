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
        private const int ZERO = 0;
        private const int ONE = 1;
        private static readonly char[] _slash = new char[] {'/'};
        private static readonly char[] _comma = new char[] { ':' };

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
            MobileUserName = string.Empty;
            MobileExecToken = string.Empty;
            MobilePassword = string.Empty;
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

        public string MobileUserName { get; set; }
        public string MobilePassword { get; set; }
        public string MobileClientId { get; set; }
        public string MobileSecretKey { get; set; }

        public string MobileExecToken
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
            if (ciParams.ContainsKey("MobileHostAddress"))
            {
                string mcServerUrl = ciParams["MobileHostAddress"].Trim();
                if (!string.IsNullOrEmpty(mcServerUrl))
                {
                    //ssl
                    bool useSSL = false;
                    if (ciParams.ContainsKey("MobileUseSSL"))
                    {
                        int.TryParse(ciParams["MobileUseSSL"], out int mcUseSslAsInt);
                        UseSslAsInt = mcUseSslAsInt;
                        useSSL = mcUseSslAsInt == ONE;
                    }

                    //url is something like http://xxx.xxx.xxx.xxx:8080
                    string[] arr = mcServerUrl.Split(_comma, StringSplitOptions.RemoveEmptyEntries);
                    if (arr.Length == 1)
                    {
                        if (arr[0].Trim().In(true, HTTP, HTTPS))
                            throw new ArgumentException(string.Format(Resources.McInvalidUrl, mcServerUrl));
                        HostAddress = arr[0].TrimEnd(_slash);
                        HostPort = useSSL ? PORT_443 : PORT_8080;
                    }
                    else if (arr.Length == 2)
                    {
                        if (arr[0].Trim().In(true, HTTP, HTTPS))
                        {
                            HostAddress = arr[1].Trim(_slash);
                            HostPort = useSSL ? PORT_443 : PORT_8080;
                        }
                        else
                        {
                            HostAddress = arr[0].Trim(_slash);
                            HostPort = arr[1].Trim();
                        }
                    }
                    else if (arr.Length == 3)
                    {
                        HostAddress = arr[1].Trim(_slash);
                        HostPort = arr[2].Trim();
                    }

                    if (HostAddress.Trim() == string.Empty)
                    {
                        throw new ArgumentException(Resources.McEmptyHostAddress);
                    }

                    //mc username
                    if (ciParams.ContainsKey("MobileUserName"))
                    {
                        string mcUsername = ciParams["MobileUserName"].Trim();
                        if (!string.IsNullOrEmpty(mcUsername))
                        {
                            MobileUserName = mcUsername;
                        }
                    }

                    //mc password
                    if (ciParams.ContainsKey("MobilePassword"))
                    {
                        string mcPassword = ciParams["MobilePassword"];
                        if (!string.IsNullOrEmpty(mcPassword))
                        {
                            MobilePassword = Aes256Encryptor.Instance.Decrypt(mcPassword);
                        }
                    }

                    //mc tenantId
                    if (ciParams.ContainsKey("MobileTenantId"))
                    {
                        string mcTenantId = ciParams["MobileTenantId"];
                        if (!string.IsNullOrEmpty(mcTenantId))
                        {
                            TenantId = mcTenantId;
                        }
                    }

                    //mc exec token	
                    if (ciParams.ContainsKey("MobileExecToken"))
                    {
                        var mcExecToken = ciParams["MobileExecToken"];
                        if (!string.IsNullOrEmpty(mcExecToken))
                        {
                            MobileExecToken = Aes256Encryptor.Instance.Decrypt(mcExecToken);
                        }
                    }

                    //Proxy enabled flag
                    if (ciParams.ContainsKey("MobileUseProxy"))
                    {
                        string useProxy = ciParams["MobileUseProxy"];
                        int.TryParse(useProxy, out int mcUseProxyAsInt);
                        UseProxyAsInt = mcUseProxyAsInt;
                    }

                    //Proxy type
                    if (ciParams.ContainsKey("MobileProxyType"))
                    {
                        string proxyType = ciParams["MobileProxyType"];
                        if (!string.IsNullOrEmpty(proxyType))
                        {
                            ProxyType = int.Parse(proxyType);
                        }
                    }

                    //proxy address
                    string proxyAddress = ciParams.GetOrDefault("MobileProxySetting_Address");
                    if (!string.IsNullOrEmpty(proxyAddress))
                    {
                        // data is something like "16.105.9.23:8080"
                        string[] strArrayForProxyAddress = proxyAddress.Split(new char[] { ':' });
                        if (strArrayForProxyAddress.Length == 2)
                        {
                            ProxyAddress = strArrayForProxyAddress[0];
                            ProxyPort = int.Parse(strArrayForProxyAddress[1]);
                        }
                    }


                    //Proxy authentication
                    if (ciParams.ContainsKey("MobileProxySetting_Authentication"))
                    {
                        string proxyAuthentication = ciParams["MobileProxySetting_Authentication"];
                        if (!string.IsNullOrEmpty(proxyAuthentication))
                        {
                            ProxyAuth = int.Parse(proxyAuthentication);
                        }
                    }

                    //Proxy username
                    if (ciParams.ContainsKey("MobileProxySetting_UserName"))
                    {
                        string proxyUsername = ciParams["MobileProxySetting_UserName"].Trim();
                        if (!string.IsNullOrEmpty(proxyUsername))
                        {
                            ProxyUserName = proxyUsername;
                        }
                    }

                    //Proxy password
                    if (ciParams.ContainsKey("MobileProxySetting_Password"))
                    {
                        string proxyPassword = ciParams["MobileProxySetting_Password"];
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
            var execToken = MobileExecToken.Trim();

            var ret = new AuthTokenInfo();

            // it may or may not contains surrounding quotes
            if (execToken.StartsWith("\"") && execToken.EndsWith("\"") && execToken.Length > 1)
            {
                execToken = execToken.Substring(1, execToken.Length - 2);
            }

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

                if ("client".EqualsIgnoreCase(key))
                {
                    ret.ClientId = value;
                }
                else if ("secret".EqualsIgnoreCase(key))
                {
                    ret.SecretKey = value;
                }
                else if ("tenant".EqualsIgnoreCase(key))
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
                usernameOrClientId = string.Format("ClientId: {0}", MobileClientId);
            }
            else if (MobileAuthType == AuthType.UsernamePassword)
            {
                usernameOrClientId = string.Format("Username: {0}", MobileUserName);
            }
            string strProxy = string.Format("UseProxy: {0}", UseProxyAsStr);
            if (UseProxy)
            {
                strProxy += string.Format(", ProxyType: {0}, ProxyAddress: {1}, ProxyPort: {2}, ProxyAuth: {3}, ProxyUser: {4}",
                    ProxyTypeAsStr, ProxyAddress, ProxyPort, ProxyAuthAsStr, ProxyUserName);
            }
            string mcConnStr =
                 string.Format("HostAddress: {0}, Port: {1}, AuthType: {2}, {3}, TenantId: {4}, UseSSL: {5}, {6}",
                 HostAddress, HostPort, MobileAuthType, usernameOrClientId, TenantId, UseSslAsStr, strProxy);
            return mcConnStr;
        }
    }

}
