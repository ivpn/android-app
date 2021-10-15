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

import android.util.Log
import com.wireguard.android.crypto.Keypair
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.BuildController
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.nightmode.NightMode
import net.ivpn.core.v2.protocol.port.Port
import net.ivpn.core.v2.serverlist.dialog.Filters
import net.ivpn.core.vpn.Protocol
import java.util.*
import javax.inject.Inject

@ApplicationScope
class Settings @Inject constructor(
        private val settingsPreference: EncryptedSettingsPreference,
        private val stickyPreference: StickyPreference,
        private val buildController: BuildController
) {

    companion object {
        private val TAG = Settings::class.java.simpleName
    }

    val dns: String?
        get() {
            val isAntiSurveillanceEnabled = isAntiSurveillanceEnabled
            val isAntiSurveillanceHardcoreEnabled = isAntiSurveillanceHardcoreEnabled
            val isMultiHopEnabled = isMultiHopEnabled
            val dns: String?
            val hardcoreDns: String?
            if (isMultiHopEnabled) {
                dns = antiTrackerDefaultDNSMulti
                hardcoreDns = antiTrackerHardcoreDNSMulti
            } else {
                dns = antiTrackerDefaultDNS
                hardcoreDns = antiTrackerHardcoreDNS
            }
            if (isAntiSurveillanceEnabled) {
                return if (isAntiSurveillanceHardcoreEnabled) {
                    hardcoreDns
                } else {
                    dns
                }
            }
            val isCustomDNSEnabled = isCustomDNSEnabled
            val customDNS = customDNSValue
            return if (isCustomDNSEnabled && customDNS != null && customDNS.isNotEmpty()) {
                customDNS
            } else null
        }

    var nightMode: NightMode?
        get() {
            val name = stickyPreference.nightMode
            if (name != null) {
                return NightMode.valueOf(name)
            }
            return if (buildController.isSystemDefaultNightModeSupported) {
                NightMode.SYSTEM_DEFAULT
            } else {
                NightMode.BY_BATTERY_SAVER
            }
        }
        set(mode) {
            stickyPreference.nightMode = mode?.name
        }

    var filter: Filters?
        get() {
            val name = settingsPreference.getFilter()
            return if (name != null) {
                Filters.valueOf(name)
            } else Filters.COUNTRY
        }
        set(filter) {
            settingsPreference.setFilter(filter?.name)
        }

    var isAutoUpdateEnabled: Boolean
        get() = settingsPreference.isAutoUpdateEnabled()
        set(value) {
            settingsPreference.putAutoUpdateSetting(value)
        }

    var nextVersion: String?
        get() = settingsPreference.getNextVersion()
        set(nextVersion) {
            settingsPreference.setNextVersion(nextVersion)
        }

    var isKillSwitchEnabled: Boolean
        get() = settingsPreference.killSwitch
        set(value) {
            settingsPreference.killSwitch = value
        }

    var isMultiHopEnabled: Boolean
        get() = settingsPreference.getSettingMultiHop()
        set(value) {
            settingsPreference.putSettingMultiHop(value)
        }

    var isNetworkRulesEnabled: Boolean
        get() = settingsPreference.getSettingNetworkRules()
        set(value) {
            settingsPreference.putSettingsNetworkRules(value)
        }

    var isCustomDNSEnabled: Boolean
        get() = settingsPreference.isCustomDNSEnabled()
        set(value) {
            settingsPreference.putSettingCustomDNS(value)
        }

    var isStartOnBootEnabled: Boolean
        get() = settingsPreference.getSettingStartOnBoot()
        set(value) {
            settingsPreference.putSettingStartOnBoot(value)
        }

    var isSentryEnabled: Boolean
        get() = settingsPreference.isSentryEnabled()
        set(value) {
            settingsPreference.enableSentry(value)
        }

    var isAntiSurveillanceEnabled: Boolean
        get() = settingsPreference.getIsAntiSurveillanceEnabled()
        set(value) {
            settingsPreference.putAntiSurveillance(value)
        }

    var isAntiSurveillanceHardcoreEnabled: Boolean
        get() = settingsPreference.getIsAntiSurveillanceHardcoreEnabled()
        set(value) {
            settingsPreference.putAntiSurveillanceHardcore(value)
        }

    var isAdvancedKillSwitchDialogEnabled: Boolean
        get() = settingsPreference.getIsAdvancedKillSwitchDialogEnabled()
        set(value) {
            settingsPreference.putSettingAdvancedKillSwitch(value)
        }

    val ipList: LinkedList<String>?
        get() = settingsPreference.getIpList()

    fun setIpList(ips: String?) {
        settingsPreference.putIpList(ips)
    }

    val ipv6List: LinkedList<String>
        get() = Mapper.ipListFrom(settingsPreference.ipv6List)

    fun setIPv6List(ips: String?) {
        settingsPreference.ipv6List = ips
    }

    var antiTrackerDefaultDNS: String?
        get() = settingsPreference.getAntiSurveillanceDns()
        set(dns) {
            settingsPreference.putAntiSurveillanceDns(dns)
        }

    var antiTrackerHardcoreDNS: String?
        get() = settingsPreference.getAntiSurveillanceHardcoreDns()
        set(dns) {
            settingsPreference.putAntiSurveillanceHardcoreDns(dns)
        }

    var antiTrackerDefaultDNSMulti: String?
        get() = settingsPreference.getAntiSurveillanceDnsMulti()
        set(dns) {
            settingsPreference.putAntiSurveillanceDnsMulti(dns)
        }

    var antiTrackerHardcoreDNSMulti: String?
        get() = settingsPreference.getAntiSurveillanceHardcoreDnsMulti()
        set(dns) {
            settingsPreference.putAntiSurveillanceHardcoreDnsMulti(dns)
        }

    var openVpnPort: Port
        get() {
            val portJson = settingsPreference.getOpenvpnPort()
            return if (portJson!!.isEmpty()) Port.UDP_2049 else Port.from(portJson)
        }
        set(port) {
            settingsPreference.setOpenvpnPort(port.toJson())
        }

    var wireGuardPort: Port
        get() {
            val portJson = settingsPreference.getWgPort()
            return if (portJson!!.isEmpty()) Port.WG_UDP_2049 else Port.from(portJson)
        }
        set(port) {
            settingsPreference.setWgPort(port.toJson())
        }

    var customDNSValue: String?
        get() = settingsPreference.getCustomDNSValue()
        set(dns) {
            settingsPreference.setCustomDNSValue(dns)
        }

    val isGenerationTimeExist: Boolean
        get() = settingsPreference.isGenerationTimeExist()

    var generationTime: Long
        get() = settingsPreference.getGenerationTime()
        set(value) {
            settingsPreference.putGenerationTime(value)
        }

    var lastUsedIp: String?
        get() = settingsPreference.getLastUsedIp()
        set(ip) {
            settingsPreference.putLastUsedIp(ip)
        }

    val wireGuardPublicKey: String?
        get() = settingsPreference.getSettingsWgPublicKey()

    val wireGuardPrivateKey: String?
        get() = settingsPreference.getSettingsWgPrivateKey()

    var wireGuardIpAddress: String?
        get() = settingsPreference.getSettingsWgIpAddress()
        set(ipAddress) {
            settingsPreference.setSettingsWgIpAddress(ipAddress)
        }

    fun nextPort() {
        val protocol = stickyPreference.currentProtocol
        if (protocol == Protocol.OPENVPN) {
            val nextPort = openVpnPort.next()
            Log.d(TAG, "nextPort: next port = ")
            openVpnPort = nextPort
        } else {
            val nextPort = wireGuardPort.next()
            Log.d(TAG, "nextPort: next port = ")
            wireGuardPort = nextPort
        }
    }

    var localBypass: Boolean
        get() = settingsPreference.bypassLocalSettings
        set(value) {
            settingsPreference.bypassLocalSettings = value
        }

    var ipv6Setting: Boolean
        get() = settingsPreference.ipv6Settings
        set(value) {
            settingsPreference.ipv6Settings = value
        }

    var showAllServersSetting: Boolean
        get() = settingsPreference.ipv6ShowAllServers
        set(value) {
            settingsPreference.ipv6ShowAllServers = value
        }

    var regenerationPeriod: Int
        get() = settingsPreference.getRegenerationPeriod()
        set(value) {
            settingsPreference.putRegenerationPeriod(value)
        }

    fun generateWireGuardKeys(): Keypair {
        return Keypair()
    }

    fun removeWireGuardKeys() {
        settingsPreference.setSettingsWgPrivateKey("")
        settingsPreference.setSettingsWgPublicKey("")
    }

    fun saveWireGuardKeypair(keypair: Keypair?) {
        settingsPreference.setSettingsWgPrivateKey(keypair?.privateKey)
        settingsPreference.setSettingsWgPublicKey(keypair?.publicKey)
        settingsPreference.putGenerationTime(System.currentTimeMillis())
        val alarm = IVPNApplication.appComponent.provideGlobalWireGuardAlarm()
        alarm.stop()
        alarm.start()
    }
}