package net.ivpn.core.common.prefs

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

import android.content.SharedPreferences
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import java.util.*
import javax.inject.Inject

@ApplicationScope
class EncryptedSettingsPreference @Inject constructor(val preference: Preference) {

    companion object {
        private const val IS_MIGRATED = "IS_MIGRATED"

        private const val SETTINGS_LOGGING = "SETTINGS_LOGGING"
        private const val SETTINGS_SENTRY = "SETTINGS_SENTRY"
        private const val SETTINGS_MULTI_HOP = "SETTINGS_MULTI_HOP"
        private const val SETTINGS_ADVANCED_KILL_SWITCH_DIALOG = "SETTINGS_ADVANCED_KILL_SWITCH_DIALOG"
        private const val SETTINGS_START_ON_BOOT = "SETTINGS_START_ON_BOOT"
        private const val SETTINGS_NETWORK_RULES = "SETTINGS_NETWORK_RULES"
        private const val SETTINGS_WG_PRIVATE_KEY = "SETTINGS_WG_PRIVATE_KEY"
        private const val SETTINGS_WG_PUBLIC_KEY = "SETTINGS_WG_PUBLIC_KEY"
        private const val SETTINGS_WG_IP_ADDRESS = "SETTINGS_WG_IP_ADDRESS"
        private const val SETTINGS_CUSTOM_DNS = "SETTINGS_CUSTOM_DNS"
        private const val SETTINGS_ANTI_SURVEILLANCE = "SETTINGS_ANTI_SURVEILLANCE"
        private const val SETTINGS_ANTI_SURVEILLANCE_HARDCORE = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE"
        private const val SETTINGS_ANTI_SURVEILLANCE_DNS = "SETTINGS_ANTI_SURVEILLANCE_DNS"
        private const val SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS"
        private const val SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI = "SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI"
        private const val SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI"
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
        private const val WIREGUARD_KEY_GENERATION_TIME = "WIREGUARD_KEY_GENERATION_TIME"
        private const val WIREGUARD_KEY_REGENERATION_PERIOD = "WIREGUARD_KEY_REGENERATION_PERIOD"
        private const val RULE_CONNECT_TO_VPN = "RULE_CONNECT_TO_VPN"
        private const val RULE_DISCONNECT_FROM_VPN = "RULE_DISCONNECT_FROM_VPN"
        private const val IP_LIST = "IP_LIST"
        private const val IPV6_LIST = "IPV6_LIST"
        private const val LAST_USED_IP = "LAST_USED_IP"
    }

    private val sharedPreferences: SharedPreferences = preference.settingsPreference

