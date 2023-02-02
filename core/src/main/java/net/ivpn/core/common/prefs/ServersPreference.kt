package net.ivpn.core.common.prefs

import android.content.SharedPreferences
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.rest.data.model.ServerLocation.Companion.from
import net.ivpn.core.rest.data.model.ServerLocation.Companion.stringFrom
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import java.util.*
import javax.inject.Inject

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

@ApplicationScope
class ServersPreference @Inject constructor(
        private val preference: Preference,
        private val protocolController: ProtocolController
) {

    companion object {
        private const val CURRENT_ENTER_SERVER = "CURRENT_ENTER_SERVER"
        private const val CURRENT_EXIT_SERVER = "CURRENT_EXIT_SERVER"
        private const val SERVERS_LIST = "SERVERS_LIST"
        private const val LOCATION_LIST = "LOCATION_LIST"
        private const val FAVOURITES_SERVERS_LIST = "FAVOURITES_SERVERS_LIST"
        private const val EXCLUDED_FASTEST_SERVERS = "EXCLUDED_FASTEST_SERVERS"
        private const val SETTINGS_FASTEST_SERVER = "SETTINGS_FASTEST_SERVER"
        private const val SETTINGS_RANDOM_ENTER_SERVER = "SETTINGS_RANDOM_ENTER_SERVER"
        private const val SETTINGS_RANDOM_EXIT_SERVER = "SETTINGS_RANDOM_EXIT_SERVER"
    }

    var listeners = ArrayList<OnValueChangeListener>()

    private val properSharedPreference: SharedPreferences
        get() {
            val protocol = protocolController.currentProtocol
            return if (protocol == Protocol.WIREGUARD) {
                preference.wireguardServersSharedPreferences
            } else {
                preference.serversSharedPreferences
            }
        }

    val serverLocations: List<ServerLocation>?
        get() {
            val sharedPreferences = properSharedPreference
            return from(sharedPreferences.getString(LOCATION_LIST, null))
        }

    val serversList: List<Server>?
        get() {
            val sharedPreferences = properSharedPreference
            return Mapper.serverListFrom(sharedPreferences.getString(SERVERS_LIST, null))
        }

    val openvpnServersList: List<Server>?
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            return Mapper.serverListFrom(sharedPreferences.getString(SERVERS_LIST, null))
        }

    val wireguardServersList: List<Server>?
        get() {
            val sharedPreferences = preference.wireguardServersSharedPreferences
            return Mapper.serverListFrom(sharedPreferences.getString(SERVERS_LIST, null))
        }

    val favouritesServersList: MutableList<Server>
        get() {
            val sharedPreferences = properSharedPreference
            val servers = Mapper.serverListFrom(sharedPreferences.getString(FAVOURITES_SERVERS_LIST, null))
            return servers ?: ArrayList()
        }

    val openvpnFavouritesServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            val servers = Mapper.serverListFrom(sharedPreferences.getString(FAVOURITES_SERVERS_LIST, null))
            return servers ?: ArrayList()
        }

    val wireguardFavouritesServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.wireguardServersSharedPreferences
            val servers = Mapper.serverListFrom(sharedPreferences.getString(FAVOURITES_SERVERS_LIST, null))
            return servers ?: ArrayList()
        }

    val excludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = properSharedPreference
            val servers = Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val openvpnExcludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            val servers = Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val wireguardExcludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.wireguardServersSharedPreferences
            val servers = Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val settingFastestServer: Boolean
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            return sharedPreferences.getBoolean(SETTINGS_FASTEST_SERVER, true)
        }

    fun setCurrentServer(serverType: ServerType?, server: Server?) {
        if (serverType == null || server == null) return
        val sharedPreferences = preference.serversSharedPreferences
        val serverKey = if (serverType == ServerType.ENTRY) CURRENT_ENTER_SERVER else CURRENT_EXIT_SERVER
        sharedPreferences.edit()
                .putString(serverKey, Mapper.from(server))
                .apply()
    }

    fun putOpenVpnServerList(servers: List<Server?>?) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit()
                .putString(SERVERS_LIST, Mapper.stringFrom(servers))
                .apply()
    }

    fun putWireGuardServerList(servers: List<Server?>?) {
        val sharedPreferences = preference.wireguardServersSharedPreferences
        sharedPreferences.edit()
                .putString(SERVERS_LIST, Mapper.stringFrom(servers))
                .apply()
    }

    fun putOpenVPNLocations(locations: List<ServerLocation>) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit()
                .putString(LOCATION_LIST, stringFrom(locations))
                .apply()
    }

    fun putWireGuardLocations(locations: List<ServerLocation>) {
        val sharedPreferences = preference.wireguardServersSharedPreferences
        sharedPreferences.edit()
                .putString(LOCATION_LIST, stringFrom(locations))
                .apply()
    }

    fun getCurrentServer(serverType: ServerType?): Server? {
        if (serverType == null) return null
        val sharedPreferences = preference.serversSharedPreferences
        val serverKey = if (serverType == ServerType.ENTRY) CURRENT_ENTER_SERVER else CURRENT_EXIT_SERVER
        return Mapper.from(sharedPreferences.getString(serverKey, null))
    }

    fun addFavouriteServer(server: Server?) {
        val openvpnServer = openvpnServersList?.first { it == server }
        val wireguardServer = wireguardServersList?.first { it == server }
        if (server == null || openvpnServer == null || wireguardServer == null || favouritesServersList.contains(server)) {
            return
        }
        val openvpnServers = openvpnFavouritesServersList
        val wireguardServers = wireguardFavouritesServersList
        openvpnServers.add(openvpnServer)
        wireguardServers.add(wireguardServer)
        preference.serversSharedPreferences.edit()
            .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(openvpnServers))
            .apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(wireguardServers))
            .apply()
    }

    fun removeFavouriteServer(server: Server) {
        val openvpnServer = openvpnServersList?.first { it == server }
        val wireguardServer = wireguardServersList?.first { it == server }
        if (openvpnServer == null || wireguardServer == null) {
            return
        }
        val openvpnServers = openvpnFavouritesServersList
        val wireguardServers = wireguardFavouritesServersList
        openvpnServers.remove(openvpnServer)
        wireguardServers.remove(wireguardServer)
        preference.serversSharedPreferences.edit()
            .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(openvpnServers))
            .apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(wireguardServers))
            .apply()
    }

    fun addToExcludedServersList(server: Server?) {
        val openvpnServer = openvpnServersList?.first { it == server }
        val wireguardServer = wireguardServersList?.first { it == server }
        if (server == null || openvpnServer == null || wireguardServer == null || excludedServersList.contains(server)) {
            return
        }
        val openvpnServers = openvpnExcludedServersList
        val wireguardServers = wireguardExcludedServersList
        openvpnServers.add(openvpnServer)
        wireguardServers.add(wireguardServer)
        preference.serversSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(openvpnServers))
            .apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(wireguardServers))
            .apply()
        notifyValueChanges()
    }

    fun removeFromExcludedServerList(server: Server) {
        val openvpnServer = openvpnServersList?.first { it == server }
        val wireguardServer = wireguardServersList?.first { it == server }
        if (openvpnServer == null || wireguardServer == null) {
            return
        }
        val openvpnServers = openvpnExcludedServersList
        val wireguardServers = wireguardExcludedServersList
        openvpnServers.remove(openvpnServer)
        wireguardServers.remove(wireguardServer)
        preference.serversSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(openvpnServers))
            .apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(wireguardServers))
            .apply()
        notifyValueChanges()
    }

    fun putSettingFastestServer(value: Boolean) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit()
                .putBoolean(SETTINGS_FASTEST_SERVER, value)
                .apply()
    }

    fun putSettingRandomServer(value: Boolean, serverType: ServerType) {
        val key = if (serverType == ServerType.ENTRY)
            SETTINGS_RANDOM_ENTER_SERVER
        else SETTINGS_RANDOM_EXIT_SERVER

        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit()
                .putBoolean(key, value)
                .apply()
    }

    fun getSettingRandomServer(serverType: ServerType): Boolean {
        val key = if (serverType == ServerType.ENTRY)
            SETTINGS_RANDOM_ENTER_SERVER
        else SETTINGS_RANDOM_EXIT_SERVER

        val sharedPreferences = preference.serversSharedPreferences
        return sharedPreferences.getBoolean(key, false)
    }

    fun updateCurrentServersWithLocation() {
        updateCurrentServersWithLocationFor(preference.wireguardServersSharedPreferences)
        updateCurrentServersWithLocationFor(preference.serversSharedPreferences)
    }

    fun updateCurrentServersWithPort() {
        updateCurrentServersWithPortFor(preference.wireguardServersSharedPreferences)
    }

    private fun updateCurrentServersWithLocationFor(preference: SharedPreferences) {
        val servers = Mapper.serverListFrom(preference.getString(SERVERS_LIST, null))
        if (servers == null || servers.isEmpty()) {
            return
        }
        val entryServer = Mapper.from(preference.getString(CURRENT_ENTER_SERVER, null))
        val exitServer = Mapper.from(preference.getString(CURRENT_EXIT_SERVER, null))
        if (entryServer != null && entryServer.latitude == 0.0 && entryServer.longitude == 0.0) {
            for (server in servers) {
                if (server == entryServer) {
                    preference.edit()
                            .putString(CURRENT_ENTER_SERVER, Mapper.from(server))
                            .apply()
                    break
                }
            }
        }
        if (exitServer != null && exitServer.latitude == 0.0 && exitServer.longitude == 0.0) {
            for (server in servers) {
                if (server == exitServer) {
                    preference.edit()
                            .putString(CURRENT_EXIT_SERVER, Mapper.from(server))
                            .apply()
                    break
                }
            }
        }
    }

    private fun updateCurrentServersWithPortFor(preference: SharedPreferences) {
        val servers = Mapper.serverListFrom(preference.getString(SERVERS_LIST, null))
        if (servers == null || servers.isEmpty()) {
            return
        }
        val entryServer = Mapper.from(preference.getString(CURRENT_ENTER_SERVER, null))
        val exitServer = Mapper.from(preference.getString(CURRENT_EXIT_SERVER, null))
        if (entryServer != null && entryServer.hosts.random().multihopPort == 0) {
            for (server in servers) {
                if (server == entryServer) {
                    preference.edit()
                        .putString(CURRENT_ENTER_SERVER, Mapper.from(server))
                        .apply()
                    break
                }
            }
        }
        if (exitServer != null && exitServer.hosts.random().multihopPort == 0) {
            for (server in servers) {
                if (server == exitServer) {
                    preference.edit()
                        .putString(CURRENT_EXIT_SERVER, Mapper.from(server))
                        .apply()
                    break
                }
            }
        }
    }

    fun addListener(listener: OnValueChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnValueChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyValueChanges() {
        for (listener in listeners) {
            listener.onValueChange()
        }
    }

    interface OnValueChangeListener {
        fun onValueChange()
    }

}