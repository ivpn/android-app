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

import android.content.Context;

import androidx.databinding.ObservableField;

import net.ivpn.core.R;
import net.ivpn.core.common.pinger.PingProvider;
import net.ivpn.core.rest.data.model.ServerType;
import net.ivpn.core.common.prefs.Settings;

import javax.inject.Inject;

public class ServersListCommonViewModel {

    public final ObservableField<String> title = new ObservableField<>();

    private Settings settings;
    private PingProvider pingProvider;

    @Inject
    ServersListCommonViewModel(Settings settings,
                               PingProvider pingProvider) {
        this.settings = settings;
        this.pingProvider = pingProvider;
    }

    public void onResume() {
        pingProvider.pingAll(false);
    }

    public void start(Context context, ServerType serverType) {
        boolean isMultiHopEnabled = isMultiHopEnabled();
        String titleStr;
        if (isMultiHopEnabled) {
            if (serverType.equals(ServerType.ENTRY)) {
                titleStr = context.getString(R.string.servers_list_title_entry);
            } else {
                titleStr = context.getString(R.string.servers_list_title_exit);
            }
        } else {
            titleStr = context.getString(R.string.servers_list_title);
        }
        title.set(titleStr);
    }

    private boolean isMultiHopEnabled() {
        return settings.isMultiHopEnabled();
    }
}