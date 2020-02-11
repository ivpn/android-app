package net.ivpn.client.ui.syncservers;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;

import net.ivpn.client.common.prefs.OnServerListUpdatedListener;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.rest.data.model.Server;

import java.util.List;

import javax.inject.Inject;

public class SyncServersViewModel extends BaseObservable {

    public final ObservableBoolean loading = new ObservableBoolean();
    public final ObservableBoolean error = new ObservableBoolean();

    private SyncServersNavigator navigator;
    private ServersRepository serversRepository;

    private OnServerListUpdatedListener listener = new OnServerListUpdatedListener() {

        @Override
        public void onSuccess(List<Server> servers, boolean isForced) {
            loading.set(false);
            navigator.onGetServers();
        }

        @Override
        public void onError(Throwable throwable) {
            loading.set(false);
            error.set(true);
        }

        @Override
        public void onError() {
            loading.set(false);
            error.set(true);
        }
    };

    @Inject
    SyncServersViewModel(ServersRepository serversRepository) {
        this.serversRepository = serversRepository;
    }

    public void setNavigator(SyncServersNavigator navigator) {
        this.navigator = navigator;
    }

    void syncServers() {
        if (!needUpdateServersForeground()) {
            updateServersListBackground();
            return;
        }
        updateServersListForeground();
    }

    private void updateServersListForeground() {
        serversRepository.addOnServersListUpdatedListener(listener);
        error.set(false);
        loading.set(true);
        serversRepository.updateServerList(false);
    }

    private void updateServersListBackground() {
        serversRepository.updateServerList(false);
        navigator.onGetServers();
    }

    private boolean needUpdateServersForeground() {
        return !serversRepository.isServersListExist();
    }

    void release() {
        serversRepository.removeOnServersListUpdatedListener(listener);
        listener = null;
    }
}