package net.ivpn.client.common.migration;

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

import android.util.Log;

import net.ivpn.client.common.prefs.EncryptedSettingsPreference;
import net.ivpn.client.common.prefs.NetworkProtectionPreference;

import java.util.Set;

public class UF0T1 {

    private static final String TAG = UF0T1.class.getSimpleName();

    private EncryptedSettingsPreference settingsPreference;
    private NetworkProtectionPreference networkPreference;

    UF0T1(EncryptedSettingsPreference settingsPreference, NetworkProtectionPreference networkPreference) {
        this.settingsPreference = settingsPreference;
        this.networkPreference = networkPreference;
    }

    public void update() {
        Log.d(TAG, "update: ");
        boolean isNetworkRulesExist = settingsPreference.isNetworkRuleSettingsExist();
        Log.d(TAG, "update: isNetworkRulesExist = " + isNetworkRulesExist);
        if (isNetworkRulesExist) {
            return;
        }

        Log.d(TAG, "update: preference.isDefaultBehaviourExist() = " + networkPreference.isDefaultBehaviourExist());
        Log.d(TAG, "update: preference.isMobileBehaviourExist() = " + networkPreference.isMobileBehaviourExist());
        if (networkPreference.isDefaultBehaviourExist() || networkPreference.isMobileBehaviourExist()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }

        Set<String> trustedSsid = networkPreference.getTrustedWifiList();
        Set<String> untrustedSsid = networkPreference.getUntrustedWifiList();
        Log.d(TAG, "update: trustedSsid = " + trustedSsid);
        if (trustedSsid != null) {
            Log.d(TAG, "update: trustedSsid.size() = " + trustedSsid.size());
        }

        Log.d(TAG, "update: untrustedSsid = " + untrustedSsid);
        if (untrustedSsid != null) {
            Log.d(TAG, "update: untrustedSsid.size() = " + untrustedSsid.size());
        }

        if (trustedSsid != null && !trustedSsid.isEmpty()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }
        if (untrustedSsid != null && !untrustedSsid.isEmpty()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }

        settingsPreference.putSettingsNetworkRules(false);
    }
}
