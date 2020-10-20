package net.ivpn.client.common.prefs;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.SharedPreferences;

import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.model.ServerLocation;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class ServersPreference {
    private static final String CURRENT_ENTER_SERVER = "CURRENT_ENTER_SERVER";
    private static final String CURRENT_EXIT_SERVER = "CURRENT_EXIT_SERVER";
    private static final String SERVERS_LIST = "SERVERS_LIST";
    private static final String LOCATION_LIST = "LOCATION_LIST";
    private static final String FAVOURITES_SERVERS_LIST = "FAVOURITES_SERVERS_LIST";
    private static final String EXCLUDED_FASTEST_SERVERS = "EXCLUDED_FASTEST_SERVERS";
    private static final String SETTINGS_FASTEST_SERVER = "SETTINGS_FASTEST_SERVER";

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

    void putOpenVPNLocations(List<ServerLocation> locations) {
        SharedPreferences sharedPreferences = preference.getServersSharedPreferences();
        sharedPreferences.edit()
                .putString(LOCATION_LIST, ServerLocation.Companion.stringFrom(locations))
                .apply();
    }

    void putWireGuardLocations(List<ServerLocation> locations) {
        SharedPreferences sharedPreferences = preference.getWireguardServersSharedPreferences();
        sharedPreferences.edit()
                .putString(LOCATION_LIST, ServerLocation.Companion.stringFrom(locations))
                .apply();
    }

    List<ServerLocation> getServerLocations() {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        return ServerLocation.Companion.from(sharedPreferences.getString(LOCATION_LIST, null));
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

    public boolean getSettingFastestServer() {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        return sharedPreferences.getBoolean(SETTINGS_FASTEST_SERVER, true);
    }

    public void putSettingFastestServer(boolean value) {
        SharedPreferences sharedPreferences = getProperSharedPreference();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_FASTEST_SERVER, value)
                .apply();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServersRepository.class);
    //Need to be done on upgrade to version 2.0
    public void updateCurrentServersWithLocation() {
        updateCurrentServersWithLocationFor(preference.getWireguardServersSharedPreferences());
        updateCurrentServersWithLocationFor(preference.getServersSharedPreferences());
    }

    private void updateCurrentServersWithLocationFor(SharedPreferences preference) {
        List<Server> servers = Mapper.serverListFrom(preference.getString(SERVERS_LIST, null));
        if (servers == null || servers.isEmpty()) {
            return;
        }

        Server entryServer = Mapper.from(preference.getString(CURRENT_ENTER_SERVER, null));
        Server exitServer = Mapper.from(preference.getString(CURRENT_EXIT_SERVER, null));

        if (entryServer != null && Double.compare(entryServer.getLatitude(), 0) == 0
                && Double.compare(entryServer.getLongitude(), 0) == 0) {
            for (Server server: servers) {
                if (server.equals(entryServer)) {
                    LOGGER.info("Found Entry server and set correct coordinates");
                    LOGGER.info("Before = " + entryServer);
                    LOGGER.info("After  = " + server);
                    preference.edit()
                            .putString(CURRENT_ENTER_SERVER, Mapper.from(server))
                            .apply();
                    break;
                }
            }
        }

        if (exitServer != null && Double.compare(exitServer.getLatitude(), 0) == 0
                && Double.compare(exitServer.getLongitude(), 0) == 0) {
            for (Server server: servers) {
                if (server.equals(exitServer)) {
                    LOGGER.info("Found EXIT server and set correct coordinates");
                    LOGGER.info("Before = " + entryServer);
                    LOGGER.info("After  = " + server);
                    preference.edit()
                            .putString(CURRENT_EXIT_SERVER, Mapper.from(server))
                            .apply();
                    break;
                }
            }
        }
    }

    private SharedPreferences getProperSharedPreference() {
        Protocol protocol = protocolController.getCurrentProtocol();
        if (protocol.equals(Protocol.WIREGUARD)) {
            return preference.getWireguardServersSharedPreferences();
        } else {
            return preference.getServersSharedPreferences();
        }
    }
}
