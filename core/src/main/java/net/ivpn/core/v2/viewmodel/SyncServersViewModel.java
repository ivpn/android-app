package net.ivpn.core.v2.viewmodel;

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

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;

import net.ivpn.core.common.prefs.OnServerListUpdatedListener;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.rest.data.model.Server;
import net.ivpn.core.v2.sync.SyncServersNavigator;

import java.util.List;

import javax.inject.Inject;

public class SyncServersViewModel extends BaseObservable {

    public final ObservableBoolean loading = new ObservableBoolean();
    public final ObservableBoolean error = new ObservableBoolean();

    private SyncServersNavigator navigator;
    private ServersRepository serversRepository;

    private OnServerListUpdatedListener listener = new OnServerListUpdatedListener() {
        @Override
        public void onSuccess(List<? extends Server> servers, boolean isForced) {
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

    public void syncServers() {
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

    public void release() {
        serversRepository.removeOnServersListUpdatedListener(listener);
        listener = null;
    }
}