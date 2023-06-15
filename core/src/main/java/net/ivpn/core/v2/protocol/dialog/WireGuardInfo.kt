package net.ivpn.core.v2.protocol.dialog

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

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

import net.ivpn.core.common.utils.DateUtil

class WireGuardInfo(
        val publicKey: String,
        val ipAddress: String,
        val presharedKey: String?,
        private val lastGeneratedTime: Long,
        private val regenerationPeriod: Long
) {
    val ipAddressUI: String
    get() {
        return if (publicKey.isBlank()) {
            ""
        } else {
            ipAddress
        }
    }

    val lastGenerated: String
        get() {
            return if (publicKey.isBlank()) {
                ""
            } else {
                DateUtil.formatWireGuardKeyDate(lastGeneratedTime)
            }
        }
    val nextRegenerationDate: String
        get() {
            return if (publicKey.isBlank()) {
                ""
            } else {
                DateUtil.formatWireGuardKeyDate(lastGeneratedTime + regenerationPeriod * DateUtil.DAY)
            }
        }
    val validUntil: String
        get() {
            return if (publicKey.isBlank()) {
                ""
            } else {
                DateUtil.formatWireGuardKeyDate(lastGeneratedTime + 40 * DateUtil.DAY)
            }
        }
}