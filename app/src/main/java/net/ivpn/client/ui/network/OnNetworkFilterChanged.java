package net.ivpn.client.ui.network;

import net.ivpn.client.vpn.model.NetworkState;

public interface OnNetworkFilterChanged {
    void onNetworkFilterChanged(NetworkState state);
}
