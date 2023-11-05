package net.ivpn.core.common

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
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
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import net.ivpn.core.common.v2ray.V2RaySettings
import net.ivpn.core.rest.data.ServersListResponse
import net.ivpn.core.rest.data.model.AntiTracker
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.wireguard.ErrorResponse
import java.util.*

object Mapper {
    fun from(json: String?): Server? {
        return if (json == null) null else Gson().fromJson(json, Server::class.java)
    }

    fun from(server: Server?): String {
        return Gson().toJson(server)
    }

    fun serverListFrom(json: String?): MutableList<Server>? {
        if (json == null) return null
        val type = object : TypeToken<List<Server>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun ipListFrom(json: String?): LinkedList<String> {
        if (json == null) {
            return LinkedList()
        }
        val type = object : TypeToken<LinkedList<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun getProtocolServers(json: String?): ServersListResponse? {
        return if (json == null) null else Gson().fromJson(json, ServersListResponse::class.java)
    }

    fun stringFrom(servers: List<Server?>?): String {
        return Gson().toJson(servers)
    }

    fun stringFromPorts(ports: List<Port?>?): String {
        return Gson().toJson(ports)
    }

    fun portsFrom(json: String?): List<Port> {
        val type = object : TypeToken<List<Port>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun antiTrackerListFrom(json: String?): List<AntiTracker> {
        val type = object : TypeToken<List<AntiTracker>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun stringFromAntiTrackerList(list: List<AntiTracker?>?): String {
        return Gson().toJson(list)
    }

    fun antiTrackerFrom(json: String?): AntiTracker {
        val type = object : TypeToken<AntiTracker>() {}.type
        return Gson().fromJson(json, type)
    }

    fun stringFromAntiTracker(dns: AntiTracker?): String {
        return Gson().toJson(dns)
    }

    fun v2raySettingsFrom(json: String?): V2RaySettings {
        val type = object : TypeToken<V2RaySettings>() {}.type
        return Gson().fromJson(json, type)
    }

    fun stringFromV2raySettings(settings: V2RaySettings?): String {
        return Gson().toJson(settings)
    }

    fun stringFromIps(ips: List<String>?): String? {
        if (ips == null) return null
        return Gson().toJson(ips)
    }

    @JvmStatic
    fun errorResponseFrom(json: String?): ErrorResponse? {
        return if (json == null || json.isEmpty()) null else try {
            Gson().fromJson(json, ErrorResponse::class.java)
        } catch (jsonSyntaxException: JsonSyntaxException) {
            null
        } catch (jsonSyntaxException: IllegalStateException) {
            null
        }
    }
}