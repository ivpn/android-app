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
import android.os.Build;

import net.ivpn.client.common.dagger.ApplicationComponent;
import net.ivpn.client.common.dagger.DaggerApplicationComponent;
import net.ivpn.client.vpn.ServiceConstants;

public class IVPNApplication extends Application implements ServiceConstants {

    private static IVPNApplication instance;

    public IVPNApplication() {
        instance = this;
    }

    public ApplicationComponent appComponent = DaggerApplicationComponent.factory().create(this);

    @Override
    public void onCreate() {
        super.onCreate();

//        try {
//            ProviderInstaller.installIfNeeded(getApplicationContext());
//            SSLContext sslContext;
//            sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(null, null, null);
//            sslContext.createSSLEngine();
//        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
//                | NoSuchAlgorithmException | KeyManagementException e) {
//            e.printStackTrace();
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appComponent.provideNotificationUtil().createNotificationChannels();
        }

        appComponent.provideComponentUtil().performBaseComponentsInit();
    }

    public static IVPNApplication getApplication() {
        return instance;
    }

}