package net.ivpn.client.rest.data.session;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionNewRequestBody {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("wg_public_key")
    @Expose
    private String wgPublicKey;
    @SerializedName("app_name")
    @Expose
    private String appName;
    @SerializedName("force")
    @Expose
    private Boolean force;
    @SerializedName("confirmation")
    @Expose
    private String tfaToken;
    @SerializedName("captcha_id")
    @Expose
    private String captchaId;
    @SerializedName("captcha")
    @Expose
    private String captchaValue;

    public SessionNewRequestBody(String username, String wgPublicKey, Boolean force) {
        this.username = username;
        this.wgPublicKey = wgPublicKey;
        this.appName = "IVPN for Android";
        this.force = force;
    }

    public SessionNewRequestBody(String username, String wgPublicKey, Boolean force, String tfaToken) {
        this.username = username;
        this.wgPublicKey = wgPublicKey;
        this.appName = "IVPN for Android";
        this.force = force;
        this.tfaToken = tfaToken;
    }

    public SessionNewRequestBody(String username, String wgPublicKey, Boolean force, String captchaId, String captchaValue) {
        this.username = username;
        this.wgPublicKey = wgPublicKey;
        this.appName = "IVPN for Android";
        this.force = force;
        this.captchaId = captchaId;
        this.captchaValue = captchaValue;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return "SessionNewRequestBody{" +
                "username='" + username + '\'' +
                ", wgPublicKey='" + wgPublicKey + '\'' +
                ", appName='" + appName + '\'' +
                ", force=" + force +
                ", tfaToken='" + tfaToken + '\'' +
                ", captchaId='" + captchaId + '\'' +
                ", captchaValue='" + captchaValue + '\'' +
                '}';
    }
}