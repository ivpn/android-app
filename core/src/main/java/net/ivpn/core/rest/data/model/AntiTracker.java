package net.ivpn.core.rest.data.model;

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

public class AntiTracker {

    @SerializedName("default")
    @Expose
    private Default _default;
    @SerializedName("hardcore")
    @Expose
    private Hardcore hardcore;

    public Default getDefault() {
        return _default;
    }

    public void setDefault(Default _default) {
        this._default = _default;
    }

    public Hardcore getHardcore() {
        return hardcore;
    }

    public void setHardcore(Hardcore hardcore) {
        this.hardcore = hardcore;
    }

    public class Hardcore {

        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("multihop-ip")
        @Expose
        private String multihopIp;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMultihopIp() {
            return multihopIp;
        }

        public void setMultihopIp(String multihopIp) {
            this.multihopIp = multihopIp;
        }

    }

    public class Default {

        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("multihop-ip")
        @Expose
        private String multihopIp;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMultihopIp() {
            return multihopIp;
        }

        public void setMultihopIp(String multihopIp) {
            this.multihopIp = multihopIp;
        }

    }
}