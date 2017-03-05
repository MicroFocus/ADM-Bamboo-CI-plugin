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

package com.hp.octane.plugins.bamboo.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by lazara on 28/12/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OctaneConnectionDTO {
    private String octaneUrl;
    private String accessKey;
    private String apiSecret;
    private String userName;
    public String getUserName() { return userName;}

    public void setUserName(String userName) { this.userName = userName;}

    public String getOctaneUrl() {
        return octaneUrl;
    }

    public void setOctaneUrl(String octaneUrl) {
        this.octaneUrl = octaneUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
}