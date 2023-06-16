package com.adm.utils.uft.model;

import com.adm.utils.uft.EncryptionUtils;

public class UftRunAsUser {
    private String username;
    private String encodedPassword;
    private String password;

    public UftRunAsUser(String username, String password, boolean isEncoded) {
        this.username = username;
        if (isEncoded) {
            this.encodedPassword = password;
        } else {
            this.password = password;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public String getPassword() { return password; }

    public String getPasswordAsEncrypted() throws Exception {
        return EncryptionUtils.Encrypt(password, EncryptionUtils.getSecretKey());
    }

    public String getEncodedPasswordAsEncrypted() throws Exception {
        return EncryptionUtils.Encrypt(encodedPassword, EncryptionUtils.getSecretKey());
    }
}