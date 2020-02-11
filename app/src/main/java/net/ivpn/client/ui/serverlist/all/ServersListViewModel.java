package net.ivpn.client.ui.serverlist.all;

import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import net.ivpn.client.common.prefs.OnServerListUpdatedListener;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.data.model.Server;

import java.util.List;

import javax.inject.Inject;

public class ServersListViewModel extends BaseObservable {

    private static final String TAG = ServersListViewModel.class.getSimpleName();

    public final ObservableList<Server> obsServers = new ObservableArrayList<>();
    public final ObservableField<Server> forbiddenServer = new ObservableField();
    public final ObservableBoolean dataRefreshing = new ObservableBoolean();
    public final ObservableBoolean dataLoading = new ObservableBoolean();

    private OnServerListUpdatedListener listener = new OnServerListUpdatedListener() {

        @Override
        public void onSuccess(List<Server> servers, boolean isForced) {
            dataRefreshing.set(false);
            dataLoading.set(false);
            obsServers.clear();
            obsServers.addAll(servers);
        }

        @Override
        public void onError(Throwable throwable) {
            dataRefreshing.set(false);
        }

        @Override
        public void onError() {
            dataRefreshing.set(false);
        }
    };
    private Server pendingServer;
    private ServerType serverType;

    private Settings settings;
    private ServersRepository serversRepository;

    @Inject
    ServersListViewModel(Settings settings, ServersRepository serversRepository) {
        this.settings = settings;
        this.serversRepository = serversRepository;

        init();
    }

    private void init() {
        serversRepository.addOnServersListUpdatedListener(listener);
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public void start(ServerType serverType) {
        this.serverType = serverType;
        forbiddenServer.set(getForbiddenServer(serverType));
        if (isServersListExist()) {
            obsServers.clear();
            obsServers.addAll(getCachedServersList());
        } else {
            loadServers(false);
        }
    }

    void setCurrentServer(Server server) {
        if (serverType != null) {
            serversRepository.serverSelected(server, serverType);
        }
    }

    public void loadServers(final boolean isRefreshing) {
        if (isRefreshing) {
            dataRefreshing.set(true);
        } else {
            dataLoading.set(true);
        }
        serversRepository.updateServerList(isRefreshing);
    }

    public void cancel() {
        dataLoading.set(false);
        serversRepository.removeOnServersListUpdatedListener(listener);
        listener = null;
    }

    void addFavouriteServer(Server server) {
        serversRepository.addFavouritesServer(server);
        pendingServer = server;
    }

    void setSettingFastestServer() {
        serversRepository.fastestServerSelected();
    }

    boolean isFastestServerAllowed() {
        return !settings.isMultiHopEnabled();
    }

    void applyPendingAction() {
        if (pendingServer == null) {
            return;
        }

        serversRepository.removeFavouritesServer(pendingServer);
        pendingServer = null;
    }

    private List<Server> getCachedServersList() {
        return serversRepository.getServers(false);
    }

    private Server getForbiddenServer(ServerType serverType) {
        return serversRepository.getForbiddenServer(serverType);
    }

    private boolean isServersListExist() {
        return serversRepository.isServersListExist();
    }
}