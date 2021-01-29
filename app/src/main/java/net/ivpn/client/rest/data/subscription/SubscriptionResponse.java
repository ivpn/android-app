package net.ivpn.client.rest.data.subscription;

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

import net.ivpn.client.rest.data.model.Data;
import net.ivpn.client.rest.data.model.ServiceStatus;
import net.ivpn.client.rest.data.model.WireGuard;

public class SubscriptionResponse {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("token")
    @Expose
    private String sessionToken;
    @SerializedName("vpn_username")
    @Expose
    private String vpnUsername;
    @SerializedName("vpn_password")
    @Expose
    private String vpnPassword;
    @SerializedName("service_status")
    @Expose
    private ServiceStatus serviceStatus;
    @SerializedName("wireguard")
    @Expose
    private WireGuard wireguard;
//    @SerializedName("data")
//    @Expose
//    private Data data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getVpnUsername() {
        return vpnUsername;
    }

    public void setVpnUsername(String vpnUsername) {
        this.vpnUsername = vpnUsername;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public WireGuard getWireguard() {
        return wireguard;
    }

    public void setWireguard(WireGuard wireguard) {
        this.wireguard = wireguard;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
//
//    public Data getData() {
//        return data;
//    }
//
//    public void setData(Data data) {
//        this.data = data;
//    }

    @Override
    public String toString() {
        return "SubscriptionResponse{" +
                "username='" + username + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", sessionToken='" + sessionToken + '\'' +
                ", vpnUsername='" + vpnUsername + '\'' +
                ", vpnPassword='" + vpnPassword + '\'' +
                ", serviceStatus=" + serviceStatus +
                ", wireguard=" + wireguard +
//                ", data=" + data +
                '}';
    }
}