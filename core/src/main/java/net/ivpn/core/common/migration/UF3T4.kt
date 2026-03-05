package net.ivpn.core.common.migration

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

import net.ivpn.core.common.prefs.ServersPreference
import org.slf4j.LoggerFactory

/**
 * Migration from version 3 to 4.
 * Migrates per-protocol favorites (OpenVPN and WireGuard) to unified favorites storage.
 * The unified favorites use protocol-agnostic identifiers (gateway prefix for locations,
 * dns_name for hosts) which allows favorites to be shared across all VPN protocols.
 */
class UF3T4(
    private val serversPreference: ServersPreference
) : Update {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UF3T4::class.java)
    }

    override fun update() {
        LOGGER.info("Migrating favorites to unified storage")
        serversPreference.migrateOldFavouritesToUnified()
        serversPreference.migrateLegacyHostFavouritesToUnified()
        LOGGER.info("Favorites migration completed")
    }
}

