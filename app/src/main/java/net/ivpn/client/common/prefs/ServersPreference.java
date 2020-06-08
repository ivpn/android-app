package net.ivpn.client.common.prefs;

import android.content.SharedPreferences;

import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class ServersPreference {
    private static final String CURRENT_ENTER_SERVER = "CURRENT_ENTER_SERVER";
    private static final String CURRENT_EXIT_SERVER = "CURRENT_EXIT_SERVER";
    private static final String SERVERS_LIST = "SERVERS_LIST";
    private static final String FAVOURITES_SERVERS_LIST = "FAVOURITES_SERVERS_LIST";
    private static final String EXCLUDED_FASTEST_SERVERS = "EXCLUDED_FASTEST_SERVERS";

    private Preference preference;
    private ProtocolController protocolController;

    @Inject
    ServersPreference(Preference preference, ProtocolController protocolController) {
        this.preference = preference;
        this.protocolController = protocolController;
    }

    public void setCurrentServer(ServerType serverType, Server server) {
        if (serverType == null || server == null) return;
        SharedPreferences sharedPreferences = getProperSharedPreference();
        String serverKey = serverType.equals(ServerType.ENTRY) ? CURRENT_ENTER_SERVER : CURRENT_EXIT_SERVER;
        sharedPreferences.edit()
                .putString(serverKey, Mapper.from(server))
                .apply();
    }

    void putOpenVpnServerList(List<Server> servers) {
        SharedPreferences sharedPreferences = preference.getServersSharedPreferences();
        sharedPreferences.edit()
                .putString(SERVERS_LIST, Mapper.stringFrom(servers))
                .apply();
    }

    void putWireGuardServerList(List<Server> servers) {
        SharedPreferences sharedPreferences = preference.getWireguardServersSharedPreferences();
        sharedPreferences.edit()
                .putString(SERVERS_LIST, Mapper.stringFrom(servers))
                .apply();
    }

    List<Server> getServersList() {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        return Mapper.serverListFrom(sharedPreferences.getString(SERVERS_LIST, null));
    }

    public Server getCurrentServer(ServerType serverType) {
        if (serverType == null) return null;
        SharedPreferences sharedPreferences = getProperSharedPreference();
        String serverKey = serverType.equals(ServerType.ENTRY) ? CURRENT_ENTER_SERVER : CURRENT_EXIT_SERVER;
        return Mapper.from(sharedPreferences.getString(serverKey, null));
    }

    List<Server> getFavouritesServersList() {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        List<Server> servers = Mapper.serverListFrom(sharedPreferences.getString(FAVOURITES_SERVERS_LIST, null));
        return servers != null ? servers : new ArrayList<Server>();
    }

    void addFavouriteServer(Server server) {
        List<Server> servers = getFavouritesServersList();
        if (server == null || servers.contains(server)) {
            return;
        }
        servers.add(server);

        SharedPreferences sharedPreferences = getProperSharedPreference();
        sharedPreferences.edit()
                .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(servers))
                .apply();
    }

    void removeFavouriteServer(Server server) {
        List<Server> servers = getFavouritesServersList();
        servers.remove(server);

        SharedPreferences sharedPreferences = getProperSharedPreference();
        sharedPreferences.edit()
                .putString(FAVOURITES_SERVERS_LIST, Mapper.stringFrom(servers))
                .apply();
    }

    void addToExcludedServersList(Server server) {
        List<Server> servers = getExcludedServersList();
        if (server == null || servers.contains(server)) {
            return;
        }
        servers.add(server);

        SharedPreferences sharedPreferences = getProperSharedPreference();
        sharedPreferences.edit()
                .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(servers))
                .apply();
    }

    void removeFromExcludedServerList(Server server) {
        List<Server> servers = getExcludedServersList();
        servers.remove(server);

        SharedPreferences sharedPreferences = getProperSharedPreference();
        sharedPreferences.edit()
                .putString(EXCLUDED_FASTEST_SERVERS, Mapper.stringFrom(servers))
                .apply();
    }

    List<Server> getExcludedServersList() {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        List<Server> servers = Mapper.serverListFrom(sharedPreferences.getString(EXCLUDED_FASTEST_SERVERS, null));
        return servers != null ? servers : new ArrayList<>();
    }

    private SharedPreferences getProperSharedPreference() {
        Protocol protocol = protocolController.getCurrentProtocol();
        if (protocol.equals(Protocol.WireGuard)) {
            return preference.getWireguardServersSharedPreferences();
        } else {
            return preference.getServersSharedPreferences();
        }
    }
}
