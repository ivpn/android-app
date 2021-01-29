package net.ivpn.client.common.utils;

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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import net.ivpn.client.vpn.model.NetworkSource;

import static net.ivpn.client.vpn.model.NetworkSource.*;

public class NetworkUtil {

    public static NetworkSource getCurrentSource(Context context) {
        NetworkInfo networkInfo = getCurrentNetworkInfo(context);
        if (networkInfo == null) {
            return NO_NETWORK;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_DATA;
            } else {
                return NO_NETWORK;
            }
        } else {
            return UNDEFINED;
        }
    }

    public static String getCurrentWifiSsid(Context context) {
        WifiManager wifiManager = (WifiManager)
                context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = null;
        if (wifiManager != null) {
            wifi = wifiManager.getConnectionInfo();
        }
        if (wifi != null) {
            return wifi.getSSID();
        }

        return null;
    }

    private static NetworkInfo getCurrentNetworkInfo(Context context) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return conn.getActiveNetworkInfo();
    }
}
