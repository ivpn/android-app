package net.ivpn.client.v2.network

import net.ivpn.client.vpn.model.NetworkState

interface OnChangeNetworkStateListener {

    fun onNetworkStateChanged(state: NetworkState)

}