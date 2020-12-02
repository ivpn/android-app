package net.ivpn.client.common;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.ivpn.client.common.updater.Update;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.ServersListResponse;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class Mapper {

    public static Server from(String json) {
        if (json == null) return null;
        return new Gson().fromJson(json, Server.class);
    }

    public static String from(Server server) {
        return new Gson().toJson(server);
    }

    public static List<Server> serverListFrom(String json) {
        if (json == null) return null;
        Type type = new TypeToken<List<Server>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static LinkedList<String> ipListFrom(String json) {
        if (json == null) return null;
        Type type = new TypeToken<LinkedList<String>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static ServersListResponse getProtocolServers(String json) {
        if (json == null) return null;
        return new Gson().fromJson(json, ServersListResponse.class);
    }

    public static String stringFrom(List<Server> servers) {
        return new Gson().toJson(servers);
    }

    public static String stringFromIps(List<String> ips) {
        return new Gson().toJson(ips);
    }

    @Nullable
    public static ErrorResponse errorResponseFrom(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return new Gson().fromJson(json, ErrorResponse.class);
        } catch (JsonSyntaxException | IllegalStateException jsonSyntaxException) {
            return null;
        }
    }

    public static Update updateFrom(String json) {
        if (json == null || json.isEmpty()) return null;
        return new Gson().fromJson(json, Update.class);
    }
}
