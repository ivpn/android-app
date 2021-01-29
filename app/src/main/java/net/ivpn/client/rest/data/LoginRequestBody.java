package net.ivpn.client.rest.data;

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

public class LoginRequestBody {

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("device_os")
    @Expose
    private String deviceOS;

    @SerializedName("device_type")
    @Expose
    private String deviceType;

    @SerializedName("wireguard_public_key")
    @Expose
    private String wgPublicKey;

    @SerializedName("wireguard_comment")
    @Expose
    private String wgComment;

    public LoginRequestBody(String username, String password, boolean isTablet, String wgPublicKey) {
        this.username = username;
        this.password = password;
        this.deviceType = isTablet ? "tablet" : "phone";
        this.deviceOS = "android";
        this.wgPublicKey = wgPublicKey;
        this.wgComment = wgPublicKey != null ? "IVPN Client for Android" : null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
