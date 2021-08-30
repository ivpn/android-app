package net.ivpn.core.vpn.controller;

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
import android.util.Log;

import com.wireguard.android.model.Tunnel;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.v2.timepicker.TimePickerActivity;
import net.ivpn.core.vpn.OnProtocolChangedListener;
import net.ivpn.core.vpn.Protocol;
import net.ivpn.core.vpn.ProtocolController;
import net.ivpn.core.vpn.VPNConnectionState;
import net.ivpn.core.vpn.VPNStateListener;
import net.ivpn.core.vpn.openvpn.IVPNService;
import net.ivpn.core.vpn.wireguard.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.VpnStatus;

@ApplicationScope
public class VpnBehaviorController implements BehaviourListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpnBehaviorController.class);
    private static final String TAG = VpnBehaviorController.class.getSimpleName();

    private VpnBehavior behavior;
    private Protocol protocol;

    private ConfigManager configManager;

    private List<VpnStateListener> listeners = new ArrayList<>();

    public VPNStateListener vpnStateListener;

    @Inject
    public VpnBehaviorController(ConfigManager configManager,
                                 ProtocolController protocolController) {
        this.configManager = configManager;

        OnProtocolChangedListener onProtocolChangedListener = this::init;
        protocolController.addOnProtocolChangedListener(onProtocolChangedListener);
    }

    public void init(Protocol protocol) {
        if (protocol == this.protocol) {
            return;
        }
        this.protocol = protocol;
        if (behavior != null) {
            behavior.destroy();
        }
        behavior = getBehavior(protocol);
        behavior.behaviourListener = this;
        for (VpnStateListener listener : listeners) {
            behavior.addStateListener(listener);
        }
    }

    public void disconnect() {
        behavior.disconnect();
    }

    public void pauseActionByUser() {
        Context context = IVPNApplication.INSTANCE.getApplication();
        Intent intent = new Intent(context, TimePickerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void pauseFor(long pauseDuration) {
        behavior.pause(pauseDuration);
    }

    public void resumeActionByUser() {
        behavior.resume();
    }

    public void stopActionByUser() {
        behavior.stop();
    }

    @Override
    public void onDisconnectingFromVpn() {
        vpnStateListener.onDisconnectingFromVpn();
    }

    @Override
    public void onConnectingToVpn() {
        vpnStateListener.onConnectingToVpn();
    }

    @Override
    public void updateVpnConnectionState(VPNConnectionState state) {
        vpnStateListener.updateVpnConnectionState(state);
    }

    public void onServerUpdated(Boolean forceConnect) {
        if (isVPNActive()) {
            behavior.reconnect();
        } else if (forceConnect) {
            behavior.startConnecting();
        }
    }

    public void connectActionByRules() {
        LOGGER.info("connectActionByRules");
        behavior.startConnecting(false);
    }

    public void connectionActionByUser() {
        LOGGER.info("actionByUser");
        behavior.actionByUser();
    }

    public void regenerate() {
        LOGGER.info("regenerate");
        behavior.regenerateKeys();
    }

    public void notifyVpnState() {
        LOGGER.info("notifyVpnState");
        behavior.notifyVpnState();
    }

    public void addVpnStateListener(VpnStateListener stateListener) {
        Log.d(TAG, "setVpnStateListener: ");
        listeners.add(stateListener);
        behavior.addStateListener(stateListener);
    }

    public void removeVpnStateListener(VpnStateListener stateListener) {
        Log.d(TAG, "removeVpnStateListener: ");
        listeners.remove(stateListener);
        behavior.removeStateListener(stateListener);
    }

    public boolean isVPNActive() {
        if (protocol == Protocol.OPENVPN) {
            return VpnStatus.isVPNActive()
                    || (VpnStatus.lastLevel == ConnectionStatus.LEVEL_NONETWORK
                        && IVPNService.isRunning.get());
        } else {
            Tunnel tunnel = configManager.getTunnel();
            LOGGER.info("isVPNActive, tunnel " + tunnel);
            LOGGER.info("isVPNActive, isActive = " + (tunnel != null && tunnel.getState().equals(Tunnel.State.UP)));
            return tunnel != null && tunnel.getState().equals(Tunnel.State.UP);
        }
    }

    private VpnBehavior getBehavior(Protocol protocol) {
        if (protocol == Protocol.WIREGUARD) {
            return IVPNApplication.appComponent.getWireGuardBehavior();
        }
        return IVPNApplication.appComponent.getOpenVpnBehavior();
    }
}