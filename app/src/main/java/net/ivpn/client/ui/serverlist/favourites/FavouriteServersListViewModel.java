package net.ivpn.client.ui.serverlist.favourites;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import net.ivpn.client.common.prefs.OnFavouriteServersChangedListener;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.data.model.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

public class FavouriteServersListViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(FavouriteServersListViewModel.class);

    public final ObservableList<Server> obsServers = new ObservableArrayList<>();
    public final ObservableField<Server> forbiddenServer = new ObservableField<>();

    private Server pendingServer;
    private ServerType serverType;

    private ServersRepository serversRepository;
    private Settings settings;

    @Inject
    FavouriteServersListViewModel(ServersRepository serversRepository, Settings settings) {
        this.serversRepository = serversRepository;
        this.settings = settings;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public void start(ServerType serverType) {
        this.serverType = serverType;
        forbiddenServer.set(getForbiddenServer(serverType));
        obsServers.clear();
        obsServers.addAll(getFavouriteServersList());
    }

    void setCurrentServer(Server server) {
        if (serverType != null) {
            setCurrentServer(server, serverType);
        }
    }

    void removeFavouriteServer(Server server) {
        serversRepository.removeFavouritesServer(server);
        pendingServer = server;
    }

    void addFavouriteServerListener(OnFavouriteServersChangedListener listener) {
        serversRepository.addFavouriteServerListener(listener);
    }

    void removeFavouriteServerListener(OnFavouriteServersChangedListener listener) {
        serversRepository.removeFavouriteServerListener(listener);
    }

    void setSettingFastestServer() {
        serversRepository.fastestServerSelected();
    }

    boolean isFastestServerAllowed() {
        return !settings.isMultiHopEnabled();
    }

    private List<Server> getFavouriteServersList() {
        return serversRepository.getFavouritesServers();
    }

    private void addFavouriteServer(Server server) {
        serversRepository.addFavouritesServer(server);
    }

    private Server getForbiddenServer(ServerType serverType) {
        return serversRepository.getForbiddenServer(serverType);
    }

    private void setCurrentServer(Server server, ServerType serverType) {
        serversRepository.serverSelected(server, serverType);
    }

    void applyPendingAction() {
        if (pendingServer == null) {
            return;
        }

        addFavouriteServer(pendingServer);
        pendingServer = null;
    }
}