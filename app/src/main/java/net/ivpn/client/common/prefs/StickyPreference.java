package net.ivpn.client.common.prefs;

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

import android.content.SharedPreferences;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.vpn.Protocol;

import javax.inject.Inject;

@ApplicationScope
public class StickyPreference {

    private static final String CURRENT_PROTOCOL = "CURRENT_PROTOCOL";
    private static final String SETTINGS_NIGHT_MODE = "NIGHT_MODE";

    private Preference preference;

    @Inject
    public StickyPreference(Preference preference) {
        this.preference = preference;
    }

    public String getCurrentProtocol() {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        return sharedPreferences.getString(CURRENT_PROTOCOL, Protocol.WIREGUARD.name());
    }

    public void putCurrentProtocol(Protocol protocol) {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        sharedPreferences.edit()
                .putString(CURRENT_PROTOCOL, protocol.name())
                .apply();
    }

    public boolean isProtocolSelected() {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        return sharedPreferences.contains(CURRENT_PROTOCOL);
    }

    public String getNightMode() {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        return sharedPreferences.getString(SETTINGS_NIGHT_MODE, null);
    }

    public void setNightMode(String mode) {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_NIGHT_MODE, mode)
                .apply();
    }
}