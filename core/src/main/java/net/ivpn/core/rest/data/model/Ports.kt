package net.ivpn.core.rest.data.model

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 Privatus Limited.

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

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PortResponse(_protocol: String = "UDP", _portNumber: Int = 0) {

    @SerializedName("type")
    @Expose
    var protocol: String? = null

    @SerializedName("port")
    @Expose
    var portNumber: Int? = null

    fun toJson(): String {
        return Gson().toJson(this)
    }

    fun toThumbnail(): String {
        return "$protocol $portNumber"
    }

    fun next(): PortResponse {
        return PortResponse("UDP", 2049)
    }

    fun isUDP(): Boolean {
        return protocol.equals("UDP", ignoreCase = true)
    }

    companion object {

        fun from(json: String): PortResponse {
            return Gson().fromJson(json, PortResponse::class.java)
        }

        val defaultWgPort: PortResponse
            get() = PortResponse("UDP", 2049)

        val defaultOvPort: PortResponse
            get() = PortResponse("TCP", 443)

        val valuesForMultiHop: List<PortResponse>
            get() = listOf(PortResponse("UDP", 2049), PortResponse("TCP", 443))

    }

}

class Ports {

    @SerializedName("wireguard")
    @Expose
    lateinit var wireguard: List<PortResponse>

    @SerializedName("openvpn")
    @Expose
    lateinit var openvpn: List<PortResponse>

}
