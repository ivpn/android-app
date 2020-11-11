package net.ivpn.client.v2.viewmodel

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
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.rest.data.proofs.LocationResponse
import net.ivpn.client.rest.requests.common.Request
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.v2.map.model.Location
import net.ivpn.client.vpn.OnProtocolChangedListener
import net.ivpn.client.vpn.ProtocolController
import net.ivpn.client.vpn.controller.VpnBehaviorController
import net.ivpn.client.vpn.controller.VpnStateListener
import net.ivpn.client.vpn.controller.VpnStateListenerImpl
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class LocationViewModel @Inject constructor(
        private val serversRepository: ServersRepository,
        private val settings: Settings,
        private val httpClientFactory: HttpClientFactory,
        protocolController: ProtocolController,
        vpnBehaviorController: VpnBehaviorController
) : ViewModel() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LocationViewModel::class.java)
    }

    val dataLoading = ObservableBoolean()
    val locations = ObservableField<List<ServerLocation>?>()
    private val homeLocation = ObservableField<Location>()
    var state: ConnectionState? = null

    val ip = ObservableField<String>()
    val location = ObservableField<String>()
    val isp = ObservableField<String>()

    val isLocationAPIError = ObservableBoolean()

    var uiListener: LocationUpdatesUIListener? = null

    private var locationListeners = arrayListOf<CheckLocationListener>()

    private var request: Request<LocationResponse>? = null

    init {
        vpnBehaviorController.addVpnStateListener(getVpnStateListener())
        checkLocation()
        protocolController.addOnProtocolChangedListener(getOnProtocolChangeListener())
    }

    fun addLocationListener(listener: CheckLocationListener) {
        locationListeners.add(listener)
        val location = homeLocation.get()
        val stateL = state
        if (location != null && stateL != null
                && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
            listener.onSuccess(location, stateL)
        }
    }

    fun removeLocationListener(listener: CheckLocationListener) {
        locationListeners.remove(listener)
    }

    fun checkLocation() {
        isLocationAPIError.set(false)
        uiListener?.onLocationUpdated()
        request?.cancel()
        request = Request(settings, httpClientFactory, serversRepository, Request.Duration.SHORT)
        LOGGER.info("Checking location...")
        dataLoading.set(true)
        val location = homeLocation.get()
        val stateL = state
        if (location != null && stateL != null
                && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
            for (listener in locationListeners) {
                listener.onSuccess(location, stateL)
            }
        }
        request?.start({ obj: IVPNApi -> obj.location }, object : RequestListener<LocationResponse?> {
            override fun onSuccess(response: LocationResponse?) {
                LOGGER.info(response.toString())
                this@LocationViewModel.onSuccess(response)
                uiListener?.onLocationUpdated()
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("Error while updating location ", throwable)
                for (listener in locationListeners) {
                    listener.onError()
                }
                this@LocationViewModel.onError()
            }

            override fun onError(string: String) {
                LOGGER.error("Error while updating location ", string)
                for (listener in locationListeners) {
                    listener.onError()
                }
                this@LocationViewModel.onError()
            }
        })
    }

    fun reset() {
    }

    private fun onSuccess(response: LocationResponse?) {
        dataLoading.set(false)
        response?.let {
            val stateL = state
            if (stateL != null
                    && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
                val newLocation = Location(it.longitude.toFloat(),
                        it.latitude.toFloat(),
                        false,
                        "${it.city}",
                        "${it.country}",
                        it.countryCode)
                homeLocation.set(newLocation)
                for (listener in locationListeners) {
                    listener.onSuccess(newLocation, stateL)
                }
            }

            ip.set(it.ipAddress)
            if (it.city != null && it.city.isNotEmpty()) {
                location.set(it.getLocation())
            } else {
                location.set(it.country)
            }
            isp.set(if (it.isIvpnServer) "IVPN" else it.isp)
//            isp.set(it.isp)

        }
    }

    private fun onError() {
        dataLoading.set(false)
        isLocationAPIError.set(true)

        ip.set("Connection error")
        isp.set("Connection error")
        location.set("Connection error")
        uiListener?.onLocationUpdated()
    }

    private fun getOnProtocolChangeListener(): OnProtocolChangedListener {
        return OnProtocolChangedListener { locations.set(serversRepository.locations) }
    }

    private fun getVpnStateListener(): VpnStateListener {
        return object : VpnStateListenerImpl() {
            override fun onConnectionStateChanged(state: ConnectionState) {
                LOGGER.info("Get VPN connection state: $state")
                this@LocationViewModel.state = state
                when (state) {
                    ConnectionState.NOT_CONNECTED -> {
                        checkLocationWithDelay()
                    }
                    ConnectionState.DISCONNECTING -> {
                        val location = homeLocation.get()
                        if (location != null) {
                            for (listener in locationListeners) {
                                listener.onSuccess(location, state)
                            }
                        }
                    }
                    ConnectionState.CONNECTED -> {
                        checkLocationWithDelay()
                    }
                    ConnectionState.PAUSED -> {
                        checkLocationWithDelay()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun checkLocationWithDelay() {
        dataLoading.set(true)
        Handler().postDelayed({
            checkLocation()
        }, 1000)
    }

    interface CheckLocationListener {
        fun onSuccess(location: Location, connectionState: ConnectionState)

        fun onError()
    }

    interface LocationUpdatesUIListener {
        fun onLocationUpdated()
    }
}