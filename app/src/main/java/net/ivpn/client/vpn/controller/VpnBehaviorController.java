package net.ivpn.client.vpn.controller;

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

    @Inject
    public VpnBehaviorController(ConfigManager configManager, ServersRepository serversRepository,
                                 ProtocolController protocolController) {
        LOGGER.info("VPN controller is init");
        this.configManager = configManager;

        OnServerChangedListener onServerChangedListener = this::onServerUpdated;
        serversRepository.setOnServerChangedListener(onServerChangedListener);

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

    public void onServerUpdated() {
        if (isVPNActive()) {
            behavior.reconnect();
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
        behavior.addStateListener(stateListener);
    }

    public void removeVpnStateListener(VpnStateListener stateListener) {
        Log.d(TAG, "removeVpnStateListener: ");
        behavior.removeStateListener(stateListener);
    }

    public long getConnectionTime() {
        return behavior.getConnectionTime();
    }

    public boolean isVPNActive() {
        if (protocol == Protocol.OpenVPN) {
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
        if (protocol == Protocol.WireGuard) {
            return IVPNApplication.getApplication().appComponent.provideProtocolComponent().create().getWireGuardBehavior();
        }
        return IVPNApplication.getApplication().appComponent.provideProtocolComponent().create().getOpenVpnBehavior();
    }
}