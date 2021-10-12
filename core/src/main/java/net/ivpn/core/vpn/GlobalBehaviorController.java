package net.ivpn.core.vpn;

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

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.v2.mocklocation.MockLocationController;
import net.ivpn.core.vpn.controller.VpnBehaviorController;
import net.ivpn.core.vpn.local.PermissionActivity;
import net.ivpn.core.vpn.model.VPNRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static net.ivpn.core.vpn.VPNState.NONE;
import static net.ivpn.core.vpn.VPNState.VPN;

@ApplicationScope
public class GlobalBehaviorController implements ServiceConstants, VPNStateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBehaviorController.class);

    private VPNState state = NONE;
    private boolean isVpnDisconnecting;
    private VPNRule vpnRule = VPNRule.NOTHING;

    private VpnBehaviorController vpnBehaviorController;
    private MockLocationController mock;

    @Inject
    public GlobalBehaviorController(VpnBehaviorController vpnBehaviorController,
                                    MockLocationController mock) {
        this.vpnBehaviorController = vpnBehaviorController;
        this.mock = mock;

        vpnBehaviorController.vpnStateListener = this;
    }

    public VPNState getState() {
        return state;
    }

    public void onConnectingToVpn() {
        LOGGER.info("onConnectingToVpn: state BEFORE = " + state);
        switch (state) {
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

    public void applyNetworkRules(VPNRule vpnRule) {
        LOGGER.info("applyNetworkRules vpnRule = " + vpnRule);
        applyVpnRule(vpnRule);
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
        Context context = IVPNApplication.INSTANCE.getApplication();

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
        Context context = IVPNApplication.INSTANCE.getApplication();
        Intent vpnIntent = new Intent(context, PermissionActivity.class);
        vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(vpnIntent);
    }

    public void release() {
        LOGGER.info("release");
        finishAll();
    }

    public void finishAll() {
        LOGGER.info("finishAll");
        stopVPN();
        state = NONE;
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
        mock.stop();
        state = NONE;
    }

    private void onVpnConnected() {
        LOGGER.info("onVpnConnected");
        mock.mock();
        state = VPN;
    }
}