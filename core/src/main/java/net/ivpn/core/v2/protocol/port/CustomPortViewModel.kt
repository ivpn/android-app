package net.ivpn.core.v2.protocol.port

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 Privatus Limited.

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

import android.provider.Settings.Global.getString
import androidx.lifecycle.ViewModel
import net.ivpn.core.R
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import javax.inject.Inject

@ApplicationScope
class CustomPortViewModel @Inject constructor(
    private val settings: Settings,
    private val protocolController: ProtocolController
) : ViewModel() {

    val portRangesText: String
        get() {
            val combinedRanges = getRanges().sortedBy { it.first }.combineIntervals()
            return combinedRanges.joinToString(", ") { "${it.first} - ${it.last}" }
        }

    val enableType: Boolean
        get() = protocol == Protocol.OPENVPN

    private val protocol: Protocol
        get() = protocolController.currentProtocol

    fun validate(port: Int): String? {
        for (range in getRanges()) {
            if (port in range) {
                return null
            }
        }
        return portRangesText
    }

    fun addPort(port: Port) {
        if (protocol == Protocol.WIREGUARD) {
            val ports = settings.wireGuardCustomPorts
            settings.wireGuardCustomPorts = ports.plus(port)
        } else {
            val ports = settings.openVpnCustomPorts
            settings.openVpnCustomPorts = ports.plus(port)
        }
    }

    private fun getPortRanges(): List<Port> {
        return if (protocol == Protocol.WIREGUARD) {
            settings.wireGuardPortRanges
        } else {
            settings.openVpnPortRanges
        }
    }

    private fun getRanges(): List<IntRange> {
        return mapPortsToIntRanges(getPortRanges())
    }

    private fun mapPortsToIntRanges(ports: List<Port>): List<IntRange> {
        val intRanges = mutableListOf<IntRange>()

        for (port in ports) {
            val range = port.range
            val intRange = range.min..range.max
            intRanges.add(intRange)
        }

        return intRanges
    }

    private fun List<IntRange>.combineIntervals(): List<IntRange> {
        val combined = mutableListOf<IntRange>()
        var accumulator = 0..0

        for (interval in sortedBy { it.first }) {
            if (accumulator == 0..0) {
                accumulator = interval
            }

            if (accumulator.last >= interval.last) {
                // interval is already inside accumulator
            } else if (accumulator.last + 1 >= interval.first) {
                // interval hangs off the back end of accumulator
                accumulator = (accumulator.first..interval.last)
            } else {
                // interval does not overlap
                combined.add(accumulator)
                accumulator = interval
            }
        }

        if (accumulator != 0..0) {
            combined.add(accumulator)
        }

        return combined
    }

}
