package net.ivpn.core.rest.data.model;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

public class Config {

    @SerializedName("antitracker")
    @Expose
    private AntiTrackerConfig antitracker;
    
    @SerializedName("antitracker_plus")
    @Expose
    private AntiTrackerPlus antitrackerPlus;
    @SerializedName("api")
    @Expose
    private Api api;
    @SerializedName("ports")
    @Expose
    private Ports ports;

    public AntiTrackerConfig getAntitracker() {
        return antitracker;
    }

    public void setAntitracker(AntiTrackerConfig antitracker) {
        this.antitracker = antitracker;
    }

    public AntiTrackerPlus getAntiTrackerPlus() {
        return antitrackerPlus;
    }

    public void setAntiTrackerPlus(AntiTrackerPlus antitrackerPlus) {
        this.antitrackerPlus = antitrackerPlus;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Ports getPorts() {
        return ports;
    }

    public void setPorts(Ports ports) {
        this.ports = ports;
    }

}