package net.ivpn.core.common.distance

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

import kotlinx.coroutines.*
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.map.model.Location
import net.ivpn.core.vpn.OnProtocolChangedListener
import net.ivpn.core.vpn.ProtocolController
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ApplicationScope
class DistanceProvider @Inject constructor(
    private val serversRepository: ServersRepository,
    protocolController: ProtocolController
) {
    val distances = HashMap<Server, Float>()

    var homeLocation: Location? = null
    var previousCalculationJob: Job? = null

    val distanceCalculationScope = CoroutineScope(Dispatchers.Default)
    val listeners = mutableSetOf<OnDistanceChangedListener>()

    init {
        protocolController.addOnProtocolChangedListener(getOnProtocolChangeListener())
    }

    fun updateLocation(location: Location?) {
        location?.let {
            homeLocation = it
            if (distances.isNotEmpty()) {
                calculateDistances()
            }
        }
    }

    fun subscribe(listener: OnDistanceChangedListener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: OnDistanceChangedListener) {
        listeners.remove(listener)
    }

    private fun getOnProtocolChangeListener(): OnProtocolChangedListener {
        return OnProtocolChangedListener {
            distances.clear()
            serversRepository.getServers(false)?.let {
                distances.clear()
                for (server in it) {
                    distances[server] = Float.MAX_VALUE
                }

                if (homeLocation != null) {
                    calculateDistances()
                }
            }
        }
    }

    private fun calculateDistances() {
        previousCalculationJob?.cancel()

        previousCalculationJob = distanceCalculationScope.launch {
            homeLocation?.let {
                for ((server, _) in distances) {
                    distances[server] = getDistanceBetween(
                        it.latitude,
                        it.longitude, server.latitude.toFloat(), server.longitude.toFloat()
                    )
                }

                withContext(Dispatchers.Main) {
                    listeners.forEach { listener -> listener.onDistanceChanged() }
                }
            }
        }
    }

    private suspend fun getDistanceBetween(
        latitude1: Float,
        longitude1: Float,
        latitude2: Float,
        longitude2: Float
    ): Float {
        val radiusEarth = 6371
        val dLat = deg2rad(latitude2 - latitude1)
        val dLon = deg2rad(longitude2 - longitude1)
        val a = sin(dLat / 2f) * sin(dLat / 2f) +
                cos(deg2rad(latitude1)) * cos(deg2rad(latitude2)) *
                sin(dLon / 2f) * sin(dLon / 2f)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radiusEarth * c // Distance in km
    }

    private fun deg2rad(degrees: Float): Float {
        return (degrees * (Math.PI / 180f)).toFloat()
    }
}