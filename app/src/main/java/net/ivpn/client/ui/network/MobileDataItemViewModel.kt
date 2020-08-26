package net.ivpn.client.ui.network

import net.ivpn.client.vpn.local.NetworkController
import javax.inject.Inject

class MobileDataItemViewModel @Inject constructor(
        private val networkController: NetworkController
) : NetworkItemViewModel(networkController) {

    override fun applyState() {
        currentState.set(selectedState.get())
        networkController.updateMobileDataState(selectedState.get())
    }

}