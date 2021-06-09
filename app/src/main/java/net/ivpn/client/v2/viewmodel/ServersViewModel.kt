package net.ivpn.client.v2.viewmodel

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

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.multihop.MultiHopController
import net.ivpn.client.common.pinger.OnPingFinishListener
import net.ivpn.client.common.pinger.PingProvider
import net.ivpn.client.common.pinger.PingResultFormatter
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.connect.createSession.ConnectionState
import net.ivpn.client.vpn.controller.DefaultVPNStateListener
import net.ivpn.client.vpn.controller.VpnBehaviorController
import net.ivpn.client.vpn.controller.VpnStateListener
import javax.inject.Inject

@ApplicationScope
class ServersViewModel @Inject constructor(
        val serversRepository: ServersRepository,
        private val multiHopController: MultiHopController,
        val settings: Settings,
        val vpnBehaviorController: VpnBehaviorController,
        val pingProvider: PingProvider
) : ViewModel() {

    val entryRandomServer = ObservableBoolean()
    val exitRandomServer = ObservableBoolean()
    val fastestServer = ObservableBoolean()
    val entryServerVisibility = ObservableBoolean()
    val exitServerVisibility = ObservableBoolean()

    val entryServer = ObservableField<Server>()
    val exitServer = ObservableField<Server>()
    val mapServer = ObservableField<Server>()
    val pingResultExitServer = ObservableField<PingResultFormatter>()
    val pingResultEnterServer = ObservableField<PingResultFormatter>()

    init {
        multiHopController.addListener(getOnMultihopValueChanges())
        vpnBehaviorController.addVpnStateListener(getVPNStateListener())
    }

    fun onResume() {
        initStates()
    }

    fun reset() {
        initStates()
    }

    private fun initStates() {
        entryServer.set(getCurrentServer(ServerType.ENTRY))
        exitServer.set(getCurrentServer(ServerType.EXIT))
        mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())

        fastestServer.set(isFastestServerEnabled())
        entryRandomServer.set(getSettingsRandomServer(ServerType.ENTRY))
        exitRandomServer.set(getSettingsRandomServer(ServerType.EXIT))

        entryServerVisibility.set(!fastestServer.get() && !entryRandomServer.get())
        exitServerVisibility.set(!exitRandomServer.get())

        pingResultExitServer.set(null)
        pingResultEnterServer.set(null)

        ping(entryServer.get(), getPingFinishListener(ServerType.ENTRY))
        ping(exitServer.get(), getPingFinishListener(ServerType.EXIT))
    }

    private fun getCurrentServer(serverType: ServerType): Server? {
        return serversRepository.getCurrentServer(serverType)
    }

    private fun getSettingsRandomServer(serverType: ServerType): Boolean {
        if (isVpnActive()) {
            return false
        }

        return serversRepository.getSettingRandomServer(serverType)
    }

    private fun isFastestServerEnabled(): Boolean {
        if (multiHopController.isEnabled || isVpnActive()) {
            return false
        }

        return serversRepository.getSettingFastestServer()
    }

    private fun getVPNStateListener(): VpnStateListener {
        return object : DefaultVPNStateListener() {

            override fun onConnectionStateChanged(state: ConnectionState?) {
                if (state == null) {
                    return
                }
                updateServerVisibility()
            }

            override fun notifyServerAsFastest(server: Server) {
                entryServer.set(server)
                mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())
            }

            override fun notifyServerAsRandom(server: Server, serverType: ServerType) {
                when(serverType) {
                    ServerType.ENTRY -> entryServer.set(server)
                    ServerType.EXIT -> exitServer.set(server)
                }
                mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())
            }
        }
    }

    private fun updateServerVisibility() {
        fastestServer.set(isFastestServerEnabled())
        entryRandomServer.set(getSettingsRandomServer(ServerType.ENTRY))
        exitRandomServer.set(getSettingsRandomServer(ServerType.EXIT))

        entryServerVisibility.set(!fastestServer.get() && !entryRandomServer.get())
        exitServerVisibility.set(!exitRandomServer.get())
    }

    private fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    private fun ping(server: Server?, listener: OnPingFinishListener) {
        pingProvider.ping(server, listener)
    }

    private fun getOnMultihopValueChanges(): MultiHopController.OnValueChangeListener {
        return object : MultiHopController.OnValueChangeListener {
            override fun onValueChange(value: Boolean) {
                updateServerVisibility()
                mapServer.set(if (value) exitServer.get() else entryServer.get())
            }
        }
    }

    private fun getPingFinishListener(serverType: ServerType): OnPingFinishListener {
        return OnPingFinishListener { _, result: PingResultFormatter? ->
            if (serverType == ServerType.ENTRY) {
                pingResultEnterServer.set(result)
            } else {
                pingResultExitServer.set(result)
            }
        }
    }

    fun setServerLocation(serverLocation: ServerLocation) {
        val serverToConnect: Server = getServerFor(serverLocation) ?: return

        if (multiHopController.isEnabled) {
            if (serverToConnect.canBeUsedAsMultiHopWith(entryServer.get())) {
                exitServer.set(serverToConnect)
                serversRepository.serverSelected(serverToConnect, ServerType.EXIT)
            }
        } else {
            entryServer.set(serverToConnect)
            serversRepository.serverSelected(serverToConnect, ServerType.ENTRY)
        }
        mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())
    }

    private fun getServerFor(serverLocation: ServerLocation): Server? {
        var serverForLocation: Server? = null
        serversRepository.getServers(false)?.let {
            for (server in it) {
                if (serverLocation.city == server.city) {
                    serverForLocation = server
                    break
                }
            }
        }

        return serverForLocation
    }

    fun isLocationSuitable(serverLocation: ServerLocation): Boolean {
        if (multiHopController.isEnabled) {
            entryServer.get()?.let {
                return it.city != serverLocation.city && it.countryCode != serverLocation.countryCode
            }
        }

        return true
    }

    fun isExitServerIPv6BadgeEnabled(): Boolean {
        exitServer.get()?.let {
            return settings.ipv6Setting && settings.showAllServersSetting && it.isIPv6Enabled
        } ?: return false
    }

    fun isEntryServerIPv6BadgeEnabled(): Boolean {
        entryServer.get()?.let {
            return settings.ipv6Setting && settings.showAllServersSetting && it.isIPv6Enabled
        } ?: return false
    }
}