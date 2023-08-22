package net.ivpn.core.v2.protocol.port

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

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

    fun getPorts(): List<Port> {
        return if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardPorts
        } else {
            settings.openVpnPorts
        }
    }

    fun getCustomPorts(): List<Port> {
        return if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardCustomPorts
        } else {
            settings.openVpnCustomPorts
        }
    }

    fun getPort(): Port {
        return if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardPort
        } else {
            settings.openVpnPort
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
