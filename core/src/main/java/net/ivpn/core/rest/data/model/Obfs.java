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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Obfs {

    @SerializedName("obfs3_multihop_port")
    @Expose
    private int obfs3MultihopPort;
    
    @SerializedName("obfs4_multihop_port")
    @Expose
    private int obfs4MultihopPort;
    
    @SerializedName("obfs4_key")
    @Expose
    private String obfs4Key;

    public int getObfs3MultihopPort() {
        return obfs3MultihopPort;
    }

    public void setObfs3MultihopPort(int obfs3MultihopPort) {
        this.obfs3MultihopPort = obfs3MultihopPort;
    }

    public int getObfs4MultihopPort() {
        return obfs4MultihopPort;
    }

    public void setObfs4MultihopPort(int obfs4MultihopPort) {
        this.obfs4MultihopPort = obfs4MultihopPort;
    }

    public String getObfs4Key() {
        return obfs4Key;
    }

    public void setObfs4Key(String obfs4Key) {
        this.obfs4Key = obfs4Key;
    }

    @Override
    public String toString() {
        return "Obfs{" +
                "obfs3MultihopPort=" + obfs3MultihopPort +
                ", obfs4MultihopPort=" + obfs4MultihopPort +
                ", obfs4Key='" + obfs4Key + '\'' +
                '}';
    }
} 