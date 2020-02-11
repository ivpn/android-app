package net.ivpn.client.ui.network;

import net.ivpn.client.vpn.model.NetworkState;

public interface OnNetworkBehaviourChangedListener {
    void onNetworkBehaviourChanged(NetworkState state);
}
