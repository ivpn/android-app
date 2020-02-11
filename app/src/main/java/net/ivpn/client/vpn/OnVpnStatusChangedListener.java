package net.ivpn.client.vpn;

import de.blinkt.openvpn.core.ConnectionStatus;

public interface OnVpnStatusChangedListener {
    void onStatusChanged(ConnectionStatus status);
}
