package net.ivpn.core.rest.data.model;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class V2RayConfig {

    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("openvpn")
    @Expose
    private List<Port> openvpn;
    
    @SerializedName("wireguard")
    @Expose
    private List<Port> wireguard;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Port> getOpenvpn() {
        return openvpn;
    }

    public void setOpenvpn(List<Port> openvpn) {
        this.openvpn = openvpn;
    }

    public List<Port> getWireguard() {
        return wireguard;
    }

    public void setWireguard(List<Port> wireguard) {
        this.wireguard = wireguard;
    }

    @Override
    public String toString() {
        return "V2RayConfig{" +
                "id='" + id + '\'' +
                ", openvpn=" + openvpn +
                ", wireguard=" + wireguard +
                '}';
    }
} 