package net.ivpn.core.vpn.local;

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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.common.utils.NetworkUtil;
import net.ivpn.core.v2.MainActivity;
import net.ivpn.core.vpn.ServiceConstants;
import net.ivpn.core.vpn.model.NetworkSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class WifiWatcherService extends Service implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(WifiWatcherService.class);

    public static AtomicBoolean isRunning = new AtomicBoolean(false);

    private WifiBroadcastReceiver receiver;
    private NotificationManagerCompat notificationManager;

    private int notificationId;

    public WifiWatcherService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LOGGER.info("onCreate");
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        notificationId = ServiceConstants.WIFI_WATCHER_CHANNEL.hashCode();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();

        if (intent == null || action == null) {
            isRunning.set(false);
            return endService();
        }

        LOGGER.info("onStartCommand: action = " + action);

        switch (action) {
            case START_WIFI_WATCHER:
                isRunning.set(true);
                return startWifiWatcher();
            case STOP_WIFI_WATCHER:
                isRunning.set(false);
                showNotification(System.currentTimeMillis());
                return endService();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        LOGGER.info("onDestroy");
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private int endService() {
        LOGGER.info("endService");
        stopForeground(true);
        notificationManager.cancel(notificationId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopSelf(STOP_FOREGROUND_REMOVE);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private int startWifiWatcher() {
        LOGGER.info("startWifiWatcher");
        showNotification(System.currentTimeMillis());
        registerReceiver();
        return START_STICKY;
    }

    private void closeSystemDialogs() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(intent);
    }

    private void registerReceiver() {
        if (receiver != null) {
            //receiver has already registered
            return;
        }
        receiver = new WifiBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
        LOGGER.info("registerReceiver: successfully");
    }

    private int doSendBroadcast() {
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, ServiceConstants.APP_SETTINGS_ACTION);
        LocalBroadcastManager.getInstance(IVPNApplication.application).sendBroadcast(actionIntent);
        return START_NOT_STICKY;
    }

    private void sendWifiConnectionBroadcast(String ssid) {
        LOGGER.info("sendWifiConnectionBroadcast");
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, WIFI_CHANGED_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_VALUE, ssid);
        LocalBroadcastManager.getInstance(IVPNApplication.application).sendBroadcast(actionIntent);
    }

    private void sendOnMobileDataBroadcast() {
        LOGGER.info("sendOnMobileDataBroadcast");
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, ON_MOBILE_DATA_ACTION);
        LocalBroadcastManager.getInstance(IVPNApplication.application).sendBroadcast(actionIntent);
    }

    private void sendNoNetworkBroadcast() {
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, NO_NETWORK_ACTION);
        LocalBroadcastManager.getInstance(IVPNApplication.application).sendBroadcast(actionIntent);
    }

    //ToDo Need to refactor, think how to make this code clean
    private void showNotification(long when) {
        int icon = R.drawable.ic_stat_name;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ServiceConstants.WIFI_WATCHER_CHANNEL);

        builder.setContentTitle(getString(R.string.notification_wifi_watcher_title));
        builder.setContentText(getString(R.string.notification_wifi_watcher_content));
        builder.setOngoing(true);

        builder.setSmallIcon(icon);
        builder.setContentIntent(getPendingIntent());
        builder.setColor(getResources().getColor(R.color.colorAccent));
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_wifi_watcher_content))
                .setBigContentTitle(getString(R.string.notification_wifi_watcher_title)));

        if (when != 0) {
            builder.setWhen(when);
            builder.setShowWhen(true);
        }

        addSettingsAction(builder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            builder.setChannelId(ServiceConstants.WIFI_WATCHER_CHANNEL);
        }

        builder.setTicker(getString(R.string.notification_wifi_watcher_content));

        Notification notification = builder.getNotification();

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
        startForeground(notificationId, notification);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void addSettingsAction(NotificationCompat.Builder builder) {
        Intent[] intents = new NavDeepLinkBuilder(IVPNApplication.application)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.networkProtectionFragment).createTaskStackBuilder().getIntents();
        PendingIntent pendingIntent = PendingIntent.getActivities(this, IVPN_REQUEST_CODE,
                intents, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_settings,
                getString(R.string.notification_ks_settings_action), pendingIntent);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LOGGER.info("On receive");
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                NetworkSource source = NetworkUtil.getCurrentSource(context);
                switch (source) {
                    case WIFI: {
                        String currentWiFiSsid = NetworkUtil.getCurrentWifiSsid(context);
                        if (currentWiFiSsid != null) {
                            sendWifiConnectionBroadcast(currentWiFiSsid);
                        } else {
                            sendNoNetworkBroadcast();
                        }
                        break;
                    }
                    case MOBILE_DATA: {
                        sendOnMobileDataBroadcast();
                        break;
                    }
                    case NO_NETWORK: {
                        sendNoNetworkBroadcast();
                        break;
                    }
                    case UNDEFINED: {
                        //do nothing
                        break;
                    }
                }
            }
        }
    }
}