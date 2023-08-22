package net.ivpn.core.vpn;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import androidx.annotation.Nullable;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.vpn.controller.VpnBehaviorController;
import net.ivpn.core.vpn.local.PermissionActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class AlwaysOnVpnService extends VpnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlwaysOnVpnService.class);

    @Inject
    VpnBehaviorController vpnBehaviorController;

    @Override
    public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this);
        if (intent == null || intent.getComponent() == null || !intent.getComponent().getPackageName().equals(getPackageName())) {
            LOGGER.info("Service started by Always-on VPN feature");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                vpnBehaviorController.connectActionByRules();
            } else {
                Intent vpnIntent = new Intent(this, PermissionActivity.class);
                vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(vpnIntent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
