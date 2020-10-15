package net.ivpn.client.common.prefs;

import androidx.annotation.Nullable;

import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.IVPNApi;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.data.ServersListResponse;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.model.ServerLocation;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class ServersRepository implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServersRepository.class);

    private EnumMap<Protocol, EnumMap<ServerType, Server>> currentServers = new EnumMap<>(Protocol.class);
    private List<OnFavouriteServersChangedListener> onFavouritesChangedListeners;
    private List<OnServerListUpdatedListener> onServerListUpdatedListeners;
    private List<OnServerChangedListener> onServerChangedListeners;
    private Settings settings;
    private ProtocolController protocolController;
    private ServersPreference serversPreference;
    private HttpClientFactory httpClientFactory;

    private Request<ServersListResponse> request = null;

    @Inject
    public ServersRepository(Settings settings, HttpClientFactory httpClientFactory,
                             ProtocolController protocolController,
                             ServersPreference serversPreference) {
        this.settings = settings;
        this.protocolController = protocolController;
        this.serversPreference = serversPreference;
        this.httpClientFactory = httpClientFactory;

        init();
    }

    private void init() {
        onFavouritesChangedListeners = new ArrayList<>();
        onServerListUpdatedListeners = new ArrayList<>();
        onServerChangedListeners = new ArrayList<>();
        currentServers.put(Protocol.OPENVPN, new EnumMap<>(ServerType.class));
        currentServers.put(Protocol.WIREGUARD, new EnumMap<>(ServerType.class));
    }

    @Nullable
    public Server getCurrentServer(ServerType serverType) {
        Server server = getCurrentServers().get(serverType);
        if (server == null) {
            server = serversPreference.getCurrentServer(serverType);
            setCurrentServer(serverType, server);
        }

        if (server == null) {
            server = getDefaultServer(serverType);
            setCurrentServer(serverType, server);
        }

        return server;
    }

    public void setCurrentServer(ServerType serverType, Server server) {
        if (server == null) return;
        LOGGER.info("Set current server = " + server + " serverType = " + serverType);

        getCurrentServers().put(serverType, server);
        serversPreference.setCurrentServer(serverType, server);

        ServerType anotherServerType = ServerType.getAnotherType(serverType);
        Server serverMH = getCurrentServer(anotherServerType);
        if (!server.canBeUsedAsMultiHopWith(serverMH)) {
            serverMH = getDefaultServer(anotherServerType);
            getCurrentServers().put(anotherServerType, serverMH);
            serversPreference.setCurrentServer(anotherServerType, serverMH);
        }
    }

    @Nullable
    public Server getDefaultServer(ServerType serverType) {
        List<Server> servers = getServers(false);
        if (servers != null && !servers.isEmpty()) {
            Server anotherServer = serversPreference.getCurrentServer(ServerType.getAnotherType(serverType));
            for (Server server : servers) {
                if (server.canBeUsedAsMultiHopWith(anotherServer)) {
                    return server;
                }
            }
        }
        return null;
    }

    public boolean isServersListExist() {
        List<Server> servers = serversPreference.getServersList();
        return servers != null && !servers.isEmpty();
    }

    public List<Server> getServers(boolean isForced) {
        List<Server> servers = serversPreference.getServersList();
        if (isForced || servers == null) {
            //update server list online
            updateServerList(isForced);
            //update servers list offline
            tryUpdateServerListOffline();
            servers = serversPreference.getServersList();
        }
        return servers;
    }

    public List<ServerLocation> getLocations() {
        List<ServerLocation> locations = serversPreference.getServerLocations();
        if (locations == null) {
            updateLocations();
            locations = serversPreference.getServerLocations();
        }

        return locations;
    }

    private void updateLocations() {
        List<Server> servers = serversPreference.getServersList();
        List<ServerLocation> locations = new ArrayList<>();

        for (Server server : servers) {
            locations.add(new ServerLocation(
                    server.getCity(),
                    server.getCountryCode(),
                    server.getLatitude(),
                    server.getLongitude()
            ));
        }

        if (protocolController.getCurrentProtocol() == Protocol.OPENVPN) {
            serversPreference.putOpenVPNLocations(locations);
        } else {
            serversPreference.putWireGuardLocations(locations);
        }
    }

    public List<Server> getFavouritesServers() {
        return serversPreference.getFavouritesServersList();
    }

    public void addFavouritesServer(Server server) {
        LOGGER.info("addFavouritesServer server = " + server);
        serversPreference.addFavouriteServer(server);
        notifyFavouriteServerAdded(server);
    }

    public void removeFavouritesServer(Server server) {
        LOGGER.info("removeFavouritesServer server = " + server);
        serversPreference.removeFavouriteServer(server);
        notifyFavouriteServerRemoved(server);
    }

    private List<Server> getCachedServers() {
        return serversPreference.getServersList();
    }

    public Server getForbiddenServer(ServerType serverType) {
        boolean multiHop = settings.isMultiHopEnabled();
        if (!multiHop) return null;
        return serversPreference.getCurrentServer(ServerType.getAnotherType(serverType));
    }

    public void updateServerList(final boolean isForced) {
        LOGGER.info("Updating server list, isForced = " + isForced);
        request = new Request<>(settings, httpClientFactory, this, Request.Duration.SHORT);
        request.start(IVPNApi::getServers, new RequestListener<ServersListResponse>() {
            @Override
            public void onSuccess(ServersListResponse response) {
                LOGGER.info("Updating server list, state = SUCCESS_STR");
                LOGGER.info(response.toString());
                response.markServerTypes();
                setServerList(response.getOpenVpnServerList(), response.getWireGuardServerList());
                settings.setAntiTrackerDefaultDNS(response.getConfig().getAntiTracker().getDefault().getIp());
                settings.setAntiTrackerHardcoreDNS(response.getConfig().getAntiTracker().getHardcore().getIp());
                settings.setAntiTrackerDefaultDNSMulti(response.getConfig().getAntiTracker().getDefault().getMultihopIp());
                settings.setAntiTrackerHardcoreDNSMulti(response.getConfig().getAntiTracker().getHardcore().getMultihopIp());
                settings.setIpList(Mapper.stringFromIps(response.getConfig().getApi().getIps()));

                for (OnServerListUpdatedListener listener : onServerListUpdatedListeners) {
                    listener.onSuccess(getSuitableServers(response), isForced);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Updating server list, state = ERROR", throwable);
                for (OnServerListUpdatedListener listener : onServerListUpdatedListeners) {
                    listener.onError(throwable);
                }
            }

            @Override
            public void onError(String string) {
                LOGGER.error("Updating server list, state = ERROR", string);
                for (OnServerListUpdatedListener listener : onServerListUpdatedListeners) {
                    listener.onError();
                }
            }
        });
    }

    public void fastestServerSelected() {
        serversPreference.putSettingFastestServer(true);
        for (OnServerChangedListener listener : onServerChangedListeners) {
            listener.onServerChanged();
        }
    }

    public void serverSelected(Server server, ServerType type) {
        serversPreference.putSettingFastestServer(false);
        setCurrentServer(type, server);
        for (OnServerChangedListener listener : onServerChangedListeners) {
            listener.onServerChanged();
        }
    }

    public void tryUpdateServerListOffline() {
        LOGGER.info("Trying update server list offline from cache...");
        if (getCachedServers() != null) {
            return;
        }

        ServersListResponse response = Mapper.getProtocolServers(ServersLoader.load());
        response.markServerTypes();
        settings.setAntiTrackerDefaultDNS(response.getConfig().getAntiTracker().getDefault().getIp());
        settings.setAntiTrackerHardcoreDNS(response.getConfig().getAntiTracker().getHardcore().getIp());
        settings.setAntiTrackerDefaultDNSMulti(response.getConfig().getAntiTracker().getDefault().getMultihopIp());
        settings.setAntiTrackerHardcoreDNSMulti(response.getConfig().getAntiTracker().getHardcore().getMultihopIp());
        settings.setIpList(Mapper.stringFromIps(response.getConfig().getApi().getIps()));

        setServerList(response.getOpenVpnServerList(), response.getWireGuardServerList());
    }

    public void tryUpdateIpList() {
        if (settings.getIpList() != null) {
            return;
        }

        ServersListResponse response = Mapper.getProtocolServers(ServersLoader.load());

        settings.setAntiTrackerDefaultDNS(response.getConfig().getAntiTracker().getDefault().getIp());
        settings.setAntiTrackerHardcoreDNS(response.getConfig().getAntiTracker().getHardcore().getIp());
        settings.setAntiTrackerDefaultDNSMulti(response.getConfig().getAntiTracker().getDefault().getMultihopIp());
        settings.setAntiTrackerHardcoreDNSMulti(response.getConfig().getAntiTracker().getHardcore().getMultihopIp());
        settings.setIpList(Mapper.stringFromIps(response.getConfig().getApi().getIps()));
    }

    public void tryUpdateServerLocations() {
        LOGGER.info("tryUpdateServerLocations BEFORE");
        if (serversPreference.getServerLocations() != null) {
            return;
        }
        LOGGER.info("tryUpdateServerLocations AFTER");

        ServersListResponse response = Mapper.getProtocolServers(ServersLoader.load());
        response.markServerTypes();

        List<ServerLocation> locations = new ArrayList<>();

        for (Server server : response.getOpenVpnServerList()) {
            locations.add(new ServerLocation(
                    server.getCity(),
                    server.getCountryCode(),
                    server.getLatitude(),
                    server.getLongitude()
            ));
        }
        serversPreference.putOpenVPNLocations(locations);
        locations.clear();

        for (Server server : response.getWireGuardServerList()) {
            locations.add(new ServerLocation(
                    server.getCity(),
                    server.getCountryCode(),
                    server.getLatitude(),
                    server.getLongitude()
            ));
        }
        serversPreference.putWireGuardLocations(locations);

        setServerList(response.getOpenVpnServerList(), response.getWireGuardServerList());

        updateCurrentServersWithLocation();

        currentServers.put(Protocol.OPENVPN, new EnumMap<>(ServerType.class));
        currentServers.put(Protocol.WIREGUARD, new EnumMap<>(ServerType.class));
    }

    private void updateCurrentServersWithLocation() {
        serversPreference.updateCurrentServersWithLocation();
    }

    public void setLocationList(List<ServerLocation> openVpnLocations, List<ServerLocation> wireguardLocations) {
        LOGGER.info("Putting locations, OpenVPN locations list size = " + openVpnLocations.size() + " WireGuard = " + wireguardLocations.size());
        serversPreference.putOpenVPNLocations(openVpnLocations);
        serversPreference.putWireGuardLocations(wireguardLocations);
    }

    public void setServerList(List<Server> openvpnServers, List<Server> wireguardServers) {
        LOGGER.info("Putting servers, OpenVpn servers list size = " + openvpnServers.size() + " WireGuard = " + wireguardServers.size());
        serversPreference.putOpenVpnServerList(openvpnServers);
        serversPreference.putWireGuardServerList(wireguardServers);
    }

    public void addToExcludedServersList(Server server) {
        LOGGER.info("Add tot excluded servers list: " + server);
        serversPreference.addToExcludedServersList(server);
    }

    public void removeFromExcludedServerList(Server server) {
        LOGGER.info("Remove tot excluded servers list: " + server);
        serversPreference.removeFromExcludedServerList(server);
    }

    public List<Server> getExcludedServersList() {
        return serversPreference.getExcludedServersList();
    }

    public boolean getSettingFastestServer() {
        return serversPreference.getSettingFastestServer();
    }

    public void putFastestServerSetting(boolean isEnabled) {
        serversPreference.putSettingFastestServer(isEnabled);
    }

    public List<Server> getPossibleServersList() {
        List<Server> excludedServers = getExcludedServersList();
        List<Server> serverList = getCachedServers();

        if (serverList == null) {
            tryUpdateServerListOffline();
            serverList = getCachedServers();
        }

        List<Server> possibleServersList = new ArrayList<>();
        for (Server server : serverList) {
            if (!excludedServers.contains(server)) {
                possibleServersList.add(server);
            }
        }
        if (possibleServersList.size() == 0) {
            possibleServersList.add(getDefaultServer(ServerType.ENTRY));
        }

        return possibleServersList;
    }

    public void addFavouriteServerListener(OnFavouriteServersChangedListener listener) {
        onFavouritesChangedListeners.add(listener);
    }

    public void removeFavouriteServerListener(OnFavouriteServersChangedListener listener) {
        onFavouritesChangedListeners.remove(listener);
    }

    public void addOnServersListUpdatedListener(OnServerListUpdatedListener listener) {
        onServerListUpdatedListeners.add(listener);
    }

    public void removeOnServersListUpdatedListener(OnServerListUpdatedListener listener) {
        onServerListUpdatedListeners.remove(listener);
    }

    private Protocol getCurrentProtocolType() {
        return protocolController.getCurrentProtocol();
    }

    private EnumMap<ServerType, Server> getCurrentServers() {
        Protocol currentProtocol = getCurrentProtocolType();
        return currentServers.get(currentProtocol);
    }

    private List<Server> getSuitableServers(ServersListResponse response) {
        if (getCurrentProtocolType().equals(Protocol.WIREGUARD)) {
            return response.getWireGuardServerList();
        } else {
            return response.getOpenVpnServerList();
        }
    }

    private void notifyFavouriteServerAdded(Server server) {
        for (OnFavouriteServersChangedListener listener : onFavouritesChangedListeners) {
            listener.notifyFavouriteServerAdded(server);
        }
    }

    private void notifyFavouriteServerRemoved(Server server) {
        for (OnFavouriteServersChangedListener listener : onFavouritesChangedListeners) {
            listener.notifyFavouriteServerRemoved(server);
        }
    }
}