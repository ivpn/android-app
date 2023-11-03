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

import com.google.gson.Gson
import net.ivpn.core.IVPNApplication

data class V2RayConfig(
    var log: Log,
    var inbounds: List<Inbound>,
    var outbounds: List<Outbound>
) {

    data class Log(
        var loglevel: String
    )

    data class Inbound(
        var tag: String,
        var port: String,
        var listen: String,
        var protocol: String,
        var settings: Settings
    ) {
        data class Settings(
            var address: String,
            var port: Int,
            var network: String
        )
    }

    data class Outbound(
        val tag: String,
        val protocol: String,
        var settings: Settings,
        var streamSettings: StreamSettings
    ) {
        data class Settings(
            var vnext: List<Vnext>
        ) {
            data class Vnext(
                var address: String,
                var port: Int,
                var users: List<User>
            ) {
                data class User(
                    var id: String,
                    val alterId: Int,
                    val security: String
                )
            }
        }

        data class StreamSettings(
            var network: String,
            var security: String?,
            var quicSettings: QuicSettings?,
            var tlsSettings: TlsSettings?,
            var tcpSettings: TcpSettings?
        ) {
            data class QuicSettings(
                val security: String,
                val key: String,
                val header: Header
            ) {
                data class Header(
                    val type: String
                )
            }

            data class TlsSettings(
                var serverName: String
            )

            data class TcpSettings(
                val header: Header
            ) {
                data class Header(
                    val type: String,
                    val request: Request
                ) {
                    data class Request(
                        val version: String,
                        val method: String,
                        val path: List<String>,
                        val headers: Headers
                    ) {
                        data class Headers(
                            val host: List<String>,
                            val userAgent: List<String>,
                            val acceptEncoding: List<String>,
                            val connection: List<String>,
                            val pragma: String
                        )
                    }
                }
            }
        }
    }

    fun getLocalPort(): Pair<Int, Boolean> {
        if (inbounds.isNotEmpty()) {
            val port = inbounds[0].port.toIntOrNull() ?: 0
            val isTcp = inbounds[0].settings.network == "tcp"
            return Pair(port, isTcp)
        }
        return Pair(0, false)
    }

    fun setLocalPort(port: Int, isTcp: Boolean) {
        if (inbounds.isNotEmpty()) {
            inbounds[0].port = port.toString()
            inbounds[0].settings.network = if (isTcp) "tcp" else "udp"
        }
    }

    companion object {
        private fun createFromTemplate(
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String
        ): V2RayConfig {
            val config = parse("config.json")
            if (config.inbounds.isNotEmpty()) {
                config.inbounds[0].settings.address = inboundIp
                config.inbounds[0].settings.port = inboundPort
            }
            if (config.outbounds.isNotEmpty()) {
                config.outbounds[0].settings.vnext[0].address = outboundIp
                config.outbounds[0].settings.vnext[0].port = outboundPort
                config.outbounds[0].settings.vnext[0].users[0].id = outboundUserId
            }
            return config
        }

        fun createQuick(
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String,
            tlsSrvName: String
        ): V2RayConfig {
            val config = createFromTemplate(outboundIp, outboundPort, inboundIp, inboundPort, outboundUserId)
            config.outbounds.getOrNull(0)?.streamSettings?.apply {
                network = "quic"
                tcpSettings = null
                tlsSettings?.serverName = tlsSrvName
            }
            return config
        }

        fun createTcp(
            outboundIp: String,
            outboundPort: Int,
            inboundIp: String,
            inboundPort: Int,
            outboundUserId: String
        ): V2RayConfig {
            val config = createFromTemplate(outboundIp, outboundPort, inboundIp, inboundPort, outboundUserId)
            config.outbounds.getOrNull(0)?.streamSettings?.apply {
                network = "tcp"
                security = ""
                quicSettings = null
                tlsSettings = null
            }
            return config
        }

        fun from(json: String): V2RayConfig {
            return Gson().fromJson(json, V2RayConfig::class.java)
        }

        fun parse(jsonFile: String): V2RayConfig {
            var json = ""
            IVPNApplication.application.assets.open(jsonFile).apply {
                json = this.readBytes().toString(Charsets.UTF_8)
            }.close()

            return from(json)
        }
    }

    fun jsonString(): String {
        var configString = "{}"
        try {
            val data = Gson().toJson(this)
            configString = data.toString()
        } catch (_: Exception) {}
        return configString
    }

}
