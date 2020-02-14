package net.ivpn.client.common;

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
