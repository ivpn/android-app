package net.ivpn.core.v2.protocol.port

import androidx.lifecycle.ViewModel
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
            val combinedRanges = combinedIntervals(getRanges().sortedBy { it.first })
            val textRanges = mutableListOf<String>()

            for (range in combinedRanges) {
                textRanges.add("${range.first} - ${range.last}")
            }

            return textRanges.joinToString(", ")
        }

    private val protocol: Protocol
        get() = protocolController.currentProtocol

    fun validate(port: Int): String? {
        for (range in getRanges()) {
            if (range.contains(port)) {
                return null
            }
        }

        return "Enter port number in the range: $portRangesText"
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

    companion object {
        fun combinedIntervals(intervals: List<IntRange>): List<IntRange> {
            val combined = mutableListOf<IntRange>()
            var accumulator = (0..0) // empty range

            for (interval in intervals.sortedBy { it.first }) {
                if (accumulator == (0..0)) {
                    accumulator = interval
                }

                if (accumulator.last >= interval.last) {
                    // interval is already inside accumulator
                } else if (accumulator.last + 1 >= interval.first) {
                    // interval hangs off the back end of accumulator
                    accumulator = (accumulator.first..interval.last)
                } else if (accumulator.last <= interval.first) {
                    // interval does not overlap
                    combined.add(accumulator)
                    accumulator = interval
                }
            }

            if (accumulator != (0..0)) {
                combined.add(accumulator)
            }

            return combined
        }
    }

}
