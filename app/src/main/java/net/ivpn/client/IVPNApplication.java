package net.ivpn.client;

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

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.NonNull;

import net.ivpn.client.common.dagger.ApplicationComponent;
import net.ivpn.client.common.dagger.DaggerApplicationComponent;
import net.ivpn.client.common.utils.NetworkUtil;
import net.ivpn.client.vpn.ServiceConstants;
import net.ivpn.client.vpn.openvpn.IVPNService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IVPNApplication extends Application implements ServiceConstants {

    private static IVPNApplication instance;

    public IVPNApplication() {
        instance = this;
    }

    public ApplicationComponent appComponent = DaggerApplicationComponent.factory().create(this);

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appComponent.provideNotificationUtil().createNotificationChannels();
        }

        appComponent.provideComponentUtil().performBaseComponentsInit();
    }

    public static IVPNApplication getApplication() {
        return instance;
    }

}