package net.ivpn.core.v2.protocol.port

import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import javax.inject.Inject

@ApplicationScope
class CustomPortViewModel @Inject constructor(
    private val settings: Settings,
    private val protocolController: ProtocolController
) : ViewModel() {

    val protocol: Protocol
        get() = protocolController.currentProtocol

}
