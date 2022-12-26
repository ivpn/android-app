package net.ivpn.core.common.pinger

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.OnServerListUpdatedListener
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.utils.DateUtil
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.vpn.OnProtocolChangedListener
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.controller.DefaultVPNStateListener
import net.ivpn.core.vpn.controller.VpnStateListener
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

@ApplicationScope
class PingProvider @Inject internal constructor(
    private var pingsData :PingDataSet,
    private val protocolController: ProtocolController,
    private val serversRepository: ServersRepository
) {
    private var lastCalculationTimeStamp: Long = 0

    val pings: LiveData<MutableMap<Server, PingResultFormatter?>> = pingsData.pings
    val fastestServer: LiveData<Server?> = pingsData.fastestServer
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var lastPingedProtocol: Protocol? = null
    private var needToFindNewlyFastestServer = false
    private var isConnected = false

    init {
        serversRepository.addOnServersListUpdatedListener(onServerListUpdatedListener)
        protocolController.addOnProtocolChangedListener(onProtocolChangedListener)
    }

    fun pingAll(shouldUseHardReset: Boolean, shouldResetServers: Boolean = true) {
        val currentProtocol = protocolController.currentProtocol
        if (currentProtocol == lastPingedProtocol && !(needToFindNewlyFastestServer
                    || shouldUseHardReset || isFrequencyLimitationSatisfied)
        ) {
            return
        }
        val servers = serversRepository.getServers(false)
        if (servers == null || isConnected) {
            return
        }
        lastPingedProtocol = currentProtocol
        lastCalculationTimeStamp = System.currentTimeMillis()
        scope.launch {
            pingsData.pingAll(servers, shouldResetServers)
        }
    }

    private val isFrequencyLimitationSatisfied: Boolean
        get() {
            val currentTimeStamp = System.currentTimeMillis()
            return lastCalculationTimeStamp == 0L || currentTimeStamp - lastCalculationTimeStamp > VALIDITY_PERIOD
        }

    private val onServerListUpdatedListener: OnServerListUpdatedListener
        get() = object : OnServerListUpdatedListener {
            override fun onSuccess(servers: List<Server>, isForced: Boolean) {
                if (isForced) {
                    pingAll(true)
                }
            }

            override fun onError(throwable: Throwable) {}
            override fun onError() {}
        }

    private val onProtocolChangedListener: OnProtocolChangedListener
        get() = OnProtocolChangedListener { protocol: Protocol? ->
            if (protocol == null) {
                return@OnProtocolChangedListener
            }
            pingAll(true)
        }

    val vPNStateListener: VpnStateListener
        get() = object : DefaultVPNStateListener() {
            override fun onConnectionStateChanged(state: ConnectionState?) {
                isConnected = state == ConnectionState.CONNECTED
            }
        }

    companion object {
        private const val VALIDITY_PERIOD = DateUtil.HOUR
        private val LOGGER = LoggerFactory.getLogger(PingProvider::class.java)
    }
}