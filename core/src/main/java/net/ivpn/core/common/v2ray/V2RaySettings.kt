package net.ivpn.core.common.v2ray

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
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

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.ivpn.core.rest.data.model.Port

class V2RaySettings {

    @SerializedName("id")
    @Expose
    var id: String = ""

    @SerializedName("wireguard")
    @Expose
    lateinit var wireguard: List<Port>

    var outboundIp: String = ""
    var outboundPort: Int = 0
    var inboundIp: String = ""
    var inboundPort: Int = 0
    var dnsName: String = ""

    val tlsSrvName: String
        get() = dnsName.replace("ivpn.net", "inet-telecom.com")

    val singleHopInboundPort: Int
        get() = wireguard.firstOrNull()?.portNumber ?: 0

}
