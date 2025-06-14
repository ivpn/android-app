package net.ivpn.core.rest.data.model

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

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Port(_protocol: String, _portNumber: Int, _portRange: PortRange = PortRange()) {

    @SerializedName("type")
    @Expose
    var protocol: String = _protocol

    @SerializedName("port")
    @Expose
    var portNumber: Int = _portNumber

    @SerializedName("range")
    @Expose
    var range: PortRange = _portRange

    override fun equals(other: Any?): Boolean {
        if (other !is Port) return false
        return protocol == other.protocol && portNumber == other.portNumber
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }

    fun toThumbnail(): String {
        return "$protocol $portNumber"
    }

    fun next(ports: List<Port>): Port {
        val position = ports.indexOf(this)
        return if (position != ports.size - 1) ports[position + 1] else ports[0]
    }

    fun isUDP(): Boolean {
        return protocol.equals("UDP", ignoreCase = true)
    }

    companion object {

        fun from(json: String): Port {
            if (isLegacyFormat(json)) {
                return portFromLegacyFormat(json)
            }
            return Gson().fromJson(json, Port::class.java)
        }

        private fun isLegacyFormat(json: String): Boolean {
            val portJson = json.replace("WG_", "").replace("\"", "")
            return portJson.startsWith("UDP") || portJson.startsWith("TCP")
        }

        private fun portFromLegacyFormat(json: String): Port {
            val portJson = json.replace("WG_", "").replace("\"", "")
            val portItems = portJson.split("_")
            return Port(portItems.first(), portItems.last().toInt())
        }

        val defaultWgPort: Port
            get() = Port("UDP", 2049)

        val defaultOvPort: Port
            get() = Port("UDP", 2049)

        val valuesForMultiHop: List<Port>
            get() = listOf(Port("UDP", 2049), Port("TCP", 443))

    }

}

class Ports {

    @SerializedName("test")
    @Expose
    var test: List<TestConfig>? = null

    @SerializedName("wireguard")
    @Expose
    lateinit var wireguard: List<Port>

    @SerializedName("openvpn")
    @Expose
    lateinit var openvpn: List<Port>

    @SerializedName("obfs3")
    @Expose
    var obfs3: ObfsConfig? = null

    @SerializedName("obfs4")
    @Expose
    var obfs4: ObfsConfig? = null

    @SerializedName("v2ray")
    @Expose
    var v2ray: V2ray? = null

}

class PortRange {

    @SerializedName("min")
    @Expose
    var min: Int = 0

    @SerializedName("max")
    @Expose
    var max: Int = 0

}
