package net.ivpn.client.common.utils;

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
