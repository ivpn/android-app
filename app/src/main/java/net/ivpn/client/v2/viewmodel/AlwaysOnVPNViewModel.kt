package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import net.ivpn.client.common.BuildController
import javax.inject.Inject

class AlwaysOnVPNViewModel @Inject constructor(
        buildController: BuildController
){

    val isAlwaysOnVpnSupported = ObservableBoolean()

    init {
        isAlwaysOnVpnSupported.set(buildController.isAlwaysOnVpnSupported)
    }
}