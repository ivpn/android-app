package net.ivpn.client.vpn.local;

import android.app.Notification;
import android.app.NotificationManager;
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

import net.ivpn.client.R;
import net.ivpn.client.common.utils.NetworkUtil;
import net.ivpn.client.v2.MainActivity;
import net.ivpn.client.vpn.ServiceConstants;
import net.ivpn.client.vpn.model.NetworkSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class WifiWatcherService extends Service implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(WifiWatcherService.class);

    public static AtomicBoolean isRunning = new AtomicBoolean(false);

    private WifiBroadcastReceiver receiver;
    private NotificationManager notificationManager;

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
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
            case APP_SETTINGS_ACTION:
                closeSystemDialogs();
                return doSendBroadcast();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopSelf(STOP_FOREGROUND_REMOVE);
        } else {
            notificationManager.cancel(notificationId);
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
        sendBroadcast(actionIntent);
        return START_NOT_STICKY;
    }

    private void sendWifiConnectionBroadcast(String ssid) {
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, WIFI_CHANGED_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_VALUE, ssid);
        sendBroadcast(actionIntent);
    }

    private void sendOnMobileDataBroadcast() {
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, ON_MOBILE_DATA_ACTION);
        sendBroadcast(actionIntent);
    }

    private void sendNoNetworkBroadcast() {
        Intent actionIntent = new Intent();
        actionIntent.setAction(WIFI_WATCHER_ACTION);
        actionIntent.putExtra(WIFI_WATCHER_ACTION_EXTRA, NO_NETWORK_ACTION);
        sendBroadcast(actionIntent);
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
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private void addSettingsAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, WifiWatcherService.class);
        intent.setAction(APP_SETTINGS_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_settings,
                getString(R.string.notification_ks_settings_action), pendingIntent);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
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