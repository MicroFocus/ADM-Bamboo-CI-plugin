/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.adm.bamboo.plugin.sv.model;

import org.apache.commons.lang.StringUtils;
import java.net.MalformedURLException;
import java.net.URL;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;

public class SvServerSettingsModel {
    private final String url;
    private final String username;
    private final String password;

    public SvServerSettingsModel(String url, String username, String password) {
        this.url = StringUtils.trim(url);
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public URL getUrlObject() throws MalformedURLException {
        return new URL(url);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Credentials getCredentials() {
        if (StringUtils.isBlank(username) || password == null) {
            return null;
        }
        return new Credentials(username, password);
    }
}
