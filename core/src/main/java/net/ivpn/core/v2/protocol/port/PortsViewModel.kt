package net.ivpn.core.v2.protocol.port

import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import javax.inject.Inject

@ApplicationScope
class PortsViewModel @Inject constructor(
    private val settings: Settings,
    private val protocolController: ProtocolController
) : ViewModel() {

    val protocol: Protocol
        get() = protocolController.currentProtocol
    val openVPNPort: Port
        get() = settings.openVpnPort
    val wireGuardPort: Port
        get() = settings.wireGuardPort


    fun getPorts(): List<Port> {
        return if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardPorts
        } else {
            settings.openVpnPorts
        }
    }

    fun setPort(port: Port) {
        if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardPort = port
        } else {
            settings.openVpnPort = port
        }
    }

}