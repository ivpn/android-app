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

import android.util.Log
import com.wireguard.android.crypto.Keypair
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.BuildController
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.nightmode.NightMode
import net.ivpn.core.common.v2ray.V2RaySettings
import net.ivpn.core.rest.data.model.AntiTracker
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.v2.serverlist.dialog.Filters
import net.ivpn.core.vpn.Protocol
import java.util.*
import javax.inject.Inject
import kotlin.collections.List

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
            var dns: String? = antiTrackerDefaultDNS
            var hardcoreDns: String? = antiTrackerHardcoreDNS
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

    var isMultiHopSameProviderAllowed: Boolean
        get() = settingsPreference.isMultiHopSameProviderAllowed
        set(value) {
            settingsPreference.isMultiHopSameProviderAllowed = value
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

    val antiTrackerDefaultDNS: String
        get() = antiTracker?.normal ?: "10.0.254.2"

    val antiTrackerHardcoreDNS: String
        get() = antiTracker?.hardcore ?: "10.0.254.3"

    var openVpnPort: Port
        get() {
            val portJson = settingsPreference.getOpenvpnPort()
            return if (portJson!!.isEmpty()) Port.defaultOvPort else Port.from(portJson)
        }
        set(port) {
            settingsPreference.setOpenvpnPort(port.toJson())
        }

    var openVpnPorts: List<Port>
        get() {
            val portsJson = settingsPreference.getOpenvpnPorts()
            return if (portsJson!!.isEmpty()) emptyList() else Mapper.portsFrom(portsJson)
        }
        set(ports) {
            settingsPreference.setOpenvpnPorts(Mapper.stringFromPorts(ports))
        }

    var openVpnCustomPorts: List<Port>
        get() {
            val portsJson = settingsPreference.getOpenvpnCustomPorts()
            return if (portsJson!!.isEmpty()) emptyList() else Mapper.portsFrom(portsJson)
        }
        set(ports) {
            settingsPreference.setOpenvpnCustomPorts(Mapper.stringFromPorts(ports))
        }

    var openVpnPortRanges: List<Port>
        get() {
            return Mapper.portsFrom(settingsPreference.getOpenvpnPortRanges())
        }
        set(ports) {
            settingsPreference.setOpenvpnPortRanges(Mapper.stringFromPorts(ports))
        }

    var wireGuardPort: Port
        get() {
            val portJson = settingsPreference.getWgPort()
            return if (portJson!!.isEmpty()) Port.defaultWgPort else Port.from(portJson)
        }
        set(port) {
            settingsPreference.setWgPort(port.toJson())
        }

    var wireGuardPorts: List<Port>
        get() {
            val portsJson = settingsPreference.getWgPorts()
            return if (portsJson!!.isEmpty()) emptyList() else Mapper.portsFrom(portsJson)
        }
        set(ports) {
            settingsPreference.setWgPorts(Mapper.stringFromPorts(ports))
        }

    var wireGuardCustomPorts: List<Port>
        get() {
            val portsJson = settingsPreference.getWgCustomPorts()
            return if (portsJson!!.isEmpty()) emptyList() else Mapper.portsFrom(portsJson)
        }
        set(ports) {
            settingsPreference.setWgCustomPorts(Mapper.stringFromPorts(ports))
        }

    var wireGuardPortRanges: List<Port>
        get() {
            return Mapper.portsFrom(settingsPreference.getWgPortRanges())
        }
        set(ports) {
            settingsPreference.setWgPortRanges(Mapper.stringFromPorts(ports))
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

    val wireGuardPresharedKey: String?
        get() = settingsPreference.getSettingsWgPresharedKey()

    var antiTrackerList: List<AntiTracker>
        get() {
            return Mapper.antiTrackerListFrom(settingsPreference.getAntiTrackerList())
        }
        set(list) {
            settingsPreference.setAntiTrackerList(Mapper.stringFromAntiTrackerList(list))
        }

    var antiTracker: AntiTracker?
        get() {
            val json = settingsPreference.getAntiTracker()
            return if (json!!.isEmpty()) null else Mapper.antiTrackerFrom(json)
        }
        set(dns) {
            settingsPreference.setAntiTracker(Mapper.stringFromAntiTracker(dns))
        }

    var v2ray: Boolean
        get() = settingsPreference.v2ray
        set(v2ray) {
            settingsPreference.v2ray = v2ray
        }

    var v2rayProtocol: String
        get() = settingsPreference.v2rayProtocol ?: "udp"
        set(v2rayProtocol) {
            settingsPreference.v2rayProtocol = v2rayProtocol
        }

    var v2raySettings: V2RaySettings?
        get() {
            val json = settingsPreference.getV2raySettings()
            return if (json!!.isEmpty()) null else Mapper.v2raySettingsFrom(json)
        }
        set(settings) {
            settingsPreference.setV2raySettings(Mapper.stringFromV2raySettings(settings))
        }

    fun nextPort() {
        val protocol = stickyPreference.currentProtocol
        if (protocol == Protocol.OPENVPN) {
            val nextPort = openVpnPort.next(openVpnPorts)
            Log.d(TAG, "nextPort: next port = ")
            openVpnPort = nextPort
        } else {
            val nextPort = wireGuardPort.next(wireGuardPorts)
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
        settingsPreference.setSettingsWgPresharedKey("")
    }

    fun saveWireGuardKeypair(keypair: Keypair?) {
        settingsPreference.setSettingsWgPrivateKey(keypair?.privateKey)
        settingsPreference.setSettingsWgPublicKey(keypair?.publicKey)
        settingsPreference.putGenerationTime(System.currentTimeMillis())
        val alarm = IVPNApplication.appComponent.provideGlobalWireGuardAlarm()
        alarm.stop()
        alarm.start()
    }

    fun saveWireGuardPresharedKey(key: String?) {
        settingsPreference.setSettingsWgPresharedKey(key)
    }
}