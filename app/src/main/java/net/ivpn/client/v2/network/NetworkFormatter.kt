package net.ivpn.client.v2.network

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ObservableField
import net.ivpn.client.R
import net.ivpn.client.common.utils.StringUtil
import net.ivpn.client.vpn.model.NetworkSource
import net.ivpn.client.vpn.model.NetworkState
import net.ivpn.client.vpn.model.WifiItem

class NetworkStateFormatter(val context: Context) {

    val defaultState = ObservableField<NetworkState>()

    fun getCurrentStateColor(currentState: NetworkState?, defaultState: NetworkState?): Int {
        return if (currentState == NetworkState.DEFAULT) {
            getColor(defaultState)
        } else {
            getColor(currentState)
        }
    }

    fun getCurrentStateText(wifiItem: WifiItem, defaultState: NetworkState?): String? {
        return if (wifiItem.networkState == NetworkState.DEFAULT) {
            defaultState?.let {
                context.getString(it.textRes)
            }
        } else {
            wifiItem.networkState.get()?.let {
                context.getString(it.textRes)
            }
        }
    }

    fun getDefaultText(defaultState: NetworkState?): String? {
        return defaultState?.let {
            context.getString(it.textRes)
        }
    }

    fun isWiFiCurrentStateVisible(source: NetworkSource?, ssid: String?): Boolean {
        println("isWiFiCurrentStateVisible for $source and $ssid")
        source?.let { sourceObj ->
            ssid?.let { ssidObj ->
                println("sourceObj.ssid = ${sourceObj.ssid} ssidObj = $ssidObj")
                println("return ${sourceObj == NetworkSource.WIFI && ssidObj == sourceObj.ssid}")
                return sourceObj == NetworkSource.WIFI && ssidObj == StringUtil.formatWifiSSID(sourceObj.ssid)
            } ?: return false
        } ?: return false
    }

    fun isMobileDataCurrentStateVisible(source: NetworkSource?): Boolean {
        source?.let { sourceObj ->
                return sourceObj == NetworkSource.MOBILE_DATA
        } ?: return false
    }

    fun getColor(state: NetworkState?): Int {
        return when (state) {
            NetworkState.TRUSTED -> {
                ResourcesCompat.getColor(context.resources, R.color.color_trusted_text, null)
            }
            NetworkState.UNTRUSTED -> {
                ResourcesCompat.getColor(context.resources, R.color.color_untrusted_text, null)
            }
            NetworkState.NONE -> {
                ResourcesCompat.getColor(context.resources, R.color.color_none_text, null)
            }
            NetworkState.DEFAULT -> {
                ResourcesCompat.getColor(context.resources, R.color.color_default_text, null)
            }
            else -> {
                ResourcesCompat.getColor(context.resources, R.color.color_none_text, null)
            }
        }
    }
}