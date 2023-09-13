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

using System.Runtime.InteropServices;
using System;
using System.Security;
using System.Collections.Generic;
using System.Linq;
using Microsoft.SqlServer.Server;

namespace HpToolsLauncher.Utils
{
    internal static class Extensions
    {
        public static SecureString ToSecureString(this string plainString)
        {
            if (plainString == null)
                return null;

            SecureString secureString = new SecureString();
            foreach (char c in plainString.ToCharArray())
            {
                secureString.AppendChar(c);
            }
            return secureString;
        }
        public static string ToPlainString(this SecureString value)
        {
            IntPtr valuePtr = IntPtr.Zero;
            try
            {
                valuePtr = Marshal.SecureStringToBSTR(value);
                return Marshal.PtrToStringBSTR(valuePtr);
            }
            finally
            {
                Marshal.ZeroFreeBSTR(valuePtr);
            }
        }

        public static bool IsNullOrEmpty(this string value)
        {
            return string.IsNullOrEmpty(value);
        }
        public static bool IsNullOrWhiteSpace(this string value)
        {
            return string.IsNullOrWhiteSpace(value);
        }

        public static bool IsEmptyOrWhiteSpace(this string str)
        {
            return str != null && str.Trim() == string.Empty;
        }

        public static bool IsValidUrl(this string url)
        {
            return Uri.IsWellFormedUriString(url, UriKind.RelativeOrAbsolute);
        }

        public static bool EqualsIgnoreCase(this string s1, string s2)
        {
            return s1?.Equals(s2, StringComparison.OrdinalIgnoreCase) ?? (s2 == null);
        }

        public static bool StartsWithIngoreCase(this string str, string prefix)
        {
            return str.StartsWith(prefix, StringComparison.OrdinalIgnoreCase);
        }

        public static bool In(this string str, bool ignoreCase, params string[] values)
        {
            if (ignoreCase)
            {
                return values?.Any((string s) => EqualsIgnoreCase(str, s)) ?? (str == null);
            }
            return In(str, values);
        }

        public static bool In<T>(this T obj, params T[] values)
        {
            return values?.Any((T o) => Equals(obj, o)) ?? false;
        }

        public static bool IsNullOrEmpty<T>(this T[] arr)
        {
            return arr == null || arr.Length == 0;
        }

        // ICollection is base class of IList and IDictionary
        public static bool IsNullOrEmpty<T>(this ICollection<T> coll)
        {
            return coll == null || coll.Count == 0;
        }

        public static bool IsNullOrEmpty<T>(this IEnumerable<T> coll)
        {
            return coll?.Any() != true;
        }

        public static bool HasAny<T>(this T[] arr)
        {
            return arr?.Length > 0;
        }

        public static bool HasAny<T>(this ICollection<T> coll)
        {
            return coll?.Any() == true;
        }
        public static bool HasAny<T>(this IEnumerable<T> coll)
        {
            return coll?.Any() == true;
        }

        public static bool IsNull(this DateTime dt)
        {
            if (Convert.GetTypeCode(dt) != 0 && (dt.Date != DateTime.MinValue.Date))
            {
                return dt.Date == DateTime.MaxValue.Date;
            }
            return true;
        }

        public static bool IsNullOrEmpty(this DateTime? dt)
        {
            if (dt.HasValue)
            {
                return IsNull(dt.Value);
            }
            return true;
        }

        public static void ForEach<T>(this IEnumerable<T> enumeration, Action<T> action)
        {
            foreach (T item in enumeration)
            {
                action(item);
            }
        }
    }
}
