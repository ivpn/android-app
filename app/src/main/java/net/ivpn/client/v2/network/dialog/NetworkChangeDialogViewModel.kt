package net.ivpn.client.v2.network.dialog

import android.widget.RadioGroup
import androidx.databinding.ObservableField
import net.ivpn.client.vpn.model.NetworkState

abstract class NetworkChangeDialogViewModel(
        val currentState: NetworkState
) {
    var selectedState: ObservableField<NetworkState> = ObservableField(currentState)

    var networkStateListener = RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int ->
        onCheckedChanged(checkedId)
    }

    private fun onCheckedChanged(checkedId: Int) {
        val networkState = NetworkState.getById(checkedId)
        if (networkState == this.selectedState.get()) {
            return
        }

        this.selectedState.set(networkState)
    }

    abstract fun apply()

}