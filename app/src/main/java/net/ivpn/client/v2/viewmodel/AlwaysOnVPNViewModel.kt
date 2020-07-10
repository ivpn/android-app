package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class AlwaysOnVPNViewModel @Inject constructor(
        private val buildController: BuildController
) {

    val isAlwaysOnVpnSupported = ObservableBoolean()

    fun onResume() {
        isAlwaysOnVpnSupported.set(buildController.isAlwaysOnVpnSupported)
    }
}