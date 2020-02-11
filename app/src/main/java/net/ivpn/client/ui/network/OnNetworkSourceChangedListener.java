package net.ivpn.client.ui.network;

import net.ivpn.client.vpn.model.NetworkSource;
import net.ivpn.client.vpn.model.NetworkState;

public interface OnNetworkSourceChangedListener {
    void onNetworkSourceChanged(NetworkSource source);
    void onDefaultNetworkStateChanged(NetworkState defaultState);
}
