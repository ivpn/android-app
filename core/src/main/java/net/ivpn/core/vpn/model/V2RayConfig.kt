package net.ivpn.core.vpn.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val log: Log,
    val inbounds: List<Inbound>,
    val outbounds: List<Outbound>,
)

data class Log(
    val loglevel: String,
)

data class Inbound(
    val tag: String,
    val port: String,
    val listen: String,
    val protocol: String,
    val settings: Settings,
)

data class Settings(
    val address: String,
    val port: Long,
    val network: String,
)

data class Outbound(
    val tag: String,
    val protocol: String,
    val settings: Settings2,
    val streamSettings: StreamSettings,
)

data class Settings2(
    val vnext: List<Vnext>,
)

data class Vnext(
    val address: String,
    val port: Long,
    val users: List<User>,
)

data class User(
    val id: String,
    val alterId: Long,
    val security: String,
)

data class StreamSettings(
    val network: String,
    val security: String,
    val quicSettings: QuicSettings,
    val tlsSettings: TlsSettings,
    val tcpSettings: TcpSettings,
)

data class QuicSettings(
    val security: String,
    val key: String,
    val header: Header,
)

data class Header(
    val type: String,
)

data class TlsSettings(
    val serverName: String,
)

data class TcpSettings(
    val header: Header2,
)

data class Header2(
    val type: String,
    val request: Request,
)

data class Request(
    val version: String,
    val method: String,
    val path: List<String>,
    val headers: Headers,
)

data class Headers(
    @SerializedName("Host")
    val host: List<String>,
    @SerializedName("User-Agent")
    val userAgent: List<String>,
    @SerializedName("Accept-Encoding")
    val acceptEncoding: List<String>,
    @SerializedName("Connection")
    val connection: List<String>,
    @SerializedName("Pragma")
    val pragma: String,
)
