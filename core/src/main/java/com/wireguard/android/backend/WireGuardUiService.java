package com.wireguard.android.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.rest.data.model.ServerType;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.utils.DateUtil;
import net.ivpn.core.rest.data.model.Server;
import net.ivpn.core.v2.MainActivity;
import net.ivpn.core.v2.timepicker.TimePickerActivity;
import net.ivpn.core.vpn.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import static com.wireguard.android.backend.ConnectionStatus.CONNECTED;
import static com.wireguard.android.backend.ConnectionStatus.CONNECTING;
import static com.wireguard.android.backend.ConnectionStatus.DISCONNECTING;
import static com.wireguard.android.backend.ConnectionStatus.PAUSED;

public class WireGuardUiService extends Service implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireGuardUiService.class);
    private static final String TAG = WireGuardUiService.class.getSimpleName();

    public static AtomicBoolean isRunning = new AtomicBoolean(false);

    private CountDownTimer timer;
    private NotificationManager notificationManager;
    @Inject ServersRepository serversRepository;

    private long lastTick;
    private int notificationId;

    @Override
    public void onCreate() {
        LOGGER.info("onCreate");
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this);
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationId = ServiceConstants.VPN_CHANNEL.hashCode();
    }

    @Override
    public void onDestroy() {
        LOGGER.info("onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        LOGGER.info("onStartCommand: action = " + action);
        if (action == null) {
            isRunning.set(false);
            applyState(DISCONNECTING);
            return stop();
        }

        switch (action) {
            case DISCONNECT_ACTION: {
                return doSendActionBroadcast(DISCONNECT_ACTION);
            }
            case PAUSE_ACTION: {
                return doSendActionBroadcast(PAUSE_ACTION);
            }
            case STOP_ACTION: {
                return doSendActionBroadcast(STOP_ACTION);
            }
            case RESUME_ACTION: {
                return doSendActionBroadcast(RESUME_ACTION);
            }
            case WIREGUARD_CONNECTING: {
                isRunning.set(true);
                cancelTimer();
                return applyState(CONNECTING);
            }
            case WIREGUARD_CONNECTED: {
                isRunning.set(true);
                cancelTimer();
                return applyState(CONNECTED);
            }
            case WIREGUARD_PAUSED: {
                isRunning.set(true);
                startTimer(intent.getLongExtra(VPN_PAUSE_DURATION_EXTRA, -1));
                return applyState(PAUSED);
            }
            case WIREGUARD_DISCONNECTED: {
                isRunning.set(false);
                applyState(DISCONNECTING);
                return stop();
            }
        }

        return START_NOT_STICKY;
    }

    private int doSendActionBroadcast(String action) {
        Intent vpnAction = new Intent();
        vpnAction.setAction(NOTIFICATION_ACTION);
        vpnAction.putExtra(NOTIFICATION_ACTION_EXTRA, action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(vpnAction);
        return START_NOT_STICKY;
    }

    private void startTimer(long pauseTimer) {
        LOGGER.info("start time with duration = " + pauseTimer);
        if (pauseTimer == -1) {
            return;
        }
        lastTick = pauseTimer;
        timer = new CountDownTimer(pauseTimer, DateUtil.MINUTE) {
            @Override
            public void onTick(long millisUntilFinished) {
                LOGGER.info("Will resume in " + DateUtil.formatNotificationTimerCountDown(millisUntilFinished));
                lastTick = millisUntilFinished;
                showNotification(System.currentTimeMillis(), PAUSED);
            }

            @Override
            public void onFinish() {
                LOGGER.info("Should be resumed");
            }
        };
        timer.start();
    }

    private void cancelTimer() {
        LOGGER.info("Cancel timer = " + timer);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private int applyState(ConnectionStatus status) {
        showNotification(System.currentTimeMillis(), status);
        return START_STICKY;
    }

    private int stop() {
        LOGGER.info("stop");
        cancelTimer();
        stopForeground(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopSelf(STOP_FOREGROUND_REMOVE);
        } else {
            notificationManager.cancel(notificationId);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void showNotification(long when, ConnectionStatus status) {
        Log.d(TAG, "showNotification: ");

        int iconId = R.drawable.ic_stat_name;
        String title = getTitle(status);
        String msg = getMessage(status);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ServiceConstants.VPN_CHANNEL);

        builder.setContentTitle(title);
        builder.setContentText(msg);
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);

        builder.setSmallIcon(iconId);
        builder.setContentIntent(getContentIntent());
        builder.setColor(getResources().getColor(R.color.colorAccent));

        if (when != 0) {
            builder.setWhen(when);
            builder.setShowWhen(true);
        }

        addNotificationActions(builder, status);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(ServiceConstants.VPN_CHANNEL);
        }

        builder.setTicker(msg);

        @SuppressWarnings("deprecation")
        Notification notification = builder.getNotification();

        notificationManager.notify(notificationId, notification);
        startForeground(notificationId, notification);
    }

    private String getTitle(ConnectionStatus status) {
        switch (status) {
            case CONNECTING: {
                return getString(R.string.notification_title_connecting);
            }
            case CONNECTED: {
                return getString(R.string.notification_title_connected);
            }
            case PAUSED: {
                return getString(R.string.notification_title_paused);
            }
            default: {
                return getString(R.string.notification_title_disconnecting);
            }
        }
    }

    private String getMessage(ConnectionStatus status) {
        switch (status) {
            case PAUSED: {
                return getString(R.string.notification_resumed_in) + " " + DateUtil.formatNotificationTimerCountDown(lastTick);
            }
            case CONNECTED:
            case CONNECTING: {
                Server server = serversRepository.getCurrentServer(ServerType.ENTRY);
                return server == null ? "" : server.getDescription();
            }
            default: {
                return "";
            }
        }
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return pendingIntent;
    }

    private void addNotificationActions(NotificationCompat.Builder builder, ConnectionStatus status) {
        switch (status) {
            case CONNECTED: {
                addDisconnectAction(builder);
                addPauseAction(builder);
                break;
            }
            case PAUSED: {
                addResumeAction(builder);
                addStopAction(builder);
                break;
            }
            default: {
                addDisconnectAction(builder);
                break;
            }
        }
    }

    private void addDisconnectAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, WireGuardUiService.class);
        intent.setAction(DISCONNECT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_notifications_disconnect,
                getString(R.string.notification_disconnect), pendingIntent);
    }

    private void addPauseAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, TimePickerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_pause,
                getString(R.string.notification_pause), pendingIntent);
    }

    private void addResumeAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, WireGuardUiService.class);
        intent.setAction(RESUME_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_play,
                getString(R.string.notification_resume), pendingIntent);
    }

    private void addStopAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, WireGuardUiService.class);
        intent.setAction(STOP_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop,
                getString(R.string.notification_stop), pendingIntent);

    }
}