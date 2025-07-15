package net.ivpn.core.vpn.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

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

data class V2RayConfig(
    var log: Log,
    var inbounds: MutableList<Inbound>,
    var outbounds: MutableList<Outbound>
) {

    fun getLocalPort(): LocalPortResult {
        if (inbounds.isEmpty()) {
            return LocalPortResult(0, false)
        }
        val port = inbounds[0].port.toIntOrNull() ?: 0
        val isTcp = inbounds[0].settings.network == "tcp"
        return LocalPortResult(port, isTcp)
    }

    fun setLocalPort(port: Int, isTcp: Boolean) {
        if (inbounds.isEmpty()) return
        inbounds[0].port = port.toString()
        inbounds[0].settings.network = if (isTcp) "tcp" else "udp"
    }

    fun isValid(): String? {
        val port = getLocalPort().port
        if (port == 0 || inbounds[0].settings.network.isEmpty()) {
            return "inbounds[0].port or inbounds[0].settings.network has invalid value"
        }
        if (inbounds[0].settings.address.trim().isEmpty()) {
            return "inbounds[0].settings.address is empty"
        }
        if (inbounds[0].settings.port == 0L) {
            return "inbounds[0].settings.port is empty"
        }
        if (outbounds[0].settings.vnext[0].address.trim().isEmpty()) {
            return "outbounds[0].settings.vnext[0].address is empty"
        }
        if (outbounds[0].settings.vnext[0].port == 0L) {
            return "outbounds[0].settings.vnext[0].port is empty"
        }
        if (outbounds[0].settings.vnext[0].users[0].id.trim().isEmpty()) {
            return "outbounds[0].settings.vnext[0].users[0].id is empty"
        }

        return null
    }

    fun jsonString(): String {
        return try {
            Gson().toJson(this)
        } catch (e: Exception) {
            "{}"
        }
    }

    companion object {
        fun fromJson(json: String): V2RayConfig {
            return Gson().fromJson(json, V2RayConfig::class.java)
        }

        fun createFromTemplate(
            context: Context,
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String
        ): V2RayConfig {
            val jsonStr = context.assets.open("config.json").bufferedReader().use { it.readText() }
            val config = fromJson(jsonStr)

            // Configure inbound (local proxy) - port will be set dynamically
            config.inbounds[0].listen = "127.0.0.1"  // Always listen on localhost
            config.inbounds[0].settings.address = inboundIp
            config.inbounds[0].settings.port = inboundPort.toLong()

            // Configure outbound (to V2Ray server)
            config.outbounds[0].settings.vnext[0].address = outboundIp
            config.outbounds[0].settings.vnext[0].port = outboundPort.toLong()
            config.outbounds[0].settings.vnext[0].users[0].id = outboundUserId

            return config
        }

        fun createQUIC(
            context: Context,
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String,
            tlsSrvName: String
        ): V2RayConfig {
            val config = createFromTemplate(context, outboundIp, outboundPort, inboundIp, inboundPort, outboundUserId)
            with(config.outbounds[0].streamSettings) {
                network = "quic"
                tcpSettings = null
                tlsSettings?.serverName = tlsSrvName
            }
            return config
        }

        fun createTcp(
            context: Context,
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String
        ): V2RayConfig {
            val config = createFromTemplate(context, outboundIp, outboundPort, inboundIp, inboundPort, outboundUserId)
            with(config.outbounds[0].streamSettings) {
                network = "tcp"
                security = ""
                quicSettings = null
                tlsSettings = null
            }
            return config
        }
    }
}

data class LocalPortResult(val port: Int, val isTcp: Boolean)

data class Log(
    var loglevel: String
)

data class Inbound(
    var tag: String,
    var port: String,
    var listen: String,
    var protocol: String,
    var settings: Settings
)

data class Settings(
    var address: String,
    var port: Long,
    var network: String
)

data class Outbound(
    var tag: String,
    var protocol: String,
    var settings: Settings2,
    var streamSettings: StreamSettings
)

data class Settings2(
    var vnext: MutableList<Vnext>
)

data class Vnext(
    var address: String,
    var port: Long,
    var users: MutableList<User>
)

data class User(
    var id: String,
    var alterId: Long,
    var security: String
)

data class StreamSettings(
    var network: String,
    var security: String,
    var quicSettings: QuicSettings?,
    var tlsSettings: TlsSettings?,
    var tcpSettings: TcpSettings?
)

data class QuicSettings(
    var security: String,
    var key: String,
    var header: QuicHeader
)

data class QuicHeader(
    var type: String
)

data class TlsSettings(
    var serverName: String
)

data class TcpSettings(
    var header: TcpHeader
)

data class TcpHeader(
    var type: String,
    var request: Request
)

data class Request(
    var version: String,
    var method: String,
    var path: List<String>,
    var headers: Headers
)

data class Headers(
    @SerializedName("Host")
    var host: List<String>,
    @SerializedName("User-Agent")
    var userAgent: List<String>,
    @SerializedName("Accept-Encoding")
    var acceptEncoding: List<String>,
    @SerializedName("Connection")
    var connection: List<String>,
    @SerializedName("Pragma")
    var pragma: String
)