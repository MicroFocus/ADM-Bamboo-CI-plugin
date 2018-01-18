package com.adm.bamboo.plugin.sv.model;

import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;

/**
 * Created with IntelliJ IDEA.
 * User: jingwei
 * Date: 12/7/17
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
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
