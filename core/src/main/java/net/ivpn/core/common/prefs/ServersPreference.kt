package net.ivpn.core.common.prefs

import android.content.SharedPreferences
import net.ivpn.core.common.Mapper
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.rest.data.model.FavoriteIdentifier
import net.ivpn.core.rest.data.model.Host
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.rest.data.model.ServerLocation.Companion.from
import net.ivpn.core.rest.data.model.ServerLocation.Companion.stringFrom
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.model.V2RaySettings
import java.util.*
import javax.inject.Inject
import androidx.core.content.edit

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

@ApplicationScope
class ServersPreference @Inject constructor(
    private val preference: Preference, private val protocolController: ProtocolController
) {

    companion object {
        private const val CURRENT_ENTER_SERVER = "CURRENT_ENTER_SERVER"
        private const val CURRENT_EXIT_SERVER = "CURRENT_EXIT_SERVER"
        private const val CURRENT_ENTER_HOST = "CURRENT_ENTER_HOST"
        private const val CURRENT_EXIT_HOST = "CURRENT_EXIT_HOST"
        private const val SERVERS_LIST = "SERVERS_LIST"
        private const val LOCATION_LIST = "LOCATION_LIST"
        private const val FAVOURITES_SERVERS_LIST = "FAVOURITES_SERVERS_LIST"
        private const val UNIFIED_FAVOURITES_LIST = "UNIFIED_FAVOURITES_LIST"
        private const val EXCLUDED_FASTEST_SERVERS = "EXCLUDED_FASTEST_SERVERS"
        private const val SETTINGS_FASTEST_SERVER = "SETTINGS_FASTEST_SERVER"
        private const val SETTINGS_RANDOM_ENTER_SERVER = "SETTINGS_RANDOM_ENTER_SERVER"
        private const val SETTINGS_RANDOM_EXIT_SERVER = "SETTINGS_RANDOM_EXIT_SERVER"
        private const val V2RAY_SETTINGS = "V2RAY_SETTINGS"
        private const val FAVOURITES_HOSTS_LIST = "FAVOURITES_HOSTS_LIST"
        private const val HOST_FAVOURITES_MIGRATED = "HOST_FAVOURITES_MIGRATED"
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

    /**
     * Returns the list of favorite servers for the current protocol.
     * This uses the unified favorites system where favorites are stored as
     * protocol-agnostic identifiers (gateway prefix for locations, dns_name for hosts).
     * When retrieving, it matches these identifiers against the current protocol's servers.
     */
    val favouritesServersList: MutableList<Server>
        get() {
            val identifiers = unifiedFavouritesList
            val currentServers = serversList ?: return ArrayList()
            
            val favourites = ArrayList<Server>()
            for (server in currentServers) {
                for (identifier in identifiers) {
                    if (identifier.isLocationFavorite && identifier.matches(server)) {
                        favourites.add(server)
                        break
                    }
                }
            }
            return favourites
        }

    /**
     * Returns the unified list of favorite identifiers.
     * These identifiers are protocol-agnostic and work across OpenVPN and WireGuard.
     */
    val unifiedFavouritesList: MutableList<FavoriteIdentifier>
        get() {
            // First try to get from unified storage
            val sharedPreferences = preference.stickySharedPreferences
            val identifiers = Mapper.favoriteIdentifierListFrom(
                sharedPreferences.getString(UNIFIED_FAVOURITES_LIST, null)
            )
            return identifiers ?: ArrayList()
        }

    val openvpnFavouritesServersList: MutableList<Server>
        get() {
            val identifiers = unifiedFavouritesList
            val currentServers = openvpnServersList ?: return ArrayList()
            
            val favourites = ArrayList<Server>()
            for (server in currentServers) {
                for (identifier in identifiers) {
                    if (identifier.isLocationFavorite && identifier.matches(server)) {
                        favourites.add(server)
                        break
                    }
                }
            }
            return favourites
        }

    val wireguardFavouritesServersList: MutableList<Server>
        get() {
            val identifiers = unifiedFavouritesList
            val currentServers = wireguardServersList ?: return ArrayList()
            
            val favourites = ArrayList<Server>()
            for (server in currentServers) {
                for (identifier in identifiers) {
                    if (identifier.isLocationFavorite && identifier.matches(server)) {
                        favourites.add(server)
                        break
                    }
                }
            }
            return favourites
        }

    val excludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = properSharedPreference
            val servers =
                Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val openvpnExcludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            val servers =
                Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val wireguardExcludedServersList: MutableList<Server>
        get() {
            val sharedPreferences = preference.wireguardServersSharedPreferences
            val servers =
                Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null))
            return servers ?: ArrayList()
        }

    val settingFastestServer: Boolean
        get() {
            val sharedPreferences = preference.serversSharedPreferences
            return sharedPreferences.getBoolean(SETTINGS_FASTEST_SERVER, true)
        }

    fun setCurrentServer(serverType: ServerType?, server: Server?) {
        if (serverType == null || server == null) return
        val openvpnServer = openvpnServersList?.firstOrNull { it == server }
        val wireguardServer = wireguardServersList?.firstOrNull { it == server }
        val serverKey =
            if (serverType == ServerType.ENTRY) CURRENT_ENTER_SERVER else CURRENT_EXIT_SERVER
        preference.serversSharedPreferences.edit().putString(serverKey, Mapper.from(openvpnServer))
            .apply()
        preference.wireguardServersSharedPreferences.edit {
            putString(serverKey, Mapper.from(wireguardServer))
        }
    }

    fun getV2RaySettings(): V2RaySettings? {
        val sharedPreferences = properSharedPreference
        val json = sharedPreferences.getString(V2RAY_SETTINGS, null)
        return Mapper.v2RaySettingsFrom(json)
    }

    fun putV2RaySettings(settings: V2RaySettings?) {
        val sharedPreferences = properSharedPreference
        sharedPreferences.edit {
            putString(V2RAY_SETTINGS, Mapper.stringFromV2RaySettings(settings))
        }
    }


    fun putOpenVpnServerList(servers: List<Server?>?) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit().putString(SERVERS_LIST, Mapper.stringFrom(servers)).apply()
    }

    fun putWireGuardServerList(servers: List<Server?>?) {
        val sharedPreferences = preference.wireguardServersSharedPreferences
        sharedPreferences.edit().putString(SERVERS_LIST, Mapper.stringFrom(servers)).apply()
    }

    fun putOpenVPNLocations(locations: List<ServerLocation>) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit().putString(LOCATION_LIST, stringFrom(locations)).apply()
    }

    fun putWireGuardLocations(locations: List<ServerLocation>) {
        val sharedPreferences = preference.wireguardServersSharedPreferences
        sharedPreferences.edit().putString(LOCATION_LIST, stringFrom(locations)).apply()
    }

    fun getCurrentServer(serverType: ServerType?): Server? {
        if (serverType == null) return null
        val sharedPreferences = properSharedPreference
        val serverKey =
            if (serverType == ServerType.ENTRY) CURRENT_ENTER_SERVER else CURRENT_EXIT_SERVER
        return Mapper.from(sharedPreferences.getString(serverKey, null))
    }

    fun setCurrentHost(serverType: ServerType?, host: Host?) {
        if (serverType == null) return
        val hostKey =
            if (serverType == ServerType.ENTRY) CURRENT_ENTER_HOST else CURRENT_EXIT_HOST
        preference.serversSharedPreferences.edit {
            putString(hostKey, Mapper.stringFromHost(host))
        }
        preference.wireguardServersSharedPreferences.edit {
            putString(hostKey, Mapper.stringFromHost(host))
        }
    }

    fun getCurrentHost(serverType: ServerType?): Host? {
        if (serverType == null) return null
        val sharedPreferences = properSharedPreference
        val hostKey =
            if (serverType == ServerType.ENTRY) CURRENT_ENTER_HOST else CURRENT_EXIT_HOST
        return Mapper.hostFrom(sharedPreferences.getString(hostKey, null))
    }

    fun clearCurrentHost(serverType: ServerType?) {
        if (serverType == null) return
        val hostKey =
            if (serverType == ServerType.ENTRY) CURRENT_ENTER_HOST else CURRENT_EXIT_HOST
        preference.serversSharedPreferences.edit { remove(hostKey) }
        preference.wireguardServersSharedPreferences.edit { remove(hostKey) }
    }

    /**
     * Adds a server to the unified favorites list.
     * The server is stored as a protocol-agnostic identifier:
     * - For locations: normalized gateway (with .wg. replaced by .gw.)
     * - For hosts: dns_name
     */
    fun addFavouriteServer(server: Server?) {
        if (server == null) return
        
        val identifier = FavoriteIdentifier.fromServer(server)
        val identifiers = unifiedFavouritesList
        
        // Check if already in favorites
        if (identifiers.any { it == identifier }) {
            return
        }
        
        identifiers.add(identifier)
        saveUnifiedFavourites(identifiers)
    }

    /**
     * Removes a server from the unified favorites list.
     */
    fun removeFavouriteServer(server: Server) {
        val identifier = FavoriteIdentifier.fromServer(server)
        val identifiers = unifiedFavouritesList
        
        identifiers.removeAll { it == identifier }
        saveUnifiedFavourites(identifiers)
    }

    /**
     * Adds a specific host to favorites by dns_name.
     */
    fun addFavouriteHost(dnsName: String) {
        val identifier = FavoriteIdentifier.forHost(dnsName)
        val identifiers = unifiedFavouritesList
        
        if (identifiers.any { it == identifier }) {
            return
        }
        
        identifiers.add(identifier)
        saveUnifiedFavourites(identifiers)
    }

    /**
     * Removes a specific host from favorites by dns_name.
     */
    fun removeFavouriteHost(dnsName: String) {
        val identifier = FavoriteIdentifier.forHost(dnsName)
        val identifiers = unifiedFavouritesList
        
        identifiers.removeAll { it == identifier }
        saveUnifiedFavourites(identifiers)
    }

    /**
     * Checks if a server is in the favorites list.
     */
    fun isFavourite(server: Server): Boolean {
        val identifier = FavoriteIdentifier.fromServer(server)
        return unifiedFavouritesList.any { it == identifier }
    }

    /**
     * Clears the favorites list from storage.
     */
    fun clearFavourites() {
        preference.stickySharedPreferences.edit {
            remove(UNIFIED_FAVOURITES_LIST)
        }
    }

    /**
     * Saves the unified favorites list.
     */
    private fun saveUnifiedFavourites(identifiers: List<FavoriteIdentifier>) {
        preference.stickySharedPreferences.edit {
            putString(UNIFIED_FAVOURITES_LIST, Mapper.stringFromFavoriteIdentifiers(identifiers))
        }
    }

    /**
     * Migrates old per-protocol favorites to the new unified format.
     * This should be called once during app upgrade.
     */
    fun migrateOldFavouritesToUnified() {
        // Check if already migrated
        if (preference.stickySharedPreferences.contains(UNIFIED_FAVOURITES_LIST)) {
            return
        }

        val identifiers = mutableSetOf<FavoriteIdentifier>()

        val oldOpenvpnFavourites = Mapper.serverListFrom(
            preference.serversSharedPreferences.getString(FAVOURITES_SERVERS_LIST, null)
        )
        oldOpenvpnFavourites?.forEach { server ->
            identifiers.add(FavoriteIdentifier.fromServer(server))
        }

        val oldWireguardFavourites = Mapper.serverListFrom(
            preference.wireguardServersSharedPreferences.getString(FAVOURITES_SERVERS_LIST, null)
        )
        oldWireguardFavourites?.forEach { server ->
            identifiers.add(FavoriteIdentifier.fromServer(server))
        }

        if (identifiers.isNotEmpty()) {
            saveUnifiedFavourites(identifiers.toList())
        }
    }

    
    fun migrateLegacyHostFavouritesToUnified() {
        if (preference.stickySharedPreferences.getBoolean(HOST_FAVOURITES_MIGRATED, false)) {
            return
        }

        val identifiers = unifiedFavouritesList.toMutableList()

        fun migrateFromPrefs(servers: List<Server>?, prefs: SharedPreferences) {
            servers ?: return
            for (server in servers) {
                val key = "${FAVOURITES_HOSTS_LIST}_${getFavouriteHostsKey(server)}"
                val hostnames = prefs.getStringSet(key, null) ?: continue
                server.hosts?.forEach { host ->
                    if (host.hostname != null && hostnames.contains(host.hostname)) {
                        val dnsName = host.dnsName ?: host.hostname
                        val identifier = FavoriteIdentifier.forHost(dnsName)
                        if (!identifiers.any { it == identifier }) {
                            identifiers.add(identifier)
                        }
                    }
                }
            }
        }

        migrateFromPrefs(openvpnServersList, preference.serversSharedPreferences)
        migrateFromPrefs(wireguardServersList, preference.wireguardServersSharedPreferences)

        if (identifiers.size > unifiedFavouritesList.size) {
            saveUnifiedFavourites(identifiers)
        }

        clearLegacyHostFavourites(preference.serversSharedPreferences)
        clearLegacyHostFavourites(preference.wireguardServersSharedPreferences)

        preference.stickySharedPreferences.edit {
            putBoolean(HOST_FAVOURITES_MIGRATED, true)
        }
    }

    private fun clearLegacyHostFavourites(prefs: SharedPreferences) {
        val allKeys = prefs.all.keys.filter { it.startsWith(FAVOURITES_HOSTS_LIST) }
        if (allKeys.isNotEmpty()) {
            prefs.edit {
                allKeys.forEach { remove(it) }
            }
        }
    }

    private fun getFavouriteHostsKey(server: Server): String {
        return "${server.city}_${server.countryCode}"
    }

    fun addFavouriteHost(host: Host, parentServer: Server) {
        val dnsName = host.dnsName ?: host.hostname ?: return
        addFavouriteHost(dnsName)
    }

    fun removeFavouriteHost(host: Host, parentServer: Server) {
        val dnsName = host.dnsName ?: host.hostname ?: return
        removeFavouriteHost(dnsName)
    }

    fun isHostFavourite(host: Host, parentServer: Server): Boolean {
        val dnsName = host.dnsName ?: host.hostname ?: return false
        return unifiedFavouritesList.any { it.isHostFavorite && it.dnsName?.equals(dnsName, ignoreCase = true) == true }
    }

    fun getFavouriteHosts(servers: List<Server>): List<Pair<Host, Server>> {
        val result = mutableListOf<Pair<Host, Server>>()
        val hostIdentifiers = unifiedFavouritesList.filter { it.isHostFavorite }
        for (server in servers) {
            server.hosts?.forEach { host ->
                val dnsName = host.dnsName ?: host.hostname ?: return@forEach
                if (hostIdentifiers.any { it.dnsName?.equals(dnsName, ignoreCase = true) == true }) {
                    result.add(Pair(host, server))
                }
            }
        }
        return result
    }

    fun addToExcludedServersList(server: Server?) {
        val openvpnServer = openvpnServersList?.first { it == server }
        val wireguardServer = wireguardServersList?.first { it == server }
        if (server == null || openvpnServer == null || wireguardServer == null || excludedServersList.contains(
                server
            )
        ) {
            return
        }
        val openvpnServers = openvpnExcludedServersList
        val wireguardServers = wireguardExcludedServersList
        openvpnServers.add(openvpnServer)
        wireguardServers.add(wireguardServer)
        preference.serversSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(openvpnServers)).apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(wireguardServers)).apply()
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
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(openvpnServers)).apply()
        preference.wireguardServersSharedPreferences.edit()
            .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(wireguardServers)).apply()
        notifyValueChanges()
    }

    fun putSettingFastestServer(value: Boolean) {
        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit().putBoolean(SETTINGS_FASTEST_SERVER, value).apply()
    }

    fun putSettingRandomServer(value: Boolean, serverType: ServerType) {
        val key = if (serverType == ServerType.ENTRY) SETTINGS_RANDOM_ENTER_SERVER
        else SETTINGS_RANDOM_EXIT_SERVER

        val sharedPreferences = preference.serversSharedPreferences
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getSettingRandomServer(serverType: ServerType): Boolean {
        val key = if (serverType == ServerType.ENTRY) SETTINGS_RANDOM_ENTER_SERVER
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
                    preference.edit().putString(CURRENT_ENTER_SERVER, Mapper.from(server)).apply()
                    break
                }
            }
        }
        if (exitServer != null && exitServer.latitude == 0.0 && exitServer.longitude == 0.0) {
            for (server in servers) {
                if (server == exitServer) {
                    preference.edit().putString(CURRENT_EXIT_SERVER, Mapper.from(server)).apply()
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
                    preference.edit().putString(CURRENT_ENTER_SERVER, Mapper.from(server)).apply()
                    break
                }
            }
        }
        if (exitServer != null && exitServer.hosts.random().multihopPort == 0) {
            for (server in servers) {
                if (server == exitServer) {
                    preference.edit().putString(CURRENT_EXIT_SERVER, Mapper.from(server)).apply()
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