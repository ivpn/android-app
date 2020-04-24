package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class MultiHopViewModel @Inject constructor() : ViewModel() {
    val isEnabled = ObservableBoolean()

    var navigator: MultiHopNavigator? = null

    init {
        isEnabled.set(true)
    }

    fun enableMultiHop(state: Boolean) {
        if (isEnabled.get() == state) return
        isEnabled.set(state)

        navigator?.onMultiHopStateChanged(state)
    }


    interface MultiHopNavigator {
        fun onMultiHopStateChanged(state: Boolean)
    }

}