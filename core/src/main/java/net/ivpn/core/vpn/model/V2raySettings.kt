package net.ivpn.core.vpn.model

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

data class V2RaySettings(
    var id: String = "",
    var outboundIp: String = "",
    var outboundPort: Int = 0,
    var inboundIp: String = "",
    var inboundPort: Int = 0,
    var dnsName: String = "",
    var wireguard: List<V2RayPort> = listOf()
) {

    val tlsSrvName: String
        get() = dnsName.replace("ivpn.net", "inet-telecom.com")


    val singleHopInboundPort: Int
        get() = wireguard.firstOrNull()?.port ?: 0
}

data class V2RayPort(
    val type: String,
    val port: Int
)