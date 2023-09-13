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
using System.Security.Cryptography;
using System.Text;

namespace HpToolsLauncher.Utils
{
    public sealed class Aes256Encryptor
    {
        private const string AES_256_SECRET_KEY = "AES_256_SECRET_KEY";
        private const string AES_256_SECRET_INIT_VECTOR = "AES_256_SECRET_INIT_VECTOR";
        private readonly string _secretKey;
        private readonly string _initVector;

        private static readonly Lazy<Aes256Encryptor> _instance = new Lazy<Aes256Encryptor>(() => new Aes256Encryptor());

        public static Aes256Encryptor Instance => _instance.Value;

        private Aes256Encryptor()
        {
            _secretKey = Environment.GetEnvironmentVariable(AES_256_SECRET_KEY);
            _initVector = Environment.GetEnvironmentVariable(AES_256_SECRET_INIT_VECTOR);
        }

        /// <summary>
        /// Internal usage only, used for private key decryption.
        /// </summary>
        /// <param name="textToDecrypt"></param>
        /// <returns></returns>
        public string Decrypt(string textToDecrypt)
        {
#if DEBUG
            return textToDecrypt;
#else
            RijndaelManaged rijndaelCipher = new()
            {
                Mode = CipherMode.CBC,
                Padding = PaddingMode.PKCS7,
                KeySize = 256,
                BlockSize = 128,
                Key = Encoding.UTF8.GetBytes(_secretKey), // 32 bytes
                IV = Encoding.UTF8.GetBytes(_initVector) // 16 bytes
            };
            byte[] encryptedData = Convert.FromBase64String(textToDecrypt);
            byte[] plainText = rijndaelCipher.CreateDecryptor().TransformFinalBlock(encryptedData, 0, encryptedData.Length);
            return Encoding.UTF8.GetString(plainText);
#endif
        }
    }
}
