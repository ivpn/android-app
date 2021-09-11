package net.ivpn.core.common.pinger

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

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

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.pinger.Ping.PingListener
import net.ivpn.core.rest.data.model.Server
import javax.inject.Inject

@ApplicationScope
class PingDataSet @Inject constructor() {
    val pings: MutableLiveData<MutableMap<Server, PingResultFormatter?>> = MutableLiveData()

    private var _pings: MutableMap<Server, PingResultFormatter?> = mutableMapOf()

    //ToDo try to use coroutineScope
    val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    suspend fun pingAll(servers: List<Server>) {
        scope.launch {
            clear()
            for (server in servers) {
                launch {
                    innerPing(server)
                }
            }
        }
    }

    suspend fun ping(server: Server) {
        scope.launch {
            updatePingFor(server, null)
            launch {
                innerPing(server)
            }
        }
    }

    private fun innerPing(server: Server) {
        //ToDo change inner Ping class implementation
        Ping.onAddress(server.ipAddress)
            .setTimeOutMillis(TIMEOUT)
            .setTimes(TIMES)
            .doPing(object : PingListener {
                override fun onResult(pingResult: PingResult) {}
                override fun onFinished(pingStats: PingStats) {
                    val result = if (pingStats.packetsLost == TIMES.toLong()) {
                        PingResultFormatter(PingResultFormatter.PingResult.OFFLINE, -1)
                    } else {
                        PingResultFormatter(
                            PingResultFormatter.PingResult.OK,
                            pingStats.minTimeTaken.toLong()
                        )
                    }
                    scope.launch {
                        updatePingFor(server, result)
                    }
                }

                override fun onError(e: Exception) {
                    scope.launch {
                        updatePingFor(server, PingResultFormatter(PingResultFormatter.PingResult.OFFLINE, -1))
                    }
                    e.printStackTrace()
                }
            })
    }

    private suspend fun updatePingFor(server: Server, ping: PingResultFormatter?) {
        mutex.withLock {
            val newMap = _pings.toMutableMap()
            newMap[server] = ping
            _pings = newMap
            pings.postValue(newMap)
        }
    }

    private suspend fun clear() {
        mutex.withLock {
            _pings = mutableMapOf()
            pings.postValue(_pings)
        }
    }

    companion object {
        private const val TIMES = 2
        private const val TIMEOUT = 1000
    }
}