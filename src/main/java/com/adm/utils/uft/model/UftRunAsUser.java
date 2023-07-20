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

package com.adm.utils.uft.model;

import com.adm.utils.uft.Aes256Encryptor;
import org.jetbrains.annotations.NotNull;

public class UftRunAsUser {
    private final Aes256Encryptor aes256Encryptor;
    private String username;
    private String encodedPassword;
    private String password;

    public UftRunAsUser(String username, String password, boolean isEncoded, @NotNull final Aes256Encryptor aes256Encryptor) {
        this.username = username;
        if (isEncoded) {
            this.encodedPassword = password;
        } else {
            this.password = password;
        }
        this.aes256Encryptor = aes256Encryptor;
    }

    public String getUsername() {
        return username;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public String getPassword() { return password; }

    public String getPasswordAsEncrypted() throws Exception {
        return aes256Encryptor.Encrypt(password);
    }

    public String getEncodedPasswordAsEncrypted() throws Exception {
        return aes256Encryptor.Encrypt(encodedPassword);
    }
}