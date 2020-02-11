package net.ivpn.client.common.utils;

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
