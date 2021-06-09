package net.ivpn.client.common.dagger;

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

import com.wireguard.android.backend.GoBackend;

import net.ivpn.client.common.alarm.GlobalWireGuardAlarm;
import net.ivpn.client.common.utils.ComponentUtil;
import net.ivpn.client.common.utils.NotificationChannelUtil;
import net.ivpn.client.v2.updates.UpdatesJobService;
import net.ivpn.client.vpn.controller.OpenVpnBehavior;
import net.ivpn.client.vpn.controller.WireGuardBehavior;

import dagger.BindsInstance;
import dagger.Component;

@ApplicationScope
@Component(modules = {ApplicationSubcomponents.class, NetworkModule.class})
public interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        ApplicationComponent create(@BindsInstance Context context);
    }

    ActivityComponent.Factory provideActivityComponent();

    ProtocolComponent.Factory provideProtocolComponent();

    GoBackend provideGoBackend();

    WireGuardBehavior getWireGuardBehavior();

    OpenVpnBehavior getOpenVpnBehavior();

    GlobalWireGuardAlarm provideGlobalWireGuardAlarm();

    NotificationChannelUtil provideNotificationUtil();

    ComponentUtil provideComponentUtil();

    void inject(UpdatesJobService service);

    void inject(GoBackend.WireGuardVpnService service);

}