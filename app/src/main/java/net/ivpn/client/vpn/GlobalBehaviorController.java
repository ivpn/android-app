package net.ivpn.client.vpn;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.v2.mocklocation.MockLocationController;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.local.KillSwitchPermissionActivity;
import net.ivpn.client.vpn.local.KillSwitchService;
import net.ivpn.client.vpn.local.PermissionActivity;
import net.ivpn.client.vpn.model.KillSwitchRule;
import net.ivpn.client.vpn.model.VPNRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static net.ivpn.client.vpn.VPNState.BOTH;
import static net.ivpn.client.vpn.VPNState.KILL_SWITCH;
import static net.ivpn.client.vpn.VPNState.NONE;
import static net.ivpn.client.vpn.VPNState.VPN;
import static net.ivpn.client.vpn.model.KillSwitchRule.ENABLE;
import static net.ivpn.client.vpn.model.KillSwitchRule.NOTHING;

@ApplicationScope
public class GlobalBehaviorController implements ServiceConstants, VPNStateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBehaviorController.class);

    private VPNState state = NONE;
    private boolean isVpnDisconnecting;
    private KillSwitchRule killSwitchRule = NOTHING;
    private VPNRule vpnRule = VPNRule.NOTHING;

    private BroadcastReceiver securityGuardActionsReceiver;
    private Settings settings;
    private VpnBehaviorController vpnBehaviorController;
    private MockLocationController mock;

    @Inject
    public GlobalBehaviorController(Settings settings,
                                    VpnBehaviorController vpnBehaviorController,
                                    MockLocationController mock) {
        this.settings = settings;
        this.vpnBehaviorController = vpnBehaviorController;
        this.mock = mock;

        vpnBehaviorController.vpnStateListener = this;
    }

    public void init() {
        if (isKillSwitchEnabled()) {
            state = isVpnActive() ? BOTH : KILL_SWITCH;
        } else {
            state = isVpnActive() ? VPN : NONE;
        }
        registerReceiver();
    }

    public VPNState getState() {
        return state;
    }

    public boolean isKillSwitchShouldBeStarted() {
        LOGGER.info("isKillSwitchShouldBeStarted");
        switch (killSwitchRule) {
            case ENABLE: {
                return state.equals(KILL_SWITCH) || state.equals(NONE);
            }
            case DISABLE: {
                return false;
            }
            case NOTHING: {
                return state.equals(KILL_SWITCH);
            }
        }
        return state.equals(KILL_SWITCH);
    }

    public void enableKillSwitch() {
        LOGGER.info("enableKillSwitch");
        switch (state) {
            case KILL_SWITCH:
            case NONE: {
                state = KILL_SWITCH;
                startKillSwitch();
                break;
            }
            case BOTH:
            case VPN: {
                state = BOTH;
                break;
            }
        }
    }

    public void disableKillSwitch() {
        LOGGER.info("disableKillSwitch: state BEFORE = " + state);
        switch (state) {
            case NONE:
            case KILL_SWITCH: {
                state = NONE;
                stopKillSwitch();
                break;
            }
            case VPN:
            case BOTH: {
                state = VPN;
                break;
            }
        }
    }

    public void onConnectingToVpn() {
        LOGGER.info("onConnectingToVpn: state BEFORE = " + state);
        switch (state) {
            case BOTH:
                break;
            case KILL_SWITCH:
                stopKillSwitch();
                state = BOTH;
                break;
            case VPN:
            case NONE:
                state = VPN;
                break;
        }
    }

    public void onDisconnectingFromVpn() {
        isVpnDisconnecting = true;
    }

    public void stopVPN() {
        LOGGER.info("stopVPN");
        if (!isVpnActive()) {
            LOGGER.info("VPN is NOT active, skip stopVPN event");
            return;
        }
        vpnBehaviorController.disconnect();
    }

    public void applyNetworkRules(KillSwitchRule killSwitchRule, VPNRule vpnRule) {
        LOGGER.info("applyNetworkRules killSwitchRule = " + killSwitchRule + " vpnRule = " + vpnRule);
        applyVpnRule(vpnRule);
        applyKillSwitchRule(killSwitchRule);
    }

    public void applyVpnRule(VPNRule vpnRule) {
        LOGGER.info("applyVpnRule vpnRule = " + vpnRule);
        this.vpnRule = vpnRule;
        if (vpnRule.equals(VPNRule.CONNECT)) {
            tryToConnectVpn();
        } else if (vpnRule.equals(VPNRule.DISCONNECT)) {
            stopVPN();
        }
    }

    public void applyKillSwitchRule(KillSwitchRule killSwitchRule) {
        LOGGER.info("applyKillSwitchRule: old killSwitchRule = " + this.killSwitchRule);
        LOGGER.info("applyKillSwitchRule: new killSwitchRule = " + killSwitchRule);
        LOGGER.info("applyKillSwitchRule: state = " + state);
        //Check if VPN is running or preparing to run;
        this.killSwitchRule = killSwitchRule;
        switch (killSwitchRule) {
            case ENABLE:
                settings.setKillSwitchEnabled(true);
                enableKillSwitch();
                break;
            case DISABLE:
                settings.setKillSwitchEnabled(false);
                disableKillSwitch();
                break;
            case NOTHING:
                break;
        }
    }

    private boolean isVPNRunningOrPreparingToRun() {
        return isVpnActive() || vpnRule.equals(VPNRule.CONNECT)
                || (state.equals(VPN) || state.equals(BOTH));
    }

    private void tryToConnectVpn() {
        LOGGER.info("tryToConnectVpn");
        if (isVpnActive()) {
            return;
        }

        if (isVPNPermissionGranted()) {
            vpnBehaviorController.connectActionByRules();
        } else {
            askPermissionAndStartVpn();
        }
    }

    private boolean isVPNPermissionGranted() {
        Context context = IVPNApplication.getApplication();

        Intent intent;
        try {
            intent = VpnService.prepare(context);
        } catch (Exception ignored) {
            return false;
        }

        return intent == null;
    }

    private void askPermissionAndStartVpn() {
        LOGGER.info("askPermissionAndStartVpn");
        Context context = IVPNApplication.getApplication();
        Intent vpnIntent = new Intent(context, PermissionActivity.class);
        vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(vpnIntent);
    }

    public void startKillSwitch() {
        LOGGER.info("startKillSwitch");
        Context context = IVPNApplication.getApplication();
        Intent vpnIntent = new Intent(context, KillSwitchPermissionActivity.class);
        vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(vpnIntent);

//        Intent killSwitchIntent = new Intent(context, KillSwitchService.class);
//        killSwitchIntent.setAction(START_KILL_SWITCH);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(killSwitchIntent);
//        } else {
//            context.startService(killSwitchIntent);
//        }
    }

    private void stopKillSwitch() {
        LOGGER.info("stopKillSwitch");
        if (!KillSwitchService.isRunning.get()) {
            return;
        }
        Context context = IVPNApplication.getApplication();
        Intent stopIntent = new Intent(context, KillSwitchService.class);
        stopIntent.setAction(STOP_KILL_SWITCH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(stopIntent);
        } else {
            context.startService(stopIntent);
        }
    }

    public void release() {
        LOGGER.info("release");
        finishAll();
        LocalBroadcastManager.getInstance(IVPNApplication.getApplication()).unregisterReceiver(securityGuardActionsReceiver);
    }

    public void finishAll() {
        LOGGER.info("finishAll");
        stopVPN();
        stopKillSwitch();
        state = NONE;
    }

    private void registerReceiver() {
        securityGuardActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(KILL_SWITCH_ACTION_EXTRA);
                applyKillSwitchAction(action);
            }
        };

        LocalBroadcastManager.getInstance(IVPNApplication.getApplication()).registerReceiver(securityGuardActionsReceiver,
                new IntentFilter(KILL_SWITCH_ACTION));
    }

    private void applyKillSwitchAction(String action) {
        if (action == null) return;

        switch (action) {
            case CONNECT_VPN_ACTION:
                vpnBehaviorController.connectActionByRules();
                break;
            case APP_SETTINGS_ACTION:
                openKillSwitchSettings();
                break;
            case STOP_KILL_SWITCH_ACTION:
                stopKillSwitch();
                break;
        }
    }

    private void openKillSwitchSettings() {
        new NavDeepLinkBuilder(IVPNApplication.getApplication())
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.killSwitchFragment).createTaskStackBuilder().startActivities();
    }

    private boolean isKillSwitchEnabled() {
        return settings.isKillSwitchEnabled();
    }

    private boolean isVpnActive() {
        return vpnBehaviorController.isVPNActive();
    }

    public void updateVpnConnectionState(VPNConnectionState state) {
        switch (state) {
            case CONNECTED:
                onVpnConnected();
                break;
            case DISCONNECTED:
                if (!isVpnDisconnecting) return;

                isVpnDisconnecting = false;
                onVpnDisconnected();
                break;
            case ERROR:
                break;
        }
    }

    private void onVpnDisconnected() {
        LOGGER.info("onVpnDisconnected: state = " + state);
        LOGGER.info("onVpnDisconnected: killSwitchRule = " + killSwitchRule);
        mock.stop();
        switch (state) {
            case KILL_SWITCH:
            case BOTH: {
                state = KILL_SWITCH;
                if (killSwitchRule.equals(ENABLE) || killSwitchRule.equals(NOTHING)) {
                    startKillSwitch();
                }
                break;
            }
            case NONE:
            case VPN: {
                state = NONE;
                if (killSwitchRule.equals(ENABLE)) {
                    startKillSwitch();
                }
                break;
            }
        }
    }

    private void onVpnConnected() {
        LOGGER.info("onVpnConnected");
        mock.mock();
        switch (state) {
            case KILL_SWITCH:
            case BOTH:
                state = BOTH;
                break;
            default:
                state = VPN;
        }
    }
}