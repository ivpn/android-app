package net.ivpn.client.vpn.controller;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wireguard.android.model.Tunnel;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.OnServerChangedListener;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.ui.timepicker.TimePickerActivity;
import net.ivpn.client.vpn.OnProtocolChangedListener;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.openvpn.IVPNService;
import net.ivpn.client.vpn.wireguard.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.VpnStatus;

@ApplicationScope
public class VpnBehaviorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpnBehaviorController.class);
    private static final String TAG = VpnBehaviorController.class.getSimpleName();

    private VpnBehavior behavior;
    private Protocol protocol;

    private ConfigManager configManager;

    private List<VpnStateListener> listeners = new ArrayList<>();

    @Inject
    public VpnBehaviorController(ConfigManager configManager, ServersRepository serversRepository,
                                 ProtocolController protocolController) {
        LOGGER.info("VPN controller is init");
        this.configManager = configManager;

//        OnServerChangedListener onServerChangedListener = this::onServerUpdated;
//        serversRepository.setOnServerChangedListener(onServerChangedListener);

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
        for (VpnStateListener listener: listeners) {
            behavior.addStateListener(listener);
        }
    }

    public void disconnect() {
        behavior.disconnect();
    }

    public void pauseActionByUser() {
        Context context = IVPNApplication.getApplication();
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

    public void onServerUpdated(Boolean forceConnect) {
        if (isVPNActive()) {
            behavior.reconnect();
        } else if (forceConnect) {
            behavior.startConnecting();
        }
    }

    public void connectActionByRules() {
        LOGGER.info("connectActionByRules");
        behavior.startConnecting(true);
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

    public long getConnectionTime() {
        return behavior.getConnectionTime();
    }

    public boolean isVPNActive() {
        if (protocol == Protocol.OPENVPN) {
            return VpnStatus.isVPNActive()
                    || (VpnStatus.lastLevel == ConnectionStatus.LEVEL_NONETWORK && IVPNService.isRunning.get());
        } else {
            Tunnel tunnel = configManager.getTunnel();
            LOGGER.info("isVPNActive, tunnel " + tunnel);
            LOGGER.info("isVPNActive, isActive = " + (tunnel != null && tunnel.getState().equals(Tunnel.State.UP)));
            return tunnel != null && tunnel.getState().equals(Tunnel.State.UP);
        }
    }

    private VpnBehavior getBehavior(Protocol protocol) {
        if (protocol == Protocol.WIREGUARD) {
            return IVPNApplication.getApplication().appComponent.provideProtocolComponent().create().getWireGuardBehavior();
        }
        return IVPNApplication.getApplication().appComponent.provideProtocolComponent().create().getOpenVpnBehavior();
    }
}