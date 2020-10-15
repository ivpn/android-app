package net.ivpn.client.rest.data;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.model.Config;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.vpn.Protocol;

import java.util.List;

public class ServersListResponse {
    @SerializedName("wireguard")
    @Expose
    private List<Server> wireGuard = null;
    @SerializedName("openvpn")
    @Expose
    private List<Server> openvpn = null;

    @SerializedName("config")
    @Expose
    private Config config = null;

    public List<Server> getWireGuardServerList() {
        return wireGuard;
    }

    public void setWireGuardServerList(List<Server> wireguard) {
        this.wireGuard = wireguard;
    }

    public List<Server> getOpenVpnServerList() {
        return openvpn;
    }

    public void setOpenVpnServerList(List<Server> openvpn) {
        this.openvpn = openvpn;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void markServerTypes() {
        for (Server server : wireGuard) {
            server.setType(Protocol.WIREGUARD);
        }
        for (Server server : openvpn) {
            server.setType(Protocol.OPENVPN);
        }
    }
}
