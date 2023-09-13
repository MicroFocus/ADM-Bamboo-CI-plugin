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

package com.adm.utils.uft;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Random;

public class Aes256Encryptor {
    private final String _secretKey;
    private final String _initVector;

    public Aes256Encryptor(final Pair<String, String> keyVectorPair)
    {
        if (keyVectorPair == null) {
            _secretKey = randomAlphanumeric(32);
            _initVector = randomAlphanumeric(16);
        } else {
            String secretKey = keyVectorPair.getFirst();
            String initVector = keyVectorPair.getSecond();
            if (StringUtils.isBlank(secretKey)) {
                _secretKey = randomAlphanumeric(32);
            } else {
                _secretKey = secretKey;
            }
            if (StringUtils.isBlank(initVector)) {
                _initVector = randomAlphanumeric(16);
            } else {
                _initVector = initVector;
            }
        }
    }

    public String Decrypt(String text) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(_secretKey.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(_initVector.getBytes("UTF-8"));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] results = cipher.doFinal(Base64.decodeBase64(text));

        return new String(results, "UTF-8");
    }

    public String Encrypt(final String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(_secretKey.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(_initVector.getBytes("UTF-8"));
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return Base64.encodeBase64String(results);
    }

    public String getSecretKey() {
        return _secretKey;
    }
    public String getInitVector() {
        return _initVector;
    }

    private String randomAlphanumeric(int count) {
        int end = 123;
        int start = 32;
        char[] buffer = new char[count];
        int gap = end - start;
        Random random = new SecureRandom();

        while(true) {
            while(true) {
                while(count-- != 0) {
                    char ch = (char)(random.nextInt(gap) + start);

                    if (Character.isLetter(ch) || Character.isDigit(ch)) {
                        if (ch >= '\udc00' && ch <= '\udfff') {
                            if (count == 0) {
                                ++count;
                            } else {
                                buffer[count] = ch;
                                --count;
                                buffer[count] = (char)('\ud800' + random.nextInt(128));
                            }
                        } else if (ch >= '\ud800' && ch <= '\udb7f') {
                            if (count == 0) {
                                ++count;
                            } else {
                                buffer[count] = (char)('\udc00' + random.nextInt(128));
                                --count;
                                buffer[count] = ch;
                            }
                        } else if (ch >= '\udb80' && ch <= '\udbff') {
                            ++count;
                        } else {
                            buffer[count] = ch;
                        }
                    } else {
                        ++count;
                    }
                }

                return new String(buffer);
            }
        }
    }
}