    var mockLocationSettings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_MOCK_LOCATION, false)
        }
        set(value) {
            sharedPreferences.edit()
                    .putBoolean(SETTINGS_MOCK_LOCATION, value)
                    .apply()
        }

    var bypassLocalSettings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_BYPASS_LOCAL, false)
        }
        set(value) {
            sharedPreferences.edit()
                    .putBoolean(SETTINGS_BYPASS_LOCAL, value)
                    .apply()
        }

    var ipv6Settings: Boolean
        get() {
            return sharedPreferences.getBoolean(SETTINGS_IPV6, false)
        }
        set(value) {
            sharedPreferences.edit()
                    .putBoolean(SETTINGS_IPV6, value)
                    .apply()
        }

    var ipv6List: String?
        get() {
            val value = sharedPreferences.getString(IPV6_LIST, null)
            return value
        }
        set(value) {
            sharedPreferences.edit()
                    .putString(IPV6_LIST, value)
                    .apply()
        }

    var ipv6ShowAllServers: Boolean
        get() {
            return sharedPreferences.getBoolean(IPV6_SHOW_ALL_SERVERS, true)
        }
        set(value) {
            sharedPreferences.edit()
                    .putBoolean(IPV6_SHOW_ALL_SERVERS, value)
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

    fun getAntiSurveillanceDns(): String? {
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_DNS, "10.0.254.2")
    }

    fun getAntiSurveillanceHardcoreDns(): String? {
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS, "10.0.254.3")
    }

    fun getAntiSurveillanceDnsMulti(): String? {
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI, "10.0.254.102")
    }

    fun getAntiSurveillanceHardcoreDnsMulti(): String? {
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI, "10.0.254.103")
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
        sharedPreferences.edit()
                .putLong(WIREGUARD_KEY_GENERATION_TIME, generationTime)
                .apply()
    }

    fun getRegenerationPeriod(): Int {
        return sharedPreferences.getInt(WIREGUARD_KEY_REGENERATION_PERIOD, 1)
    }

    fun putRegenerationPeriod(regenerationPeriod: Int) {
        sharedPreferences.edit()
                .putInt(WIREGUARD_KEY_REGENERATION_PERIOD, regenerationPeriod)
                .apply()
    }

    fun putSettingLogging(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_LOGGING, value)
                .apply()
    }

    fun putSettingMultiHop(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_MULTI_HOP, value)
                .apply()
    }

    fun putSettingCustomDNS(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_CUSTOM_DNS, value)
                .apply()
    }

    fun putSettingStartOnBoot(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_START_ON_BOOT, value)
                .apply()
    }

    fun putAntiSurveillance(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ANTI_SURVEILLANCE, value)
                .apply()
    }

    fun putAntiSurveillanceHardcore(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, value)
                .apply()
    }

    fun putAntiSurveillanceDns(value: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_DNS, value)
                .apply()
    }

    fun putAntiSurveillanceHardcoreDns(value: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS, value)
                .apply()
    }

    fun putAntiSurveillanceDnsMulti(value: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI, value)
                .apply()
    }

    fun putAntiSurveillanceHardcoreDnsMulti(value: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI, value)
                .apply()
    }

    fun putSettingAdvancedKillSwitch(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, value)
                .apply()
    }

    fun putAutoUpdateSetting(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_AUTO_UPDATE, value)
                .apply()
    }

    fun putSettingsNetworkRules(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_NETWORK_RULES, value)
                .apply()
    }

    fun setOpenvpnPort(json: String?) {
        sharedPreferences.edit()
                .putString(OV_PORT, json)
                .apply()
    }

    fun setCustomDNSValue(dns: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_CUSTOM_DNS_VALUE, dns)
                .apply()
    }

    fun getCustomDNSValue(): String? {
        return sharedPreferences.getString(SETTINGS_CUSTOM_DNS_VALUE, "")
    }

    fun getOpenvpnPort(): String? {
        return sharedPreferences.getString(OV_PORT, "")
    }

    fun setWgPort(json: String?) {
        sharedPreferences.edit()
                .putString(WG_PORT, json)
                .apply()
    }

    fun getWgPort(): String? {
        return sharedPreferences.getString(WG_PORT, "")
    }

    fun getSettingsWgPrivateKey(): String? {
        return sharedPreferences.getString(SETTINGS_WG_PRIVATE_KEY, "")
    }

    fun setSettingsWgPrivateKey(privateKey: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_WG_PRIVATE_KEY, privateKey)
                .apply()
    }

    fun getSettingsWgPublicKey(): String? {
        return sharedPreferences.getString(SETTINGS_WG_PUBLIC_KEY, "")
    }

    fun setSettingsWgPublicKey(publicKey: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_WG_PUBLIC_KEY, publicKey)
                .apply()
    }

    fun setSettingsWgIpAddress(ipAddress: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_WG_IP_ADDRESS, ipAddress)
                .apply()
    }

    fun getSettingsWgIpAddress(): String? {
        return sharedPreferences.getString(SETTINGS_WG_IP_ADDRESS, "")
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
        sharedPreferences.edit()
                .putBoolean(RULE_CONNECT_TO_VPN, value)
                .apply()
    }

    fun putRuleDisconnectFromVpn(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(RULE_DISCONNECT_FROM_VPN, value)
                .apply()
    }

    fun getNextVersion(): String? {
        return sharedPreferences.getString(SETTINGS_NEXT_VERSION, "{}")
    }

    fun setNextVersion(nextVersion: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_NEXT_VERSION, nextVersion)
                .apply()
    }

    fun putIpList(ips: String?) {
        sharedPreferences.edit()
                .putString(IP_LIST, ips)
                .apply()
    }

    fun getIpList(): LinkedList<String>? {
        return Mapper.ipListFrom(sharedPreferences.getString(IP_LIST, null))
    }

    fun putLastUsedIp(ip: String?) {
        sharedPreferences.edit()
                .putString(LAST_USED_IP, ip)
                .apply()
    }

    fun getLastUsedIp(): String? {
        return sharedPreferences.getString(LAST_USED_IP, null)
    }

    fun enableSentry(value: Boolean) {
        sharedPreferences.edit()
                .putBoolean(SETTINGS_SENTRY, value)
                .apply()
    }

    fun isSentryEnabled(): Boolean {
        return sharedPreferences.getBoolean(SETTINGS_SENTRY, true)
    }

    fun getFilter(): String? {
        return sharedPreferences.getString(SETTINGS_FILTER, null)
    }

    fun setFilter(filter: String?) {
        sharedPreferences.edit()
                .putString(SETTINGS_FILTER, filter)
                .apply()
    }

    private fun putIsMigrated(isMigrated: Boolean) {
        sharedPreferences.edit()
                .putBoolean(IS_MIGRATED, isMigrated)
                .apply()
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
        if (oldPreference.contains(SETTINGS_SENTRY)) {
            enableSentry(oldPreference.getBoolean(SETTINGS_SENTRY, true))
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
        if (oldPreference.contains(SETTINGS_CUSTOM_DNS)) {
            putSettingCustomDNS(oldPreference.getBoolean(SETTINGS_CUSTOM_DNS, false))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE)) {
            putAntiSurveillance(oldPreference.getBoolean(SETTINGS_ANTI_SURVEILLANCE, false))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_HARDCORE)) {
            putAntiSurveillanceHardcore(oldPreference.getBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, false))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_DNS)) {
            putAntiSurveillanceDns(oldPreference.getString(SETTINGS_ANTI_SURVEILLANCE_DNS, "10.0.254.2"))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS)) {
            putAntiSurveillanceHardcoreDns(oldPreference.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS, "10.0.254.3"))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI)) {
            putAntiSurveillanceDnsMulti(oldPreference.getString(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI, "10.0.254.102"))
        }
        if (oldPreference.contains(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI)) {
            putAntiSurveillanceHardcoreDnsMulti(oldPreference.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI, "10.0.254.103"))
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

        oldPreference.edit().clear().apply()

        putIsMigrated(true)
    }
}