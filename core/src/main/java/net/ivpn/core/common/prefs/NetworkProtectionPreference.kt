package net.ivpn.core.common.prefs

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

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.vpn.model.NetworkState
import java.util.*
import javax.inject.Inject

@ApplicationScope
class NetworkProtectionPreference @Inject constructor(
        private val preference: Preference
) {
    companion object {
        private const val SETTINGS_TRUSTED_WIFI_LIST = "SETTINGS_TRUSTED_WIFI_LIST"
        private const val SETTINGS_UNTRUSTED_WIFI_LIST = "SETTINGS_UNTRUSTED_WIFI_LIST"
        private const val SETTINGS_NONE_WIFI_LIST = "SETTINGS_NONE_WIFI_LIST"
        private const val SETTINGS_DEFAULT_NETWORK_STATE = "SETTINGS_DEFAULT_NETWORK_STATE"
        private const val SETTINGS_MOBILE_DATE_NETWORK_STATE = "SETTINGS_MOBILE_DATE_NETWORK_STATE"
    }

    val trustedWifiList: Set<String>?
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            return sharedPreferences.getStringSet(SETTINGS_TRUSTED_WIFI_LIST, HashSet())
        }

    val untrustedWifiList: Set<String>?
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            return sharedPreferences.getStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, HashSet())
        }
    val noneWifiList: Set<String>?
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            return sharedPreferences.getStringSet(SETTINGS_NONE_WIFI_LIST, HashSet())
        }
    var defaultNetworkState: NetworkState
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            val defaultNetworkStateName = sharedPreferences.getString(SETTINGS_DEFAULT_NETWORK_STATE,
                    NetworkState.NONE.name)
            return NetworkState.valueOf(defaultNetworkStateName!!)
        }
        set(defaultNetworkState) {
            val sharedPreferences = preference.networkRulesSharedPreferences
            sharedPreferences.edit()
                    .putString(SETTINGS_DEFAULT_NETWORK_STATE, defaultNetworkState.name)
                    .apply()
        }
    var mobileDataNetworkState: NetworkState
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            val mobileDataNetworkStateName = sharedPreferences.getString(SETTINGS_MOBILE_DATE_NETWORK_STATE,
                    NetworkState.DEFAULT.name)
            return NetworkState.valueOf(mobileDataNetworkStateName!!)
        }
        set(mobileDataNetworkState) {
            val sharedPreferences = preference.networkRulesSharedPreferences
            sharedPreferences.edit()
                    .putString(SETTINGS_MOBILE_DATE_NETWORK_STATE, mobileDataNetworkState.name)
                    .apply()
        }
    val isDefaultBehaviourExist: Boolean
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            return sharedPreferences.contains(SETTINGS_DEFAULT_NETWORK_STATE)
        }
    val isMobileBehaviourExist: Boolean
        get() {
            val sharedPreferences = preference.networkRulesSharedPreferences
            return sharedPreferences.contains(SETTINGS_MOBILE_DATE_NETWORK_STATE)
        }

    fun markWifiAsTrusted(wifiSsid: String?) {
        val trustedWifi = trustedWifiList
        if (wifiSsid == null || trustedWifi == null || trustedWifi.contains(wifiSsid)) {
            return
        }
        val newTrustedWifiList: MutableSet<String> = HashSet(trustedWifi)
        newTrustedWifiList.add(wifiSsid)
        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_TRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply()
    }

    fun removeMarkWifiAsTrusted(wifiSsid: String?) {
        val trustedWifi = trustedWifiList
        if (wifiSsid == null || trustedWifi == null || !trustedWifi.contains(wifiSsid)) {
            return
        }

        val newTrustedWifiList: MutableSet<String> = HashSet(trustedWifi)
        newTrustedWifiList.remove(wifiSsid)
        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_TRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply()
    }

    fun markWifiAsUntrusted(wifiSsid: String?) {
        val untrustedWifi = untrustedWifiList
        if (wifiSsid == null || untrustedWifi == null || untrustedWifi.contains(wifiSsid)) {
            return
        }

        val newTrustedWifiList: MutableSet<String> = HashSet(untrustedWifi)
        newTrustedWifiList.add(wifiSsid)

        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply()
    }

    fun removeMarkWifiAsUntrusted(wifiSsid: String?) {
        val untrustedWifi = untrustedWifiList
        if (wifiSsid == null || untrustedWifi == null || !untrustedWifi.contains(wifiSsid)) {
            return
        }

        val newTrustedWifiList: MutableSet<String> = HashSet(untrustedWifi)
        newTrustedWifiList.remove(wifiSsid)

        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply()
    }

    fun markWifiAsNone(wifiSsid: String?) {
        val noneWiFis = noneWifiList
        if (wifiSsid == null || noneWiFis == null || noneWiFis.contains(wifiSsid)) {
            return
        }

        val newTrustedWifiList: MutableSet<String> = HashSet(noneWiFis)
        newTrustedWifiList.add(wifiSsid)

        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_NONE_WIFI_LIST, newTrustedWifiList)
                .apply()
    }

    fun removeMarkWifiAsNone(wifiSsid: String?) {
        val noneWiFis = noneWifiList
        if (wifiSsid == null || noneWiFis == null || !noneWiFis.contains(wifiSsid)) {
            return
        }

        val newTrustedWifiList: MutableSet<String> = HashSet(noneWiFis)
        newTrustedWifiList.remove(wifiSsid)

        val sharedPreferences = preference.networkRulesSharedPreferences
        sharedPreferences.edit()
                .putStringSet(SETTINGS_NONE_WIFI_LIST, newTrustedWifiList)
                .apply()
    }
}