﻿using System.Runtime.InteropServices;
using System;
using System.Security;
using System.Collections.Generic;
using System.Linq;

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