package net.ivpn.client.vpn.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class WireGuardKeyBroadcastReceiver extends BroadcastReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WireGuardKeyBroadcastReceiver.class);

    @Inject
    public ProtocolController protocolController;
    @Inject
    public VpnBehaviorController vpnBehaviorController;

    @Override
    public void onReceive(Context context, Intent intent) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        LOGGER.info("Receive intent");
        if (protocolController.getCurrentProtocol().equals(Protocol.WIREGUARD)
                && vpnBehaviorController.isVPNActive()) {
            vpnBehaviorController.regenerate();
        }
    }
}