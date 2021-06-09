package net.ivpn.client.v2.viewmodel;

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

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.v2.serverlist.fastest.FastestSettingNavigator;
import net.ivpn.client.v2.serverlist.fastest.OnFastestSettingChangedListener;

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
