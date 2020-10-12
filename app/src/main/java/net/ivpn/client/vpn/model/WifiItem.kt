package net.ivpn.client.vpn.model

import androidx.databinding.ObservableField
import net.ivpn.client.common.utils.StringUtil

data class WifiItem(val ssid: String, var networkState: ObservableField<NetworkState>) {
    val title: String = StringUtil.formatWifiSSID(ssid)
}