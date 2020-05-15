package net.ivpn.client.ui.network

import android.content.Context
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ObservableField
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.vpn.local.NetworkController
import net.ivpn.client.vpn.model.NetworkState
import net.ivpn.client.vpn.model.WifiItem
import javax.inject.Inject

open class NetworkItemViewModel @Inject constructor(
        private val networkController: NetworkController
) {

    var wifiItem = ObservableField<WifiItem>()
    val currentState = ObservableField<NetworkState>()

    val defaultState = ObservableField<NetworkState>()
    val selectedState = ObservableField<NetworkState>()

    var networkStateListener = RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int ->
        onCheckedChanged(checkedId)
    }

    private lateinit var context: Context

    fun setContext(context: Context) {
        this.context = context
    }

    fun setWifiItem(wifiItem: WifiItem) {
        this.wifiItem.set(wifiItem)
        currentState.set(wifiItem.networkState)
        selectedState.set(wifiItem.networkState)
    }

    fun getColor(state: NetworkState): Int {
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
        }
    }

    private fun onCheckedChanged(checkedId: Int) {
        val networkState = NetworkState.getById(checkedId)
        if (networkState == this.selectedState.get()) {
            return
        }

        this.selectedState.set(networkState)
    }

    open fun applyState() {
        wifiItem.get()?.let {
            networkController.changeMarkFor(it.ssid, currentState.get(), selectedState.get())
            it.networkState = selectedState.get()
        }

        currentState.set(selectedState.get())
    }

    fun setDefaultState(defaultState: NetworkState) {
        this.defaultState.set(defaultState)
    }

    fun setCurrentState(currentState: NetworkState) {
        this.currentState.set(currentState)
        selectedState.set(currentState)
    }

    val title: String
        get() = wifiItem.get()!!.title

}