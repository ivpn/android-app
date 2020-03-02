package net.ivpn.client.ui.serverlist.fastest;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.rest.data.model.Server;

import java.util.List;

import javax.inject.Inject;

public class FastestSettingViewModel {

    public final ObservableList<Server> servers = new ObservableArrayList<>();
    public final ObservableList<Server> excludedServers = new ObservableArrayList<>();
    public final OnFastestSettingChangedListener listener = new OnFastestSettingChangedListener() {
        @Override
        public void onFastestSettingItemChanged(Server server, boolean isSelected) {
            if (isSelected) {
                removeFromExcludedServerList(server);
            } else {
                addToExcludedServersList(server);
            }
        }

        @Override
        public void onAttemptRemoveLastServer() {
            navigator.showToast();
        }
    };
    private FastestSettingNavigator navigator;
    private ServersRepository serversRepository;

    @Inject
    FastestSettingViewModel(ServersRepository serversRepository) {
        this.serversRepository = serversRepository;
        init();
    }

    private void init() {
        excludedServers.clear();
        excludedServers.addAll(getExcludedServersList());
        servers.clear();
        servers.addAll(getCachedServersList());
    }

    public void setNavigator(FastestSettingNavigator navigator) {
        this.navigator = navigator;
    }

    private List<Server> getExcludedServersList() {
        return serversRepository.getExcludedServersList();
    }

    private List<Server> getCachedServersList() {
        return serversRepository.getServers(false);
    }

    private void addToExcludedServersList(Server server) {
        excludedServers.add(server);
        serversRepository.addToExcludedServersList(server);
    }

    private void removeFromExcludedServerList(Server server) {
        excludedServers.remove(server);
        serversRepository.removeFromExcludedServerList(server);
    }

}
