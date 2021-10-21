package net.ivpn.core.vpn.openvpn;

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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.rest.data.model.ServerType;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.common.utils.DateUtil;
import net.ivpn.core.v2.MainActivity;
import net.ivpn.core.v2.timepicker.TimePickerActivity;
import net.ivpn.core.vpn.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.CIDRIP;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.DeviceStateReceiver;
import de.blinkt.openvpn.core.IOpenVpnService;
import de.blinkt.openvpn.core.OpenVPNManagement;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.OpenVpnManagementThread;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

public class IVPNService extends VpnService implements VpnStatus.StateListener, Handler.Callback,
        IOpenVpnService, ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(IVPNService.class);

    public static AtomicBoolean isRunning = new AtomicBoolean(false);

    private NotificationManager notificationManager;
    private Thread processThread = null;
    private VpnProfile profile;
    private DeviceStateReceiver deviceStateReceiver;
    private OpenVPNManagement management;
    private final Object processLock = new Object();
    private Runnable openVPNThread;
    private CountDownTimer timer;

    @Inject
    Settings settings;
    @Inject
    ServersRepository serversRepository;
    @Inject
    ProfileManager profileManager;
    @Inject
    ServiceConfiguration serviceConfiguration;

    private int notificationId;
    private long lastTick;
    private boolean starting = false;
    private boolean isPaused;
    private boolean isReconnecting;

    @Override
    public void onCreate() {
        super.onCreate();
        LOGGER.info("onCreate");
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationId = ServiceConstants.VPN_CHANNEL.hashCode();
        showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new VpnServiceBinder();
    }

    @Override
    public void onRevoke() {
        LOGGER.error(getString(R.string.permission_revoked));
        management.stopVPN(false);
        endVpnService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.info("onStartCommand");

        VpnStatus.addStateListener(this);

        if (intent != null && DISCONNECT_ACTION.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: DISCONNECT_ACTION");
            doSendActionBroadcast(DISCONNECT_ACTION);
            return START_NOT_STICKY;
        }

        if (intent != null && PAUSE_ACTION.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: PAUSE_ACTION");
            doSendActionBroadcast(PAUSE_ACTION);
            return START_NOT_STICKY;
        }

        if (intent != null && STOP_ACTION.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: STOP_ACTION");
            doSendActionBroadcast(STOP_ACTION);
            return START_NOT_STICKY;
        }

        if (intent != null && RESUME_ACTION.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: RESUME_ACTION");
            doSendActionBroadcast(RESUME_ACTION);
            return START_NOT_STICKY;
        }

        isReconnecting = false;

        if (intent != null && DISCONNECT_VPN.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: DISCONNECT_VPN");
            isRunning.set(false);
            //ToDo think about better solution

            showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
            isPaused = false;
            if (getManagement() != null) {
                getManagement().stopVPN(false);
            }
            endVpnService();
            return START_NOT_STICKY;
        }

        if (intent != null && RECONNECTING_VPN.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: RECONNECTING_VPN");
            isRunning.set(true);
            isReconnecting = true;
        }

        if (intent != null && STOP_VPN.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: STOP_VPN");
            isRunning.set(false);
            //ToDo think about better solution
            showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
            isPaused = false;
            if (getManagement() != null) {
                getManagement().stopVPN(false);
            }
            endVpnService();
            return START_NOT_STICKY;
        }

        if (intent != null && PAUSE_VPN.equals(intent.getAction())) {
            LOGGER.info("onStartCommand: PAUSE_VPN");
            isRunning.set(true);
            isPaused = true;
            showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
            startTimer(intent.getLongExtra(VPN_PAUSE_DURATION_EXTRA, -1));
            if (getManagement() != null) {
                getManagement().stopVPN(false);
            }

            return START_NOT_STICKY;
        }

        profile = profileManager.getVpnProfile();
        if (profile == null) {
            isRunning.set(false);
            showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
            endVpnService();
            return START_NOT_STICKY;
        } else if (intent == null) {
//          The intent is null when we are set as always-on or the service has been restarted.
            profile.checkForRestart(this);
        }

        isRunning.set(true);
        /* start the OpenVPN process itself in a background thread */
        new Thread(this::startOpenVPN).start();

        isPaused = false;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LOGGER.info("onDestroy");
        synchronized (processLock) {
            if (processThread != null) {
                management.stopVPN(false);
            }
        }

        unregisterDeviceStateReceiver();
        // Just in case unregister for state
        VpnStatus.removeStateListener(this);
    }

    public void processDied() {
        LOGGER.info("processDied");
        if (!isPaused && !isReconnecting) {
            endVpnService();
        }
        isReconnecting = false;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean protectSocket(int socket) {
        return protect(socket);
    }

    private void closeSystemDialogs() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(intent);
    }

    private void endVpnService() {
        synchronized (processLock) {
            processThread = null;
        }
        unregisterDeviceStateReceiver();
        openVPNThread = null;
        LOGGER.info("endVpnService: starting");
        cancelTimer();
        if (!starting) {
            LOGGER.info("endVpnService: stopForeground");
            stopForeground(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LOGGER.info("Stop service");
                stopSelf(STOP_FOREGROUND_REMOVE);
            } else {
                LOGGER.info("Stop service");
                notificationManager.cancel(notificationId);
                stopSelf();
            }
        }
    }

    private void startTimer(long pauseTimer) {
        LOGGER.info("Start timer");
        if (pauseTimer == -1) {
            return;
        }
        lastTick = pauseTimer;
        timer = new CountDownTimer(pauseTimer, DateUtil.MINUTE) {
            @Override
            public void onTick(long millisUntilFinished) {
                lastTick = millisUntilFinished;
                showNotification(System.currentTimeMillis(), VpnStatus.lastLevel);
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    private void cancelTimer() {
        LOGGER.info("Cancel timer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void showNotification(long when, ConnectionStatus status) {
        LOGGER.info("showNotification: status = " + status);
        int icon = R.drawable.ic_stat_name;
        String title;
        boolean isMultiHopEnabled = settings.isMultiHopEnabled();

        if (isPaused) {
            title = getString(R.string.notification_paused);
        } else {
            title = getNotificationTitleMsg(status, isMultiHopEnabled);
        }

        String msg = "";
        if (isPaused) {
            msg = this.getString(R.string.notification_resumed_in) + " " + DateUtil.formatNotificationTimerCountDown(lastTick);
        } else {
            if (profile != null) {
                msg = profile.mName;
                if (isMultiHopEnabled) {
                    msg += " -> " + serversRepository.getCurrentServer(ServerType.EXIT).getDescription();
                }
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ServiceConstants.VPN_CHANNEL);

        builder.setContentTitle(title);

        builder.setContentText(msg);
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);

        builder.setSmallIcon(icon);
        builder.setContentIntent(getGraphPendingIntent());
        builder.setColor(getResources().getColor(R.color.colorAccent));

        if (when != 0) {
            builder.setWhen(when);
            builder.setShowWhen(true);
        }

        if (status.equals(ConnectionStatus.LEVEL_NOTCONNECTED)) {
            if (isPaused) {
                addResumeAction(builder);
                addStopAction(builder);
            }
        } else {
            addDisconnectAction(builder);
            if (status.equals(ConnectionStatus.LEVEL_CONNECTED)) {
                addPauseAction(builder);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            builder.setChannelId(ServiceConstants.VPN_CHANNEL);
            if (profile != null)
                //noinspection NewApi
                builder.setShortcutId(profile.getUUIDString());
        }

        builder.setTicker(msg);

        @SuppressWarnings("deprecation")
        Notification notification = builder.getNotification();

        notificationManager.notify(notificationId, notification);
        startForeground(notificationId, notification);
    }

    private void addDisconnectAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, IVPNService.class);
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
        Intent intent = new Intent(this, IVPNService.class);
        intent.setAction(RESUME_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_play,
                getString(R.string.notification_resume), pendingIntent);
    }

    private void addStopAction(NotificationCompat.Builder builder) {
        Intent intent = new Intent(this, IVPNService.class);
        intent.setAction(STOP_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, IVPN_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop,
                getString(R.string.notification_stop), pendingIntent);
    }

    private String getNotificationTitleMsg(ConnectionStatus status, boolean isMultiHopEnabled) {
        String subtitle = isMultiHopEnabled ? " Multi-Hop" : "";
        switch (status) {
            case LEVEL_CONNECTED:
                return getString(R.string.notification_status_connected) + subtitle;
            case LEVEL_AUTH_FAILED:
            case LEVEL_NONETWORK:
            case LEVEL_NOTCONNECTED:
                return getString(R.string.notification_status_not_connected);
            case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
                return getString(R.string.notification_status_server_not_reply) + subtitle;
            case LEVEL_WAITING_FOR_USER_INPUT:
                return getString(R.string.notification_status_waiting_for_user_input);
            case LEVEL_START:
            case LEVEL_CONNECTING_SERVER_REPLIED:
                return getString(R.string.notification_status_server_not_reply) + subtitle;
            case LEVEL_VPNPAUSED:
                return getString(R.string.notification_status_paused);
            case UNKNOWN_LEVEL:
            default:
                return getString(R.string.notification_status_not_connected);
        }
    }

    synchronized void registerDeviceStateReceiver(OpenVPNManagement magnagement) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        deviceStateReceiver = new DeviceStateReceiver(magnagement);

        registerReceiver(deviceStateReceiver, filter);
    }

    synchronized void unregisterDeviceStateReceiver() {
        if (deviceStateReceiver != null)
            try {
                this.unregisterReceiver(deviceStateReceiver);
            } catch (IllegalArgumentException exception) {
                // I don't know why  this happens:
                // java.lang.IllegalArgumentException: Receiver not registered: de.blinkt.openvpn.NetworkSateReceiver@41a61a10
                // Ignore for now ...
                exception.printStackTrace();
            }
        deviceStateReceiver = null;
    }

    private void startOpenVPN() {
        LOGGER.info(getString(R.string.building_configration));
        VpnStatus.updateStateString("VPN_GENERATE_CONFIG", ConnectionStatus.LEVEL_START);

        try {
            LOGGER.info("startOpenVPN: profile " + profile.getName());
            profile.writeConfigFile(this);
        } catch (IOException e) {
            LOGGER.error("Error writing config file", e);
            endVpnService();
            return;
        }
        String nativeLibraryDirectory = getApplicationInfo().nativeLibraryDir;

        // Write OpenVPN binary
        String[] argv = VPNLaunchHelper.buildOpenvpnArgv(this);

        // Set a flag that we are starting a new VPN
        starting = true;
        // Stop the previous session by interrupting the thread.

        stopOldOpenVPNProcess();
        // An old running VPN should now be exited
        starting = false;

        // start a Thread that handles incoming messages of the managment socket
        OpenVpnManagementThread ovpnManagementThread = new OpenVpnManagementThread(profile, this);
        if (ovpnManagementThread.openManagementInterface(this)) {

            Thread mSocketManagerThread = new Thread(ovpnManagementThread, "OpenVPNManagementThread");
            mSocketManagerThread.start();
            management = ovpnManagementThread;
            LOGGER.info("started Socket Thread");
        } else {
            endVpnService();
            return;
        }

        Runnable processThread = new OpenVPNThread(this, argv, nativeLibraryDirectory);
        openVPNThread = processThread;

        synchronized (processLock) {
            this.processThread = new Thread(processThread, "OpenVPNProcessThread");
            this.processThread.start();
        }

        new Handler(getMainLooper()).post(() -> {
                    if (deviceStateReceiver != null)
                        unregisterDeviceStateReceiver();

                    registerDeviceStateReceiver(management);
                }
        );
    }

    private void stopOldOpenVPNProcess() {
        LOGGER.info("stopOldOpenVPNProcess");
        if (management != null) {
            if (openVPNThread != null)
                ((OpenVPNThread) openVPNThread).setReplaceConnection();
            if (management.stopVPN(true)) {
                // an old was asked to exit, wait 1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        forceStopOpenVpnProcess();
    }

    public void forceStopOpenVpnProcess() {
        LOGGER.info("forceStopOpenVpnProcess");
        synchronized (processLock) {
            if (processThread != null) {
                processThread.interrupt();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }

    public ParcelFileDescriptor openTun() {
        Builder builder = new Builder();
        profile.mAllowLocalLAN = serviceConfiguration.isLocalBypassEnabled();

        LOGGER.info(getString(R.string.last_openvpn_tun_config));
        if (profile.mAllowLocalLAN) {
            allowAllAFFamilies(builder);
        }

        serviceConfiguration.fillBuilder(this, builder, profile);

        String session = profile.mName;
        builder.setSession(serviceConfiguration.getSessionFormatted(this, session));
        builder.setConfigureIntent(getGraphPendingIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false);
        }
        builder.setBlocking(true);

        try {
            ParcelFileDescriptor tun = builder.establish();
            if (tun == null)
                throw new NullPointerException("Android establish() method returned null (Really broken network configuration?)");
            return tun;
        } catch (Exception e) {
            LOGGER.error(getString(R.string.tun_open_error));
            LOGGER.error(getString(R.string.error), e);
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void allowAllAFFamilies(Builder builder) {
        builder.allowFamily(OsConstants.AF_INET);
        builder.allowFamily(OsConstants.AF_INET6);
    }

    PendingIntent getGraphPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return pendingIntent;
    }

    public void addDNS(String dns) {
        serviceConfiguration.addDNS(dns);
    }

    public void setDomain(String domain) {
        serviceConfiguration.setDomain(domain);
    }

    public void addRoute(CIDRIP route) {
        serviceConfiguration.addRoute(route);
    }

    public void addRoute(String dest, String mask, String gateway, String device) {
        serviceConfiguration.addRoute(dest, mask, gateway, device);
    }

    public void addRoutev6(String network, String device) {
        serviceConfiguration.addRouteV6(network, device);
    }

    public void setMtu(int mtu) {
        serviceConfiguration.setMtu(mtu);
    }

    public void setLocalIP(String local, String netmask, int mtu, String mode) {
        serviceConfiguration.setLocalIP(local, netmask, mtu, mode);
    }

    public void setLocalIPv6(String ipv6addr) {
        serviceConfiguration.setLocalIPv6(ipv6addr);
    }

    private void doSendBroadcast(ConnectionStatus level) {
        Intent vpnStatus = new Intent();
        vpnStatus.setAction(VPN_STATUS);
        vpnStatus.putExtra(VPN_EXTRA_STATUS, level.toString());
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(vpnStatus);
    }

    private void doSendActionBroadcast(String action) {
        Intent vpnAction = new Intent();
        vpnAction.setAction(NOTIFICATION_ACTION);
        vpnAction.putExtra(NOTIFICATION_ACTION_EXTRA, action);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(vpnAction);
    }

    //ToDo check if this code is valid
    @Override
    public boolean handleMessage(Message msg) {
        Runnable r = msg.getCallback();
        if (r != null) {
            r.run();
            return true;
        } else {
            return false;
        }
    }

    public OpenVPNManagement getManagement() {
        return management;
    }

    @Override
    public String getTunReopenStatus() {
        return serviceConfiguration.getTunReopenStatus();
    }

    @Override
    public void updateState(ConnectionStatus level) {
        doSendBroadcast(level);

        showNotification(System.currentTimeMillis(), level);
    }

    public class VpnServiceBinder extends Binder {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            if (code == IBinder.LAST_CALL_TRANSACTION) {
                onRevoke();
                return true;
            }
            return false;
        }
    }
}