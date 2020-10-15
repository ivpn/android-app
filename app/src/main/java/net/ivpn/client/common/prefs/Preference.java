package net.ivpn.client.common.prefs;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;

import javax.inject.Inject;
import javax.inject.Singleton;

@ApplicationScope
public class Preference {

    public static final int LAST_LOGIC_VERSION = 1;

    private static final String CURRENT_LOGIC_VERSION = "CURRENT_LOGIC_VERSION";

    private static final String COMMON_PREF = "COMMON_PREF";
    private static final String TRUSTED_WIFI_PREF = "TRUSTED_WIFI_PREF";
    private static final String SETTINGS_PREF = "SETTINGS_PREF";
    private static final String SERVERS_PREF = "SERVERS_PREF";
    private static final String WIREGUARD_SERVERS_PREF = "WIREGUARD_SERVERS_PREF";
    private static final String FAVOURITES_SERVERS_PREF = "FAVOURITES_SERVERS_PREF";
    private static final String DISALLOWED_APPS_PREF = "DISALLOWED_APPS_PREF";
    private static final String ACCOUNT_PREF = "ACCOUNT_PREF";
    private static final String PURCHASE_PREF = "PURCHASE_PREF";
    //Don't clear this shared preference after logout
    private static final String STICKY_PREF = "STICKY_PREF";

    @Inject
    public Preference() {
    }

    public boolean isLogicVersionExist() {
        SharedPreferences sharedPreferences = getCommonSharedPreferences();
        return sharedPreferences.contains(CURRENT_LOGIC_VERSION);
    }

    public void setLogicVersion(int logicVersion) {
        SharedPreferences sharedPreferences = getCommonSharedPreferences();
        sharedPreferences.edit()
                .putInt(CURRENT_LOGIC_VERSION, logicVersion)
                .apply();
    }

    public int getLogicVersion() {
        SharedPreferences sharedPreferences = getCommonSharedPreferences();
        return sharedPreferences.getInt(CURRENT_LOGIC_VERSION, LAST_LOGIC_VERSION);
    }

    public void removeAll() {
        clear(getCommonSharedPreferences());
        clear(getSettingsSharedPreferences());
        clear(getServersSharedPreferences());
        clear(getFavouritesServersSharedPreferences());
        clear(getDisallowedAppsSharedPreferences());
        clear(getNetworkRulesSharedPreferences());
        clear(getAccountSharedPreferences());
        clear(getWireguardServersSharedPreferences());
    }

    private void clear(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) return;
        sharedPreferences.edit()
                .clear()
                .apply();
    }

    private SharedPreferences getCommonSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(COMMON_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getNetworkRulesSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(TRUSTED_WIFI_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getSettingsSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getServersSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(SERVERS_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getWireguardServersSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(WIREGUARD_SERVERS_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getFavouritesServersSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(FAVOURITES_SERVERS_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getPurchaseSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(PURCHASE_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getDisallowedAppsSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(DISALLOWED_APPS_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getAccountSharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(ACCOUNT_PREF, Context.MODE_PRIVATE);
    }

    SharedPreferences getStickySharedPreferences() {
        return IVPNApplication.getApplication().getSharedPreferences(STICKY_PREF, Context.MODE_PRIVATE);
    }
}