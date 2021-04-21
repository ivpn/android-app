package net.ivpn.client.vpn.local;

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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.ivpn.client.R;
import net.ivpn.client.v2.MainActivity;
import net.ivpn.client.vpn.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class KillSwitchService extends VpnService implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(KillSwitchService.class);

    public static AtomicBoolean isRunning = new AtomicBoolean(false);

    private ParcelFileDescriptor tun;
    private NotificationManagerCompat notificationManager;

    private int notificationId;

    @Override
    public void onRevoke() {
        LOGGER.info("onRevoke");
        endService();
    }

    @Override
    public void onCreate() {
        LOGGER.info("onCreate");
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        notificationId = ServiceConstants.KILL_SWITCH_CHANNEL.hashCode();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        LOGGER.info("onStartCommand: action = " + action);
        LOGGER.info("onStartCommand: intent = " + intent);

        if (intent == null || action == null) {
            isRunning.set(false);
            return endService();
        }

        switch (action) {
            case START_KILL_SWITCH:
                isRunning.set(true);
                return startKillSwitch();
            case CONNECT_VPN_ACTION:
                return doSendBroadcast(CONNECT_VPN_ACTION);
            case STOP_KILL_SWITCH_ACTION:
                return doSendBroadcast(STOP_KILL_SWITCH_ACTION);
            case APP_SETTINGS_ACTION:
                closeSystemDialogs();
                return doSendBroadcast(APP_SETTINGS_ACTION);
            case STOP_KILL_SWITCH:
                isRunning.set(false);
                showKillSwitchNotification(System.currentTimeMillis());
                return endService();
            default:
                isRunning.set(false);
                showKillSwitchNotification(System.currentTimeMillis());
                return endService();
        }
    }

    private int startKillSwitch() {
        LOGGER.info("startKillSwitch");
        if (tun == null) {
            openTun();
        }
        showKillSwitchNotification(System.currentTimeMillis());

        return START_STICKY;
    }

    private int endService() {
        LOGGER.info("endService");
        notificationManager.cancel(notificationId);
        closeTun();
        stopForeground(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopSelf(STOP_FOREGROUND_REMOVE);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void closeTun() {
        LOGGER.info("closeTun");
        try {
            if (tun != null) {
                tun.close();
                tun = null;
            }
        } catch (IOException e) {
            LOGGER.error("Error while closing tun", e);
            e.printStackTrace();
        }
    }

    public ParcelFileDescriptor openTun() {
        LOGGER.info("openTun");
        Builder builder = new Builder();
        builder.setSession(getString(R.string.app_name));

        builder.addAddress(ServiceConstants.IPV4, ServiceConstants.IPV4_PREFIX);
        builder.addAddress(ServiceConstants.IPV6, ServiceConstants.IPV6_PREFIX);

        builder.addRoute(ServiceConstants.IPV4_ROUTE, ServiceConstants.IPV4_ROUTE_PREFIX);
        builder.addRoute(ServiceConstants.IPV6_ROUTE, ServiceConstants.IPV6_ROUTE_PREFIX); // unicast

        builder.setMtu(ServiceConstants.MTU);

        try {
            builder.addDisallowedApplication(getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("Error while adding disallowed application");
        }

        try {
            tun = builder.establish();
            if (tun == null)
                throw new NullPointerException("Android establish() method returned null (Really broken network configuration?)");
            return tun;
        } catch (Exception e) {
            LOGGER.error(getString(R.string.tun_open_error), e);
            return null;
        }
    }

    private void showKillSwitchNotification(long when) {
        int icon = R.drawable.ic_stat_name;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ServiceConstants.KILL_SWITCH_CHANNEL);

        builder.setContentTitle(getString(R.string.notification_ks_title));

        builder.setContentText(getString(R.string.notification_ks_content));
        builder.setOngoing(true);

        builder.setSmallIcon(icon);
        builder.setContentIntent(getGraphPendingIntent());
        builder.setColor(getResources().getColor(R.color.colorAccent));
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_ks_content))
                .setBigContentTitle(getString(R.string.notification_ks_title)));

        if (when != 0) {
            builder.setWhen(when);
            builder.setShowWhen(true);
        }

        addConnectionAction(builder);
        addSettingsAction(builder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            builder.setChannelId(ServiceConstants.KILL_SWITCH_CHANNEL);
        }

        builder.setTicker(getString(R.string.notification_ks_content));

        @SuppressWarnings("deprecation")
        Notification notification = builder.getNotification();

        int notificationId = ServiceConstants.KILL_SWITCH_CHANNEL.hashCode();

        notificationManager.notify(notificationId, notification);
        startForeground(notificationId, notification);
    }

    private void addConnectionAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, KillSwitchService.class);
        intent.setAction(ServiceConstants.CONNECT_VPN_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, ServiceConstants.IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_logo_sq,
                getString(R.string.notification_ks_connect_action), pendingIntent);
    }

    private void addDisableAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, KillSwitchService.class);
        intent.setAction(ServiceConstants.STOP_KILL_SWITCH_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, ServiceConstants.IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_cancel,
                getString(R.string.notification_ks_cancel_action), pendingIntent);
    }

    private void addSettingsAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, KillSwitchService.class);
        intent.setAction(ServiceConstants.APP_SETTINGS_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, ServiceConstants.IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_settings,
                getString(R.string.notification_ks_settings_action), pendingIntent);
    }

    PendingIntent getGraphPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return pendingIntent;
    }

    private void closeSystemDialogs() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(intent);
    }

    private int doSendBroadcast(String action) {
        LOGGER.info("doSendBroadcast: action = " + action);
        Intent vpnStatus = new Intent();
        vpnStatus.setAction(ServiceConstants.KILL_SWITCH_ACTION);
        vpnStatus.putExtra(ServiceConstants.KILL_SWITCH_ACTION_EXTRA, action);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(vpnStatus);
        return START_NOT_STICKY;
    }
}