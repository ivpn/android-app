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

import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.rest.HttpClientFactory
import net.ivpn.core.rest.IVPNApi
import net.ivpn.core.rest.RequestListener
import net.ivpn.core.rest.data.ServersListResponse
import net.ivpn.core.rest.data.model.AntiTracker
import net.ivpn.core.rest.data.model.Config
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.rest.requests.common.Request
import net.ivpn.core.rest.requests.common.RequestWrapper
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.controller.VpnBehavior.OnRandomServerSelectionListener
import net.ivpn.core.vpn.model.V2RayPort
import net.ivpn.core.vpn.model.V2RaySettings
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*
import javax.inject.Inject

@ApplicationScope
class ServersRepository @Inject constructor(
        private val settings: Settings,
        private val httpClientFactory: HttpClientFactory,
        private val protocolController: ProtocolController,
        private val serversPreference: ServersPreference,
        private val userPreference: EncryptedUserPreference
): Serializable {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServersRepository::class.java)
    }

    private val currentServers = EnumMap<Protocol, EnumMap<ServerType, Server?>>(Protocol::class.java)
    private var onFavouritesChangedListeners: MutableList<OnFavouriteServersChangedListener> = ArrayList()
    private var onServerListUpdatedListeners: MutableList<OnServerListUpdatedListener> = ArrayList()
    private var onServerChangedListeners: List<OnServerChangedListener> = ArrayList()
    private var request: Request<ServersListResponse>? = null

    init {
        currentServers[Protocol.OPENVPN] = EnumMap(ServerType::class.java)
        currentServers[Protocol.WIREGUARD] = EnumMap(ServerType::class.java)
    }

    fun getCurrentServer(serverType: ServerType): Server? {
        var server = serversPreference.getCurrentServer(serverType)
        if (server == null) {
            server = getDefaultServer(serverType)
            setCurrentServer(serverType, server)
        }
        return server
    }

    private fun parseAndSaveV2RaySettings(config: Config?) {
        config?.ports?.v2ray?.let { v2rayConfig ->
            val wireguardPorts = v2rayConfig.wireguard?.map { port ->
                V2RayPort(
                    type = port.protocol,
                    port = port.portNumber
                )
            } ?: emptyList()

            val inboundPort = wireguardPorts.lastOrNull()?.port ?: 0

            val v2raySettings = V2RaySettings(
                id = v2rayConfig.id ?: "",
                outboundIp = "",
                outboundPort = 0,
                inboundIp = "",
                inboundPort = inboundPort,
                dnsName = "",
                wireguard = wireguardPorts
            )

            serversPreference.putV2RaySettings(v2raySettings)
            LOGGER.info("V2Ray settings saved: $v2raySettings")
        }
    }

    fun setCurrentServer(serverType: ServerType, server: Server?) {
        if (server == null) return
        LOGGER.info("Set current server = $server serverType = $serverType")
        getCurrentServers()[serverType] = server
        serversPreference.setCurrentServer(serverType, server)
        val anotherServerType = ServerType.getAnotherType(serverType)
        var serverMH = getCurrentServer(anotherServerType)
        if (!server.canBeUsedAsMultiHopWith(serverMH)) {
            serverMH = getDefaultServer(anotherServerType)
            getCurrentServers()[anotherServerType] = serverMH
            serversPreference.setCurrentServer(anotherServerType, serverMH)
        }
    }

    fun getDefaultServer(serverType: ServerType): Server? {
        val servers = getServers(false)
        if (!servers.isNullOrEmpty()) {
            val anotherServer = serversPreference.getCurrentServer(ServerType.getAnotherType(serverType))
            for (server in servers) {
                if (server.canBeUsedAsMultiHopWith(anotherServer)) {
                    return server
                }
            }
        }
        return null
    }

    fun isServersListExist(): Boolean {
        val servers = serversPreference.serversList
        return servers?.isNotEmpty() ?: false
    }

    fun getServers(isForced: Boolean): List<Server>? {
        var servers = serversPreference.serversList
        if (isForced || servers == null) {
            updateServerList(isForced)
            tryUpdateServerListOffline()
            servers = serversPreference.serversList
        }
        return servers
    }

    val locations: List<ServerLocation>?
        get() {
            var locations = serversPreference.serverLocations
            if (locations == null) {
                updateLocations()
                locations = serversPreference.serverLocations
            }
            return locations
        }

    private fun updateLocations() {
        val servers = serversPreference.serversList
        val locations: MutableList<ServerLocation> = ArrayList()
        if (servers == null) return
        for (server in servers) {
            locations.add(ServerLocation(
                    server.city,
                    server.countryCode,
                    server.latitude,
                    server.longitude
            ))
        }
        if (protocolController.currentProtocol == Protocol.OPENVPN) {
            serversPreference.putOpenVPNLocations(locations)
        } else {
            serversPreference.putWireGuardLocations(locations)
        }
    }

    fun getFavouritesServers(): List<Server> {
        return serversPreference.favouritesServersList
    }

    fun addFavouritesServer(server: Server) {
        LOGGER.info("addFavouritesServer server = $server")
        serversPreference.addFavouriteServer(server)
        notifyFavouriteServerAdded(server)
    }

    fun removeFavouritesServer(server: Server) {
        LOGGER.info("removeFavouritesServer server = $server")
        serversPreference.removeFavouriteServer(server)
        notifyFavouriteServerRemoved(server)
    }

    private fun getCachedServers(): List<Server>? {
        return serversPreference.serversList
    }

    fun getForbiddenServer(serverType: ServerType): Server? {
        val multiHop = settings.isMultiHopEnabled
        return if (!multiHop) null else serversPreference.getCurrentServer(ServerType.getAnotherType(serverType))
    }

    fun updateServerList(isForced: Boolean) {
        LOGGER.info("Updating server list, isForced = $isForced")
        request = Request(settings, httpClientFactory, this, Request.Duration.SHORT, RequestWrapper.IpMode.IPv4)
        request?.start({ obj: IVPNApi -> obj.servers }, object : RequestListener<ServersListResponse> {
            override fun onSuccess(response: ServersListResponse) {
                LOGGER.info("Updating server list, state = SUCCESS_STR")
                LOGGER.info(response.toString())
                response.markServerTypes()
                setServerList(response.openVpnServerList, response.wireGuardServerList)
                updateLocations()
                settings.setIpList(Mapper.stringFromIps(response.config.api.ips))
                settings.setIPv6List(Mapper.stringFromIps(response.config.api.ipv6s))
                settings.wireGuardPorts = response.config.ports.wireguard.filter { it.portNumber > 0 }
                settings.openVpnPorts = response.config.ports.openvpn.filter { it.portNumber > 0 }
                settings.wireGuardPortRanges = response.config.ports.wireguard.filter { it.range != null }
                settings.openVpnPortRanges = response.config.ports.openvpn.filter { it.range != null }
                settings.antiTrackerList = response.config.antiTrackerPlus.list
                if (settings.antiTracker == null) {
                    val defaultDns = AntiTracker()
                    settings.antiTracker = defaultDns.getDefaultList(settings.antiTrackerList, settings, userPreference)
                }
                parseAndSaveV2RaySettings(response.config)
                for (listener in onServerListUpdatedListeners) {
                    listener.onSuccess(getSuitableServers(response), isForced)
                }
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("Updating server list, state = ERROR")
                for (listener in onServerListUpdatedListeners) {
                    listener.onError(throwable)
                }
            }

            override fun onError(string: String) {
                LOGGER.error("Updating server list, state = ERROR")
                for (listener in onServerListUpdatedListeners) {
                    listener.onError()
                }
            }
        })
    }

    fun fastestServerSelected() {
        serversPreference.putSettingFastestServer(true)
        serversPreference.putSettingRandomServer(false, ServerType.ENTRY)
        for (listener in onServerChangedListeners) {
            listener.onServerChanged()
        }
    }

    fun randomServerSelected(type: ServerType) {
        serversPreference.putSettingRandomServer(true, type)
        if (type == ServerType.ENTRY) {
            serversPreference.putSettingFastestServer(false)
        }
        for (listener in onServerChangedListeners) {
            listener.onServerChanged()
        }
    }

    fun getRandomServerFor(serverType: ServerType, listener: OnRandomServerSelectionListener?) {
        val servers = getServers(false)
        if (!servers.isNullOrEmpty()) {
            val anotherServer = serversPreference.getCurrentServer(ServerType.getAnotherType(serverType))
            val availableServers = servers.filter { it.canBeUsedAsMultiHopWith(anotherServer)}
            val randomServer = availableServers.random()
            setCurrentServer(serverType, randomServer)
            listener?.onRandomServerSelected(randomServer, serverType)
        }
    }

    fun serverSelected(server: Server?, type: ServerType) {
        serversPreference.putSettingFastestServer(false)
        serversPreference.putSettingRandomServer(false, type)
        setCurrentServer(type, server)
        for (listener in onServerChangedListeners) {
            listener.onServerChanged()
        }
    }

    private fun tryUpdateServerListOffline() {
        LOGGER.info("Trying update server list offline from cache...")
        if (getCachedServers() != null) {
            return
        }
        updateServerListOffline()
    }

    fun updateServerListOffline() {
        val response = Mapper.getProtocolServers(ServersLoader.load())
        response?.let{
            it.markServerTypes()
            settings.setIpList(Mapper.stringFromIps(it.config.api.ips))
            settings.setIPv6List(Mapper.stringFromIps(it.config.api.ipv6s))
            settings.wireGuardPorts = response.config.ports.wireguard.filter { it.portNumber > 0 }
            settings.openVpnPorts = response.config.ports.openvpn.filter { it.portNumber > 0 }
            settings.wireGuardPortRanges = response.config.ports.wireguard.filter { it.range != null }
            settings.openVpnPortRanges = response.config.ports.openvpn.filter { it.range != null }
            settings.antiTrackerList = response.config.antiTrackerPlus.list
            if (settings.antiTracker == null) {
                val defaultDns = AntiTracker()
                settings.antiTracker = defaultDns.getDefaultList(settings.antiTrackerList, settings, userPreference)
            }
            parseAndSaveV2RaySettings(response.config)
            setServerList(it.openVpnServerList, it.wireGuardServerList)
        }
    }


    fun loadV2raySettings() {
        val response = Mapper.getProtocolServers(ServersLoader.load())
        response?.let {
            it.markServerTypes()
            settings.setIpList(Mapper.stringFromIps(it.config.api.ips))
            settings.setIPv6List(Mapper.stringFromIps(it.config.api.ipv6s))
            settings.antiTrackerList = it.config.antiTrackerPlus.list

            if (settings.antiTracker == null) {
                val defaultDns = AntiTracker()
                settings.antiTracker = defaultDns.getDefaultList(settings.antiTrackerList, settings, userPreference)
            }

            parseAndSaveV2RaySettings(it.config)
        }
    }


    fun tryUpdateIpList() {
        if (!settings.ipList.isNullOrEmpty()) {
            return
        }
        val response = Mapper.getProtocolServers(ServersLoader.load())
        response?.let {
            settings.wireGuardPorts = response.config.ports.wireguard.filter { it.portNumber > 0 }
            settings.openVpnPorts = response.config.ports.openvpn.filter { it.portNumber > 0 }
            settings.wireGuardPortRanges = response.config.ports.wireguard.filter { it.range != null }
            settings.openVpnPortRanges = response.config.ports.openvpn.filter { it.range != null }
            settings.antiTrackerList = response.config.antiTrackerPlus.list
            if (settings.antiTracker == null) {
                val defaultDns = AntiTracker()
                settings.antiTracker = defaultDns.getDefaultList(settings.antiTrackerList, settings, userPreference)
            }
            parseAndSaveV2RaySettings(response.config)
            settings.setIpList(Mapper.stringFromIps(it.config.api.ips))
            settings.setIPv6List(Mapper.stringFromIps(it.config.api.ipv6s))
        }
    }

    fun tryUpdateServerLocations() {
        LOGGER.info("tryUpdateServerLocations BEFORE")
        if (serversPreference.serverLocations != null) {
            return
        }
        LOGGER.info("tryUpdateServerLocations AFTER")
        val response = Mapper.getProtocolServers(ServersLoader.load())
        response?.let {
            it.markServerTypes()
            val locations: MutableList<ServerLocation> = ArrayList()
            for (server in it.openVpnServerList) {
                locations.add(ServerLocation(
                        server.city,
                        server.countryCode,
                        server.latitude,
                        server.longitude
                ))
            }
            serversPreference.putOpenVPNLocations(locations)
            locations.clear()
            for (server in it.wireGuardServerList) {
                locations.add(ServerLocation(
                        server.city,
                        server.countryCode,
                        server.latitude,
                        server.longitude
                ))
            }
            serversPreference.putWireGuardLocations(locations)
            setServerList(it.openVpnServerList, it.wireGuardServerList)
            updateCurrentServersWithLocation()
            currentServers[Protocol.OPENVPN] = EnumMap(ServerType::class.java)
            currentServers[Protocol.WIREGUARD] = EnumMap(ServerType::class.java)
        }
    }

    private fun updateCurrentServersWithLocation() {
        serversPreference.updateCurrentServersWithLocation()
    }

    fun setLocationList(openVpnLocations: List<ServerLocation>, wireguardLocations: List<ServerLocation>) {
        LOGGER.info("Putting locations, OpenVPN locations list size = " + openVpnLocations.size + " WireGuard = " + wireguardLocations.size)
        serversPreference.putOpenVPNLocations(openVpnLocations)
        serversPreference.putWireGuardLocations(wireguardLocations)
    }

    fun setServerList(openvpnServers: List<Server?>, wireguardServers: List<Server?>) {
        LOGGER.info("Putting servers, OpenVpn servers list size = " + openvpnServers.size + " WireGuard = " + wireguardServers.size)
        serversPreference.putOpenVpnServerList(openvpnServers)
        serversPreference.putWireGuardServerList(wireguardServers)
    }

    fun addToExcludedServersList(server: Server) {
        LOGGER.info("Add tot excluded servers list: $server")
        serversPreference.addToExcludedServersList(server)
    }

    fun removeFromExcludedServerList(server: Server) {
        LOGGER.info("Remove tot excluded servers list: $server")
        serversPreference.removeFromExcludedServerList(server)
    }

    fun getExcludedServersList(): List<Server> {
        return serversPreference.excludedServersList
    }

    fun getSettingFastestServer(): Boolean {
        return serversPreference.settingFastestServer
    }

    fun getSettingRandomServer(serverType: ServerType): Boolean {
        return serversPreference.getSettingRandomServer(serverType)
    }

    fun addOnServersListUpdatedListener(listener: OnServerListUpdatedListener) {
        onServerListUpdatedListeners.add(listener)
    }

    fun removeOnServersListUpdatedListener(listener: OnServerListUpdatedListener) {
        onServerListUpdatedListeners.remove(listener)
    }

    private val currentProtocolType: Protocol
        get() = protocolController.currentProtocol

    private fun getCurrentServers(): EnumMap<ServerType, Server?> {
        val currentProtocol = currentProtocolType
        return currentServers[currentProtocol]!!
    }

    private fun getSuitableServers(response: ServersListResponse): List<Server> {
        return if (currentProtocolType == Protocol.WIREGUARD) {
            response.wireGuardServerList
        } else {
            response.openVpnServerList
        }
    }

    private fun notifyFavouriteServerAdded(server: Server) {
        onFavouritesChangedListeners.forEach { it.notifyFavouriteServerAdded(server) }
    }

    private fun notifyFavouriteServerRemoved(server: Server) {
        onFavouritesChangedListeners.forEach { it.notifyFavouriteServerRemoved(server) }
    }
}
