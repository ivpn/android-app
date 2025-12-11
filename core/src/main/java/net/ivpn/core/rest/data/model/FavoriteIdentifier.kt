package net.ivpn.core.rest.data.model

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents a protocol-agnostic favorite identifier.
 * This allows favorites to be shared across VPN protocols (OpenVPN and WireGuard).
 *
 * - For locations: stores normalized gateway (with .wg. replaced by .gw.)
 * - For specific hosts: stores the dns_name
 */
data class FavoriteIdentifier(
    /**
     * The normalized gateway for location-based favorites.
     * Example: "gb.gw.ivpn.net" (normalized from either "gb.wg.ivpn.net" or "gb.gw.ivpn.net")
     * This is stored with .wg. replaced by .gw. to be protocol-agnostic.
     */
    @SerializedName("gateway")
    @Expose
    val gateway: String? = null,

    /**
     * The dns_name for host-based favorites.
     * This is used when a specific host is favorited.
     * Example: "us-ca1.dns.ivpn.net"
     */
    @SerializedName("dns_name")
    @Expose
    val dnsName: String? = null,

    /**
     * Flag to indicate if this is a host favorite.
     * Mirrors iOS implementation where hosts have country == "" && gateway != ""
     */
    @SerializedName("is_host")
    @Expose
    val isHost: Boolean = false
) {
    /**
     * Checks if this identifier represents a host favorite (specific server).
     */
    val isHostFavorite: Boolean
        get() = isHost && !dnsName.isNullOrEmpty()

    /**
     * Checks if this identifier represents a location favorite.
     */
    val isLocationFavorite: Boolean
        get() = !isHost && !gateway.isNullOrEmpty()

    /**
     * Checks if this identifier matches the given server.
     *
     * For location favorites: matches if normalized gateway matches
     * For host favorites: matches if dns_name matches
     */
    fun matches(server: Server): Boolean {
        return when {
            isHostFavorite -> server.hasHostWithDnsName(dnsName)
            isLocationFavorite -> server.getNormalizedGateway().equals(gateway, ignoreCase = true)
            else -> false
        }
    }

    companion object {
        /**
         * Creates a FavoriteIdentifier for a location (server with all hosts).
         * Stores the normalized gateway (with .wg. replaced by .gw.)
         */
        fun forLocation(server: Server): FavoriteIdentifier {
            return FavoriteIdentifier(
                gateway = server.getNormalizedGateway(),
                isHost = false
            )
        }

        /**
         * Creates a FavoriteIdentifier for a specific host by dns_name.
         */
        fun forHost(dnsName: String): FavoriteIdentifier {
            return FavoriteIdentifier(
                dnsName = dnsName,
                isHost = true
            )
        }

        /**
         * Creates a FavoriteIdentifier from a server.
         * The server model in Android represents locations, not individual hosts.
         */
        fun fromServer(server: Server): FavoriteIdentifier {
            return forLocation(server)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FavoriteIdentifier) return false

        return when {
            isHostFavorite && other.isHostFavorite -> dnsName.equals(other.dnsName, ignoreCase = true)
            isLocationFavorite && other.isLocationFavorite -> 
                gateway.equals(other.gateway, ignoreCase = true)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return when {
            isHostFavorite -> dnsName?.lowercase().hashCode()
            isLocationFavorite -> gateway?.lowercase().hashCode()
            else -> 0
        }
    }
}
