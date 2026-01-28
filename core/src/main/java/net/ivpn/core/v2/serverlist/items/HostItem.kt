package net.ivpn.core.v2.serverlist.items

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

import net.ivpn.core.rest.data.model.Host
import net.ivpn.core.rest.data.model.Server

/**
 * Represents a host item in the server list, used for displaying individual 
 * hosts when a server is expanded. This allows users to select a specific
 * host for consistent IP address connections.
 */
data class HostItem(
    val host: Host,
    val parentServer: Server
) : ConnectionOption {
    
    /**
     * Returns the host name (e.g., "gb-lon-wg-001.relays.ivpn.net")
     */
    fun getHostName(): String {
        return host.hostname ?: ""
    }
    
    /**
     * Returns a shortened host name for display (e.g., "gb-lon-wg-001")
     */
    fun getShortHostName(): String {
        val hostname = host.hostname ?: return ""
        return hostname.substringBefore(".relays")
    }
    
    /**
     * Returns the server load as a formatted percentage string
     */
    fun getLoadPercentage(): String {
        return "${host.load.toInt()}%"
    }
    
    /**
     * Returns the raw load value (0-100)
     */
    fun getLoad(): Double {
        return host.load
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HostItem) return false
        return host.hostname == other.host.hostname
    }
    
    override fun hashCode(): Int {
        return host.hostname?.hashCode() ?: 0
    }
}

