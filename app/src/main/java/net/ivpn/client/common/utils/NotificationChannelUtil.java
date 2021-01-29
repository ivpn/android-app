package net.ivpn.client.common.utils;

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

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.ivpn.client.R;
import net.ivpn.client.vpn.ServiceConstants;

import javax.inject.Inject;

public class NotificationChannelUtil implements ServiceConstants {

    private Context context;

    @Inject
    public NotificationChannelUtil(Context context) {
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannels() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        CharSequence channelName = context.getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(VPN_CHANNEL, channelName, importance);
        notificationManager.createNotificationChannel(channel);

        channelName = context.getString(R.string.notification_kill_switch_channel);
        channel = new NotificationChannel(KILL_SWITCH_CHANNEL, channelName, importance);
        notificationManager.createNotificationChannel(channel);

        channelName = context.getString(R.string.notification_wifi_watcher_channel);
        channel = new NotificationChannel(WIFI_WATCHER_CHANNEL, channelName, importance);
        notificationManager.createNotificationChannel(channel);

        channelName = context.getString(R.string.notification_update_channel);
        channel = new NotificationChannel(UPDATE_CHANNEL, channelName, importance);
        notificationManager.createNotificationChannel(channel);
    }
}
