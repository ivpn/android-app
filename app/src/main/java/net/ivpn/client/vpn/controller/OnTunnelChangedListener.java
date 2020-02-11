package net.ivpn.client.vpn.controller;

import com.wireguard.android.model.Tunnel;

public interface OnTunnelChangedListener {
    void onTunnelChanged(Tunnel tunnel);
}
