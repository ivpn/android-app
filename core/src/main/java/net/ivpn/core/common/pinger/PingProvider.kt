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

import android.os.Handler
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.OnServerListUpdatedListener
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.utils.DateUtil
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.vpn.OnProtocolChangedListener
import net.ivpn.core.vpn.Protocol
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.controller.DefaultVPNStateListener
import net.ivpn.core.vpn.controller.VpnStateListener
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ApplicationScope
class PingProvider @Inject internal constructor(
    private var pingsData :PingDataSet,
    private val protocolController: ProtocolController,
    private val serversRepository: ServersRepository
) {
    private var lastCalculationTimeStamp: Long = 0
//    private var pings: HashMap<Server, PingFuture>
    val pings: LiveData<MutableMap<Server, PingResultFormatter?>> = pingsData.pings
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

//    private val pingExecutor: ExecutorService
//    private val featureExecutor: ExecutorService
    private var lastPingedProtocol: Protocol? = null
    private var needToFindNewlyFastestServer = false
    private var isConnected = false
    private val listeners: MutableList<OnFastestServerDetectorListener> = LinkedList()

    init {
        //ToDo init pings
//        pings = HashMap()
        //In the library that used to ping servers for every "ping" request creates new background thread,
        // so don't need to do it by ourselves.
//        featureExecutor = Executors.newSingleThreadExecutor()
//        pingExecutor = Executors.newFixedThreadPool(THREAD_COUNTS)
        serversRepository.addOnServersListUpdatedListener(onServerListUpdatedListener)
        protocolController.addOnProtocolChangedListener(onProtocolChangedListener)
    }

    fun pingAll(shouldUseHardReset: Boolean) {
        LOGGER.info("Ping servers if needed: should be reset $shouldUseHardReset")
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
        //ToDo clear all pings
//        pings = HashMap()
        lastCalculationTimeStamp = System.currentTimeMillis()
        scope.launch {
            pingsData.pingAll(servers)
        }
//        pingAll(servers)
    }

    fun ping(server: Server?, listener: OnPingFinishListener?) {
        if (server == null || isConnected) {
            return
        }

//        val result = pings.value[server] ?: null
//        var pingFutures = pings[server]
//        if (pingFutures == null) {
//            pingFutures = PingFuture()
//            pingFutures.addOnPingFinishListener(OnPingFinishListener { _: Server?, _: PingResultFormatter? ->
//                sendFastestServer(
//                    null
//                )
//            })
//            val ipAddress: String
//            ipAddress = if (server.type == null || server.type == Protocol.OPENVPN) {
//                server.ipAddresses[0]
//            } else {
//                server.hosts[0].host
//            }
//            featureExecutor.execute(pingFutures.getPingRunnable(server, ipAddress, listener))
//            pings[server] = pingFutures
//        } else if (pingFutures.isFinished) {
//            listener?.onPingFinish(server, pingFutures.result)
//        } else {
//            pingFutures.addOnPingFinishListener(listener)
//        }
    }

//    val pingResults: ConcurrentHashMap<Server, PingResultFormatter>
//        get() {
//            val result = ConcurrentHashMap<Server, PingResultFormatter>()
//            var pingFuture: PingFuture?
//            for (server in pings.keys) {
//                pingFuture = pings[server]
//                if (pingFuture == null || !pingFuture.isFinished) {
//                    continue
//                }
//                result[server] = pingFuture.result
//            }
//            return result
//        }

    @JvmOverloads
    fun findFastestServer(listener: OnFastestServerDetectorListener? = null) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCalculationTimeStamp > CALCULATION_PERIOD) {
//        if (isFinished || currentTime - lastCalculationTimeStamp > CALCULATION_PERIOD) {
            sendFastestServer(listener)
            return
        }
        Handler().postDelayed(
            { sendFastestServer(listener) },
            CALCULATION_PERIOD - (currentTime - lastCalculationTimeStamp)
        )
    }

    private val isFrequencyLimitationSatisfied: Boolean
        get() {
            val currentTimeStamp = System.currentTimeMillis()
            return lastCalculationTimeStamp == 0L || currentTimeStamp - lastCalculationTimeStamp > VALIDITY_PERIOD
        }

    private fun pingAll(servers: List<Server>?) {
        LOGGER.info("Pinging servers...")
        if (servers == null) {
            return
        }
        for (server in servers) {
            ping(server, null)
        }
    }

//    private val isFinished: Boolean
//        get() {
//            for (future in pings.values) {
//                if (!future.isFinished) {
//                    return false
//                }
//            }
//            return true
//        }

    private fun innerFindFastestServer(): Server {
        LOGGER.info("Finding fastest server...")
        val possibleServersList = serversRepository.getPossibleServersList()
        val fastestServer = possibleServersList[0]
//        var fastestPing: Long = -1
//        var serverPing: Long
//        for (server in pings.keys) {
//            if (!possibleServersList.contains(server)) {
//                continue
//            }
//            serverPing = getPingFor(server)
//            if (serverPing == -1L) {
//                continue
//            }
//            if (fastestPing == -1L || fastestPing > serverPing) {
//                fastestPing = serverPing
//                fastestServer = server
//            }
//        }
        return fastestServer
    }

//    private fun getPingFor(server: Server): Long {
//        val future = pings[server]
//        if (!future!!.isFinished) {
//            return -1
//        }
//        return if (future.result == null || !future.result.isPingAvailable) {
//            -1
//        } else future.result.ping
//    }

    private fun sendFastestServer(listener: OnFastestServerDetectorListener?) {
        var fastestServer: Server? = innerFindFastestServer()
        println("Found new fastest server = " + fastestServer!!.description)
        if (fastestServer == null) {
            LOGGER.info("Send default server as fastest one")
            needToFindNewlyFastestServer = true
            fastestServer = defaultServer
            for (fListener in listeners) {
                fListener.onDefaultServerApplied(fastestServer)
            }
            listener?.onDefaultServerApplied(fastestServer)
        } else {
            LOGGER.info("Send fastest server")
            needToFindNewlyFastestServer = false
            for (fListener in listeners) {
                fListener.onDefaultServerApplied(fastestServer)
            }
            listener?.onFastestServerDetected(fastestServer)
        }
    }

    private val defaultServer: Server?
        get() = serversRepository.getDefaultServer(ServerType.ENTRY)

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

    fun subscribe(listener: OnFastestServerDetectorListener) {
        listeners.add(listener)
    }

    companion object {
        private const val VALIDITY_PERIOD = DateUtil.HOUR
        private const val CALCULATION_PERIOD = (3 * 1000).toLong()
        private const val THREAD_COUNTS = 5
        private val LOGGER = LoggerFactory.getLogger(PingProvider::class.java)
    }
}