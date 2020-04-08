package net.ivpn.client.vpn.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import com.wireguard.android.backend.WireGuardUiService;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.pinger.OnFastestServerDetectorListener;
import net.ivpn.client.common.pinger.PingProvider;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.utils.DateUtil;
import net.ivpn.client.common.utils.ToastUtil;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.ui.connect.ConnectionState;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.vpn.GlobalBehaviorController;
import net.ivpn.client.vpn.ServiceConstants;
import net.ivpn.client.vpn.VPNConnectionState;
import net.ivpn.client.vpn.controller.WireGuardKeyController.WireGuardKeysEventsListener;
import net.ivpn.client.vpn.wireguard.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static net.ivpn.client.ui.connect.ConnectionState.*;

public class WireGuardBehavior implements VpnBehavior, ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireGuardBehavior.class);

    private VpnStateListener stateListener;
    private PauseTimer timer;
    private BroadcastReceiver notificationActionReceiver;
    private ConnectionState state;
    private long connectionTime;
    private WireGuardKeyController keyController;

    private Settings settings;
    private GlobalBehaviorController globalBehaviorController;
    private ServersRepository serversRepository;
    private VpnBehaviorController vpnBehaviorController;
    private ConfigManager configManager;
    private PingProvider pingProvider;

    @Inject
    WireGuardBehavior(Settings settings, WireGuardKeyController wireGuardKeyController,
                      GlobalBehaviorController globalBehaviorController, ServersRepository serversRepository,
                      VpnBehaviorController vpnBehaviorController, ConfigManager configManager,
                      PingProvider pingProvider) {
        LOGGER.info("Creating");
        this.settings = settings;
        keyController = wireGuardKeyController;
        this.globalBehaviorController = globalBehaviorController;
        this.serversRepository = serversRepository;
        this.vpnBehaviorController = vpnBehaviorController;
        this.configManager = configManager;
        this.pingProvider = pingProvider;

        init();
    }

    private void init() {
        keyController.setKeysEventsListener(getWireGuardKeysEventsListener());
        timer = new PauseTimer(new PauseTimer.PauseTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                LOGGER.info("Will resume in " + DateUtil.formatNotificationTimerCountDown(millisUntilFinished));
                if (stateListener != null) {
                    stateListener.onTimeTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                LOGGER.info("Should be resumed");
                resume();
            }
        });
        state = NOT_CONNECTED;
        registerReceivers();
    }

    @Override
    public void actionByUser() {
        LOGGER.info("actionByUser, state = " + state);

        switch (state) {
            case CONNECTED:
            case CONNECTING: {
                startDisconnectProcess();
                break;
            }
            case PAUSING:
            case DISCONNECTING: {
                //ignore this case
                return;
            }
            case PAUSED:
            case NOT_CONNECTED: {
                startConnecting();
            }
        }
    }

    @Override
    public void setStateListener(VpnStateListener vpnStateListener) {
        LOGGER.info("setStateListener: ");
        this.stateListener = vpnStateListener;
        if (stateListener != null) {
            stateListener.onConnectionStateChanged(state);
        }
    }

    @Override
    public void removeStateListener(VpnStateListener vpnStateListener) {
        LOGGER.info("removeStateListener: ");
        stateListener = null;
    }

    @Override
    public void destroy() {
        LOGGER.info("destroy, remove all registers and listeners");
        unregisterReceivers();
        stop();
    }

    @Override
    public void notifyVpnState() {
        sendConnectionState(timer.getMillisUntilFinished());
    }

    @Override
    public long getConnectionTime() {
        if (state == null || !state.equals(CONNECTED)) {
            return -1;
        } else {
            return System.currentTimeMillis() - connectionTime;
        }
    }

    private void registerReceivers() {
        notificationActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                if (action.equals(NOTIFICATION_ACTION)) {
                    onNotificationAction(intent);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFICATION_ACTION);

        IVPNApplication.getApplication().registerReceiver(notificationActionReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        IVPNApplication.getApplication().unregisterReceiver(notificationActionReceiver);
    }

    private void onNotificationAction(Intent intent) {
        //ToDo refactor it, extra method calls...
        String actionExtra = intent.getStringExtra(NOTIFICATION_ACTION_EXTRA);
        if (actionExtra == null) {
            return;
        }
        LOGGER.info("onNotificationAction, actionExtra = " + actionExtra + " state = " + state);
        switch (actionExtra) {
            case DISCONNECT_ACTION: {
                vpnBehaviorController.disconnect();
                break;
            }
            case PAUSE_ACTION: {
                vpnBehaviorController.pauseActionByUser();
                break;
            }
            case RESUME_ACTION: {
                vpnBehaviorController.resumeActionByUser();
                break;
            }
            case STOP_ACTION: {
                vpnBehaviorController.stopActionByUser();
                break;
            }
            case RECONNECT_ACTION: {
                reconnect();
                break;
            }
        }
    }

    private void findFastestServerAndConnect() {
        LOGGER.info("findFastestServerAndConnect: state = " + state);
        if (stateListener != null) {
            stateListener.onFindingFastestServer();
        }
        pingProvider.findFastestServer(getFastestServerDetectorListener());
        //nothing to do, we will get fastest server through listener
    }

    @Override
    public void resume() {
        LOGGER.info("Resume, state = " + state);
        timer.stopTimer();
        setState(CONNECTING);
        updateNotification();
        resumeVpn();
    }

    private void resumeVpn() {
        LOGGER.info("resumeVpn: state = " + state);
        startWireGuard();
        connectionTime = System.currentTimeMillis();
    }

    @Override
    public void startConnecting() {
        startConnecting(false);
    }

    public void startConnecting(boolean forceConnecting) {
        LOGGER.info("startConnecting, state = " + state);

        if (!forceConnecting && keyController.isKeysExpired()) {
            keyController.regenerateLiveKeys();
            return;
        }

        if (!(state == NOT_CONNECTED || state == PAUSED)) {
            return;
        }

        if (isFastestServerEnabled()) {
            findFastestServerAndConnect();
        } else {
            connect();
        }
    }

    private void connect() {
        LOGGER.info("connect: state = " + state);
        timer.stopTimer();
        setState(CONNECTING);
        updateNotification();
        startWireGuard();
        connectionTime = System.currentTimeMillis();
        globalBehaviorController.updateVpnConnectionState(VPNConnectionState.CONNECTED);
    }

    private void startWireGuard() {
        LOGGER.info("startWireGuard: state = " + state);
        globalBehaviorController.onConnectingToVpn();
        configManager.startWireGuard();
        setState(CONNECTED);
        updateNotification();
    }

    private boolean isFastestServerEnabled() {
        return settings.isFastestServerEnabled();
    }

    @Override
    public void disconnect() {
        LOGGER.info("Disconnect, state = " + state);
        if (state == CONNECTING || state == CONNECTED) {
            startDisconnectProcess();
            return;
        }
        stopVpn();
    }

    private void startDisconnectProcess() {
        LOGGER.info("startDisconnectProcess: state = " + state);
        setState(DISCONNECTING);
        updateNotification();
        updateNotification();
        globalBehaviorController.onDisconnectingFromVpn();
        stopVpn();
    }

    @Override
    public void pause(long pauseDuration) {
        LOGGER.info("Pause, state = " + state);
        timer.startTimer(pauseDuration);
        pauseVpn(pauseDuration);
    }

    private void pauseVpn(long pauseDuration) {
        LOGGER.info("pauseVpn: state = " + state);
        globalBehaviorController.onDisconnectingFromVpn();
        setState(PAUSING);
        updateNotification(pauseDuration);
        configManager.stopWireGuard();
        setState(PAUSED);
        updateNotification(pauseDuration);
    }

    @Override
    public void stop() {
        LOGGER.info("Stop, state = " + state);
        timer.stopTimer();
        stopVpn();
    }

    private void stopVpn() {
        LOGGER.info("stopVpn: state = " + state);
        stopWireGuard();
        connectionTime = 0;
        globalBehaviorController.updateVpnConnectionState(VPNConnectionState.DISCONNECTED);
    }

    public void reconnect() {
        LOGGER.info("reconnect: state = " + state);
        setState(DISCONNECTING);
        updateNotification();
        stopWireGuard();
        new Handler().postDelayed(this::startConnecting, 1000);
    }

    private void stopWireGuard() {
        configManager.stopWireGuard();
        setState(NOT_CONNECTED);
        updateNotification();
        if (stateListener != null) {
            stateListener.onCheckSessionState();
        }
    }

    @Override
    public void regenerateKeys() {
        LOGGER.info("regenerateKeys");
        keyController.regenerateLiveKeys();
    }

    private void sendConnectionState() {
        sendConnectionState(0);
    }

    private void sendConnectionState(long pauseDuration) {
        LOGGER.info("sendConnectionState: state = " + state);
        if (stateListener != null) {
            stateListener.onConnectionStateChanged(state);
        }
    }

    private void updateNotification() {
        updateNotification(0);
    }

    private void updateNotification(long pauseDuration) {
        Context context = IVPNApplication.getApplication();
        Intent intent = new Intent(context, WireGuardUiService.class);
        switch (state) {
            case NOT_CONNECTED:
                if (!WireGuardUiService.isRunning.get()) {
                    return;
                }
                intent.setAction(WIREGUARD_DISCONNECTED);
                break;
            case CONNECTING:
                intent.setAction(WIREGUARD_CONNECTING);
                break;
            case CONNECTED:
                intent.setAction(WIREGUARD_CONNECTED);
                break;
            case PAUSED:
                intent.setAction(WIREGUARD_PAUSED);
                intent.putExtra(VPN_PAUSE_DURATION_EXTRA, pauseDuration);
                break;
            case DISCONNECTING:
            case PAUSING:
                return;
        }
        startService(context, intent);
    }

    private void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private OnFastestServerDetectorListener getFastestServerDetectorListener() {
        return new OnFastestServerDetectorListener() {
            @Override
            public void onFastestServerDetected(Server server) {
                LOGGER.info("Fastest server for WireGuard is detected. Server = " + server.getDescription());
                if (stateListener != null) {
                    stateListener.notifyServerAsFastest(server);
                }

                serversRepository.setCurrentServer(ServerType.ENTRY, server);
                connect();
            }

            @Override
            public void onDefaultServerApplied(Server server) {
                LOGGER.info("Default WireGuard server is applied. Server = " + server.getDescription());
                ToastUtil.toast(R.string.connect_unable_test_fastest_server);
                if (stateListener != null) {
                    stateListener.notifyServerAsFastest(server);
                }

                serversRepository.setCurrentServer(ServerType.ENTRY, server);
                connect();
            }
        };
    }

    private void setState(ConnectionState state) {
        this.state = state;
        sendConnectionState();
    }

    private WireGuardKeysEventsListener getWireGuardKeysEventsListener() {
        return new WireGuardKeysEventsListener() {
            @Override
            public void onKeyGenerating() {
                if (stateListener != null) {
                    stateListener.onRegeneratingKeys();
                }
            }

            @Override
            public void onKeyGeneratedSuccess() {
                if (stateListener != null) {
                    stateListener.onRegenerationSuccess();
                }
                switch (state) {
                    case NOT_CONNECTED:
                    case PAUSED: {
                        startConnecting();
                        break;
                    }
                    case CONNECTED:
                    case CONNECTING: {
                        reconnect();
                        break;
                    }
                    case DISCONNECTING:
                    case PAUSING: {
                        //ToDo ignore it right now.
                        break;
                    }
                }
            }

            @Override
            public void onKeyGeneratedError(String error, Throwable throwable) {
                switch (state) {
                    case NOT_CONNECTED:
                    case PAUSED: {
                        ErrorResponse errorResponse = Mapper.errorResponseFrom(error);
                        if (errorResponse != null && errorResponse.getStatus() != null) {
                            switch (errorResponse.getStatus()) {
                                case Responses.WIREGUARD_KEY_NOT_FOUND: {
                                    if (stateListener != null) {
                                        stateListener.onRegenerationError(Dialogs.WG_UPGRADE_ERROR);
                                    }
                                    return;
                                }
                                case Responses.WIREGUARD_KEY_LIMIT_REACHED: {
                                    if (stateListener != null) {
                                        stateListener.onRegenerationError(Dialogs.WG_MAXIMUM_KEYS_REACHED);
                                    }
                                    return;
                                }
                            }
                        }

                        if (keyController.isKeysHardExpired()) {
                            if (stateListener != null) {
                                stateListener.onRegenerationError(Dialogs.WG_UPGRADE_ERROR);
                            }
                        } else {
                            WireGuardBehavior.this.keyController.startShortPeriodAlarm();
                            startConnecting(true);
                        }
                        break;
                    }
                    case CONNECTED:
                    case CONNECTING: {
                        //There is nothing to do;
                        break;
                    }
                    case DISCONNECTING:
                    case PAUSING: {
                        //ToDo ignore it right now.
                        break;
                    }
                }
            }
        };
    }
}