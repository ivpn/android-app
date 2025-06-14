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

import android.content.SharedPreferences
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.vpn.model.ObfuscationType
import java.util.*
import javax.inject.Inject
import androidx.core.content.edit

@ApplicationScope
class EncryptedSettingsPreference @Inject constructor(val preference: Preference) {

    companion object {
        private const val IS_MIGRATED = "IS_MIGRATED"

        private const val SETTINGS_LOGGING = "SETTINGS_LOGGING"
        private const val SETTINGS_MULTI_HOP = "SETTINGS_MULTI_HOP"
        private const val SETTINGS_MULTI_HOP_SAME_PROVIDER_ALLOWED = "SETTINGS_MULTI_HOP_SAME_PROVIDER_ALLOWED"
        private const val SETTINGS_KILL_SWITCH = "SETTINGS_KILL_SWITCH"
        private const val SETTINGS_ADVANCED_KILL_SWITCH_DIALOG = "SETTINGS_ADVANCED_KILL_SWITCH_DIALOG"
        private const val SETTINGS_START_ON_BOOT = "SETTINGS_START_ON_BOOT"
        private const val SETTINGS_OBFUSCATION_TYPE = "SETTINGS_OBFUSCATION_TYPE"
        private const val SETTINGS_NETWORK_RULES = "SETTINGS_NETWORK_RULES"
        private const val SETTINGS_WG_PRIVATE_KEY = "SETTINGS_WG_PRIVATE_KEY"
        private const val SETTINGS_WG_PUBLIC_KEY = "SETTINGS_WG_PUBLIC_KEY"
        private const val SETTINGS_WG_IP_ADDRESS = "SETTINGS_WG_IP_ADDRESS"
        private const val SETTINGS_WG_PRESHARED_KEY = "SETTINGS_WG_PRESHARED_KEY"
        private const val SETTINGS_CUSTOM_DNS = "SETTINGS_CUSTOM_DNS"
        private const val SETTINGS_ANTI_SURVEILLANCE = "SETTINGS_ANTI_SURVEILLANCE"
        private const val SETTINGS_ANTI_SURVEILLANCE_HARDCORE = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE"
        private const val SETTINGS_CUSTOM_DNS_VALUE = "SETTINGS_CUSTOM_DNS_VALUE"
        private const val SETTINGS_AUTO_UPDATE = "SETTINGS_AUTO_UPDATE"
        private const val SETTINGS_NEXT_VERSION = "SETTINGS_NEXT_VERSION"
        private const val SETTINGS_FILTER = "SETTINGS_FILTER"
        private const val SETTINGS_MOCK_LOCATION = "SETTINGS_MOCK_LOCATION"
        private const val SETTINGS_BYPASS_LOCAL = "SETTINGS_BYPASS_LOCAL"
        private const val SETTINGS_IPV6 = "SETTINGS_IPV6"
        private const val IPV6_SHOW_ALL_SERVERS = "IPV6_SHOW_ALL_SERVERS"

        private const val OV_PORT = "OV_PORT"
        private const val WG_PORT = "WG_PORT"
        private const val WG_PORT_LIST = "WG_PORT_LIST"
        private const val WG_CUSTOM_PORT_LIST = "WG_CUSTOM_PORT_LIST"
        private const val WG_PORT_RANGE_LIST = "WG_PORT_RANGE_LIST"
        private const val OV_PORT_LIST = "OV_PORT_LIST"
        private const val OV_CUSTOM_PORT_LIST = "OV_CUSTOM_PORT_LIST"
        private const val OV_PORT_RANGE_LIST = "OV_PORT_RANGE_LIST"
        private const val WIREGUARD_KEY_GENERATION_TIME = "WIREGUARD_KEY_GENERATION_TIME"
        private const val WIREGUARD_KEY_REGENERATION_PERIOD = "WIREGUARD_KEY_REGENERATION_PERIOD"
        private const val RULE_CONNECT_TO_VPN = "RULE_CONNECT_TO_VPN"
        private const val RULE_DISCONNECT_FROM_VPN = "RULE_DISCONNECT_FROM_VPN"
        private const val IP_LIST = "IP_LIST"
        private const val IPV6_LIST = "IPV6_LIST"
        private const val LAST_USED_IP = "LAST_USED_IP"
        private const val ANTITRACKER_LIST = "ANTITRACKER_LIST"
        private const val ANTITRACKER_DNS = "ANTITRACKER_DNS"
    }

    private val sharedPreferences: SharedPreferences = preference.settingsPreference

    var obfuscationType: ObfuscationType
        get() {
            val typeName = sharedPreferences.getString(SETTINGS_OBFUSCATION_TYPE, ObfuscationType.DISABLED.name)
            return ObfuscationType.valueOf(typeName!!)
        }
        set(value) {
            sharedPreferences.edit {
                putString(SETTINGS_OBFUSCATION_TYPE, value.name)
            }
        }

