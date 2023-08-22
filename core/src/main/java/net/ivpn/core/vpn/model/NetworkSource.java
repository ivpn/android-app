package net.ivpn.core.vpn.model;

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

import android.content.res.Resources;
import android.util.Log;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.common.utils.StringUtil;

public enum NetworkSource {
    WIFI,
    MOBILE_DATA,
    NO_NETWORK,
    UNDEFINED;

    private String ssid;
    private NetworkState state;
    private NetworkState defaultState;

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setState(NetworkState state) {
        this.state = state;
    }

    public void setDefaultState(NetworkState defaultState) {
        this.defaultState = defaultState;
    }

    public NetworkState getDefaultState() {
        return defaultState;
    }

    public String getSsid() {
        if (this.equals(MOBILE_DATA)) {
            return null;
        }
        return ssid;
    }

    public NetworkState getState() {
        return state;
    }

    public NetworkState getFinalState() {
        Log.d("NetworkSource", "getFinalState: state = " + state + " defaultState = " + defaultState);
        if (state == NetworkState.DEFAULT) {
            return defaultState;
        }

        return state;
    }

    public String getTitle() {
        if (this.equals(MOBILE_DATA)) {
            Resources resources = IVPNApplication.application.getResources();
            return resources.getString(R.string.network_mobile_data);
        } else if (this.equals(NO_NETWORK) || this.equals(UNDEFINED)) {
            Resources resources = IVPNApplication.application.getResources();
            return resources.getString(R.string.network_none);
        }
        return StringUtil.formatWifiSSID(ssid);
    }

    public int getIcon() {
        if (this.equals(MOBILE_DATA)) {
            return R.drawable.ic_network_cell;
        } else {
            return R.drawable.ic_wifi;
        }
    }
}