    var mockLocationSettings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_MOCK_LOCATION, false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(SETTINGS_MOCK_LOCATION, value)
            }
        }

    var bypassLocalSettings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_BYPASS_LOCAL, false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(SETTINGS_BYPASS_LOCAL, value)
            }
        }

    var ipv6Settings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_IPV6, false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(SETTINGS_IPV6, value)
            }
        }

    var killSwitch: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_KILL_SWITCH, false)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(SETTINGS_KILL_SWITCH, value)
            }
        }

    var ipv6List: String?
        get() {
            val value = sharedPreferences.getString(IPV6_LIST, null)
            return value
        }
        set(value) {
            sharedPreferences.edit {
                putString(IPV6_LIST, value)
            }
        }

    var ipv6ShowAllServers: Boolean
        get() {
            return sharedPreferences.getBoolean(IPV6_SHOW_ALL_SERVERS, true)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(IPV6_SHOW_ALL_SERVERS, value)
            }
        }

    var isMultiHopSameProviderAllowed: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_MULTI_HOP_SAME_PROVIDER_ALLOWED, true)
        }
        set(value) {
            sharedPreferences.edit()
                .putBoolean(SETTINGS_MULTI_HOP_SAME_PROVIDER_ALLOWED, value)
                .apply()
        }

    init {
        migrate()
    }

    fun getSettingLogging(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_LOGGING, false)
    }

    fun getSettingMultiHop(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_MULTI_HOP, false)
    }

    fun getSettingStartOnBoot(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_START_ON_BOOT, false)
    }

    fun getIsAntiSurveillanceEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_ANTI_SURVEILLANCE, false)
    }

    fun getIsAntiSurveillanceHardcoreEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, false)
    }

    fun getIsAdvancedKillSwitchDialogEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, true)
    }

    fun getSettingNetworkRules(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_NETWORK_RULES, false)
    }

    fun isCustomDNSEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_CUSTOM_DNS, false)
    }

    fun isNetworkRuleSettingsExist(): Boolean {
        return sharedPreferences.contains(SETTINGS_NETWORK_RULES)
    }

    fun isGenerationTimeExist(): Boolean {
        return sharedPreferences.contains(WIREGUARD_KEY_GENERATION_TIME)
    }

    fun getGenerationTime(): Long {
        return sharedPreferences.getLong(WIREGUARD_KEY_GENERATION_TIME, 0)
    }

    fun putGenerationTime(generationTime: Long) {
        sharedPreferences.edit {
            putLong(WIREGUARD_KEY_GENERATION_TIME, generationTime)
        }
    }

    fun getRegenerationPeriod(): Int {
        return sharedPreferences.getInt(WIREGUARD_KEY_REGENERATION_PERIOD, 1)
    }

    fun putRegenerationPeriod(regenerationPeriod: Int) {
        sharedPreferences.edit {
            putInt(WIREGUARD_KEY_REGENERATION_PERIOD, regenerationPeriod)
        }
    }

    fun putSettingLogging(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_LOGGING, value)
        }
    }

    fun putSettingMultiHop(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_MULTI_HOP, value)
        }
    }

    fun putSettingCustomDNS(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_CUSTOM_DNS, value)
        }
    }

    fun putSettingStartOnBoot(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_START_ON_BOOT, value)
        }
    }

    fun putAntiSurveillance(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_ANTI_SURVEILLANCE, value)
        }
    }

    fun putAntiSurveillanceHardcore(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, value)
        }
    }

    fun putSettingAdvancedKillSwitch(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, value)
        }
    }

    fun putAutoUpdateSetting(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_AUTO_UPDATE, value)
        }
    }

    fun putSettingsNetworkRules(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SETTINGS_NETWORK_RULES, value)
        }
    }

    fun setOpenvpnPort(json: String?) {
        sharedPreferences.edit {
            putString(OV_PORT, json)
        }
    }

    fun setCustomDNSValue(dns: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_CUSTOM_DNS_VALUE, dns)
        }
    }

    fun getCustomDNSValue(): String? {
        return sharedPreferences.getString(SETTINGS_CUSTOM_DNS_VALUE, "")
    }

    fun getOpenvpnPort(): String? {
        return sharedPreferences.getString(OV_PORT, "")
    }

    fun setWgPort(json: String?) {
        sharedPreferences.edit {
            putString(WG_PORT, json)
        }
    }

    fun getWgPort(): String? {
        return sharedPreferences.getString(WG_PORT, "")
    }

    fun setOpenvpnPorts(json: String?) {
        sharedPreferences.edit {
            putString(OV_PORT_LIST, json)
        }
    }

    fun getOpenvpnPorts(): String? {
        return sharedPreferences.getString(OV_PORT_LIST, "")
    }

    fun setOpenvpnCustomPorts(json: String?) {
        sharedPreferences.edit {
            putString(OV_CUSTOM_PORT_LIST, json)
        }
    }

    fun getOpenvpnCustomPorts(): String? {
        return sharedPreferences.getString(OV_CUSTOM_PORT_LIST, "")
    }

    fun setOpenvpnPortRanges(json: String?) {
        sharedPreferences.edit {
            putString(OV_PORT_RANGE_LIST, json)
        }
    }

    fun getOpenvpnPortRanges(): String? {
        return sharedPreferences.getString(OV_PORT_RANGE_LIST, "")
    }

    fun setWgPorts(json: String?) {
        sharedPreferences.edit {
            putString(WG_PORT_LIST, json)
        }
    }

    fun getWgPorts(): String? {
        return sharedPreferences.getString(WG_PORT_LIST, "")
    }

    fun setWgCustomPorts(json: String?) {
        sharedPreferences.edit {
            putString(WG_CUSTOM_PORT_LIST, json)
        }
    }

    fun getWgCustomPorts(): String? {
        return sharedPreferences.getString(WG_CUSTOM_PORT_LIST, "")
    }

    fun setWgPortRanges(json: String?) {
        sharedPreferences.edit {
            putString(WG_PORT_RANGE_LIST, json)
        }
    }

    fun getWgPortRanges(): String? {
        return sharedPreferences.getString(WG_PORT_RANGE_LIST, "")
    }

    fun getSettingsWgPrivateKey(): String? {
        return sharedPreferences.getString(SETTINGS_WG_PRIVATE_KEY, "")
    }

    fun setSettingsWgPrivateKey(privateKey: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_WG_PRIVATE_KEY, privateKey)
        }
    }

    fun getSettingsWgPublicKey(): String? {
        return sharedPreferences.getString(SETTINGS_WG_PUBLIC_KEY, "")
    }

    fun setSettingsWgPublicKey(publicKey: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_WG_PUBLIC_KEY, publicKey)
        }
    }

    fun setSettingsWgIpAddress(ipAddress: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_WG_IP_ADDRESS, ipAddress)
        }
    }

    fun getSettingsWgIpAddress(): String? {
        return sharedPreferences.getString(SETTINGS_WG_IP_ADDRESS, "")
    }

    fun getSettingsWgPresharedKey(): String? {
        return sharedPreferences.getString(SETTINGS_WG_PRESHARED_KEY, "")
    }

    fun setSettingsWgPresharedKey(publicKey: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_WG_PRESHARED_KEY, publicKey)
        }
    }

    fun isAutoUpdateEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_AUTO_UPDATE, true)
    }

    fun getRuleConnectToVpn(): Boolean {
        return sharedPreferences.getBoolean(RULE_CONNECT_TO_VPN, true)
    }

    fun getRuleDisconnectFromVpn(): Boolean {
        return sharedPreferences.getBoolean(RULE_DISCONNECT_FROM_VPN, true)
    }

    fun putRuleConnectToVpn(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(RULE_CONNECT_TO_VPN, value)
        }
    }

    fun putRuleDisconnectFromVpn(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(RULE_DISCONNECT_FROM_VPN, value)
        }
    }

    fun getNextVersion(): String? {
        return sharedPreferences.getString(SETTINGS_NEXT_VERSION, "{}")
    }

    fun setNextVersion(nextVersion: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_NEXT_VERSION, nextVersion)
        }
    }

    fun putIpList(ips: String?) {
        sharedPreferences.edit {
            putString(IP_LIST, ips)
        }
    }

    fun getIpList(): LinkedList<String>? {
        return Mapper.ipListFrom(sharedPreferences.getString(IP_LIST, null))
    }

    fun putLastUsedIp(ip: String?) {
        sharedPreferences.edit {
            putString(LAST_USED_IP, ip)
        }
    }

    fun getLastUsedIp(): String? {
        return sharedPreferences.getString(LAST_USED_IP, null)
    }

    fun getFilter(): String? {
        return sharedPreferences.getString(SETTINGS_FILTER, null)
    }

    fun setFilter(filter: String?) {
        sharedPreferences.edit {
            putString(SETTINGS_FILTER, filter)
        }
    }

    fun setAntiTrackerList(json: String?) {
        sharedPreferences.edit {
            putString(ANTITRACKER_LIST, json)
        }
    }

    fun getAntiTrackerList(): String? {
        return sharedPreferences.getString(ANTITRACKER_LIST, "")
    }

    fun setAntiTracker(json: String?) {
        sharedPreferences.edit {
            putString(ANTITRACKER_DNS, json)
        }
    }

    fun getAntiTracker(): String? {
        return sharedPreferences.getString(ANTITRACKER_DNS, "")
    }


    private fun putIsMigrated(isMigrated: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_MIGRATED, isMigrated)
        }
    }

    private fun isMigrated(): Boolean {
        return sharedPreferences.getBoolean(IS_MIGRATED, false)
    }

    private fun migrate() {
        if (isMigrated()) return

        val oldPreference = preference.oldSettingsPreference

        if (oldPreference.all.isEmpty()) {
            putIsMigrated(true)
            return
        }

        if (oldPreference.contains(SETTINGS_LOGGING)) {
            putSettingLogging(oldPreference.getBoolean(SETTINGS_LOGGING, false))
        }
        if (oldPreference.contains(SETTINGS_MULTI_HOP)) {
            putSettingMultiHop(oldPreference.getBoolean(SETTINGS_MULTI_HOP, false))
        }
        if (oldPreference.contains(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG)) {
            putSettingAdvancedKillSwitch(oldPreference.getBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, true))
        }
        if (oldPreference.contains(SETTINGS_START_ON_BOOT)) {
            putSettingStartOnBoot(oldPreference.getBoolean(SETTINGS_START_ON_BOOT, false))
        }
        if (oldPreference.contains(SETTINGS_NETWORK_RULES)) {
            putSettingsNetworkRules(oldPreference.getBoolean(SETTINGS_NETWORK_RULES, false))
        }
        if (oldPreference.contains(SETTINGS_WG_PRIVATE_KEY)) {
            setSettingsWgPrivateKey(oldPreference.getString(SETTINGS_WG_PRIVATE_KEY, ""))
        }
        if (oldPreference.contains(SETTINGS_WG_PUBLIC_KEY)) {
            setSettingsWgPublicKey(oldPreference.getString(SETTINGS_WG_PUBLIC_KEY, ""))
        }
        if (oldPreference.contains(SETTINGS_WG_IP_ADDRESS)) {
            setSettingsWgIpAddress(oldPreference.getString(SETTINGS_WG_IP_ADDRESS, ""))
        }
        if (oldPreference.contains(SETTINGS_WG_PRESHARED_KEY)) {
            setSettingsWgPrivateKey(oldPreference.getString(SETTINGS_WG_PRESHARED_KEY, ""))
        }
        if (oldPreference.contains(SETTINGS_CUSTOM_DNS)) {
            putSettingCustomDNS(oldPreference.getBoolean(SETTINGS_CUSTOM_DNS, false))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE)) {
            putAntiSurveillance(oldPreference.getBoolean(SETTINGS_ANTI_SURVEILLANCE, false))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_HARDCORE)) {
            putAntiSurveillanceHardcore(oldPreference.getBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, false))
        }
        if (oldPreference.contains(SETTINGS_CUSTOM_DNS_VALUE)) {
            setCustomDNSValue(oldPreference.getString(SETTINGS_CUSTOM_DNS_VALUE, ""))
        }
        if (oldPreference.contains(SETTINGS_AUTO_UPDATE)) {
            putAutoUpdateSetting(oldPreference.getBoolean(SETTINGS_AUTO_UPDATE, true))
        }
        if (oldPreference.contains(SETTINGS_NEXT_VERSION)) {
            setNextVersion(oldPreference.getString(SETTINGS_NEXT_VERSION, "{}"))
        }
        if (oldPreference.contains(SETTINGS_FILTER)) {
            setFilter(oldPreference.getString(SETTINGS_FILTER, null))
        }
        if (oldPreference.contains(OV_PORT)) {
            setOpenvpnPort(oldPreference.getString(OV_PORT, ""))
        }
        if (oldPreference.contains(WG_PORT)) {
            setWgPort(oldPreference.getString(WG_PORT, ""))
        }
        if (oldPreference.contains(WIREGUARD_KEY_GENERATION_TIME)) {
            putGenerationTime(oldPreference.getLong(WIREGUARD_KEY_GENERATION_TIME, 0L))
        }
        if (oldPreference.contains(WIREGUARD_KEY_REGENERATION_PERIOD)) {
            putRegenerationPeriod(oldPreference.getInt(WIREGUARD_KEY_REGENERATION_PERIOD, 1))
        }
        if (oldPreference.contains(RULE_CONNECT_TO_VPN)) {
            putRuleConnectToVpn(oldPreference.getBoolean(RULE_CONNECT_TO_VPN, true))
        }
        if (oldPreference.contains(RULE_DISCONNECT_FROM_VPN)) {
            putRuleDisconnectFromVpn(oldPreference.getBoolean(RULE_DISCONNECT_FROM_VPN, true))
        }
        if (oldPreference.contains(IP_LIST)) {
            putIpList(oldPreference.getString(IP_LIST, null))
        }
        if (oldPreference.contains(LAST_USED_IP)) {
            putLastUsedIp(oldPreference.getString(LAST_USED_IP, null))
        }


        oldPreference.edit { clear() }

        putIsMigrated(true)
    }
}