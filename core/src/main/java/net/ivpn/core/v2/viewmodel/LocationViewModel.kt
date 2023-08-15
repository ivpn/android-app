package net.ivpn.core.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
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

import android.os.Handler
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.distance.DistanceProvider
import net.ivpn.core.common.prefs.OnServerListUpdatedListener
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.HttpClientFactory
import net.ivpn.core.rest.IVPNApi
import net.ivpn.core.rest.RequestListener
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.rest.data.proofs.LocationResponse
import net.ivpn.core.rest.requests.common.Request
import net.ivpn.core.rest.requests.common.RequestWrapper
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.v2.map.model.Location
import net.ivpn.core.vpn.OnProtocolChangedListener
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.controller.VpnBehaviorController
import net.ivpn.core.vpn.controller.VpnStateListener
import net.ivpn.core.vpn.controller.VpnStateListenerImpl
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class LocationViewModel @Inject constructor(
        private val serversRepository: ServersRepository,
        private val settings: Settings,
        private val httpClientFactory: HttpClientFactory,
        private val distanceProvider: DistanceProvider,
        protocolController: ProtocolController,
        vpnBehaviorController: VpnBehaviorController
) : ViewModel() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LocationViewModel::class.java)
    }

    val locations = ObservableField<List<ServerLocation>?>()
    var state: ConnectionState? = null

    val locationUIData = ObservableField<LocationUIData>()

    private var v4locationUIData: LocationUIData? = null
    private var v6locationUIData: LocationUIData? = null

    val isLocationAPIError = ObservableBoolean()

    private var v4LocationAPIError = false
    private var v6LocationAPIError = false

    val dataLoading = ObservableBoolean()

    private var v4dataLoading = false
    private var v6dataLoading = false

    var uiListener: LocationUpdatesUIListener? = null
    var uiIPStateListener: OnIPStateChangedListener? = null

    private var locationListeners = arrayListOf<CheckLocationListener>()

    private var requestV4: Request<LocationResponse>? = null
    private var requestV6: Request<LocationResponse>? = null

    private val homeLocation = ObservableField<Location>()

    private var homeV4Location: Location? = null
    private var homeV6Location: Location? = null

    var ipState = ObservableField(IPState.IPv4)

    val isIPv6Available = ObservableBoolean()
    val isIPv6MapUIAvailable = ObservableBoolean()
    val isLocationNotMatch = ObservableBoolean()

    init {
        vpnBehaviorController.addVpnStateListener(getVpnStateListener())
        checkLocation()
        protocolController.addOnProtocolChangedListener(getOnProtocolChangeListener())
        serversRepository.addOnServersListUpdatedListener(getOnServerListUpdatedListener())
        uiIPStateListener = object : OnIPStateChangedListener {
            override fun onIPStateChanged(uiState: IPState) {
                if (uiState == ipState.get()) return

                when (uiState) {
                    IPState.IPv4 -> {
                        LOGGER.info("Set data loading as ${v4dataLoading}")
                        dataLoading.set(v4dataLoading)
                        isLocationAPIError.set(v4LocationAPIError)
                        homeLocation.set(homeV4Location)
                        locationUIData.set(v4locationUIData)
                        distanceProvider.updateLocation(homeV4Location)
                    }
                    IPState.IPv6 -> {
                        LOGGER.info("Set data loading as ${v6dataLoading}")
                        dataLoading.set(v6dataLoading)
                        isLocationAPIError.set(v6LocationAPIError)
                        homeLocation.set(homeV6Location)
                        locationUIData.set(v6locationUIData)
                        distanceProvider.updateLocation(homeV6Location)
                    }
                }
                ipState.set(uiState)

                val location = homeLocation.get()
                val stateL = state
                if (location != null && stateL != null
                        && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
                    for (listener in locationListeners) {
                        listener.onSuccess(location, stateL)
                    }
                }
            }
        }
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
        isIPv6Available.set(false)
        isIPv6MapUIAvailable.set(false)
        isLocationAPIError.set(false)
        isLocationNotMatch.set(false)

        v4locationUIData = null
        v6locationUIData = null

        v4LocationAPIError = false
        v6LocationAPIError = false

        uiListener?.onLocationUpdated()
        requestV4?.cancel()
        requestV4 = Request(settings, httpClientFactory, serversRepository, Request.Duration.SHORT, RequestWrapper.IpMode.IPv4)
        requestV6?.cancel()
        requestV6 = Request(settings, httpClientFactory, serversRepository, Request.Duration.SHORT, RequestWrapper.IpMode.IPv6)

        LOGGER.info("Checking location...")
        v4dataLoading = true
        v6dataLoading = true
        LOGGER.info("Set data loading as true")
        dataLoading.set(true)

        val location = homeLocation.get()
        val stateL = state
        if (location != null && stateL != null
                && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
            for (listener in locationListeners) {
                listener.onSuccess(location, stateL)
            }
        }
        requestV4?.start({ obj: IVPNApi -> obj.location }, object : RequestListener<LocationResponse?> {
            override fun onSuccess(response: LocationResponse?) {
                LOGGER.info("IPv4 location = $response")
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
                LOGGER.error("IPv4 Error while updating location $string")
                LOGGER.error("Error while updating location ", string)
                for (listener in locationListeners) {
                    listener.onError()
                }
                this@LocationViewModel.onError()
            }
        })
        requestV6?.start({ obj: IVPNApi -> obj.location }, object : RequestListener<LocationResponse?> {
            override fun onSuccess(response: LocationResponse?) {
                LOGGER.info("IPv6 location = $response")
                this@LocationViewModel.onSuccessV6(response)
                uiListener?.onLocationUpdated()
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("IPv6 Error while updating location $throwable")
                this@LocationViewModel.onErrorV6()
            }

            override fun onError(string: String) {
                LOGGER.error("IPv6 Error while updating location ", string)
                this@LocationViewModel.onErrorV6()
            }
        })
    }

    fun reset() {
    }

    private fun onSuccess(response: LocationResponse?) {
        v4dataLoading = false
        if (isIPv4InfoActive()) {
            LOGGER.info("Set data loading as ${v4dataLoading}")
            dataLoading.set(v4dataLoading)
        }
        response?.let {
            val stateL = state
            if (stateL != null
                    && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
                homeV4Location = Location(it.longitude.toFloat(),
                        it.latitude.toFloat(),
                        false,
                        "${it.city}",
                        "${it.country}",
                        it.countryCode)
                if (isIPv4InfoActive()) {
                    homeV4Location?.let { location ->
                        homeLocation.set(location)
                        for (listener in locationListeners) {
                            listener.onSuccess(location, stateL)
                        }
                        distanceProvider.updateLocation(location)
                    }
                }
            }

            val locationStr = if (it.city != null && it.city.isNotEmpty()) {
                it.getLocation()
            } else {
                it.country
            }
            v4locationUIData = LocationUIData(it.ipAddress, locationStr, if (it.isIvpnServer) "IVPN" else it.isp)
            v6locationUIData?.let {v6location ->
                isLocationNotMatch.set(isIPv6Available.get() && (locationStr != v6location.location))
            }

            if (isIPv4InfoActive()) {
                locationUIData.set(v4locationUIData)
            }

        }
    }

    private fun onSuccessV6(response: LocationResponse?) {
        v6dataLoading = false
        isIPv6Available.set(true)

        if (!isIPv4InfoActive()) {
            LOGGER.info("Set data loading as ${v6dataLoading}")
            dataLoading.set(v6dataLoading)
        }
        response?.let {
            println("IPv6 response = ${it}")
            val stateL = state

            if (stateL != null
                    && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
                homeV6Location = Location(it.longitude.toFloat(),
                        it.latitude.toFloat(),
                        false,
                        "${it.city}",
                        "${it.country}",
                        it.countryCode)
                if (!isIPv4InfoActive()) {
                    homeV6Location?.let { location ->
                        homeLocation.set(location)
                        for (listener in locationListeners) {
                            listener.onSuccess(location, stateL)
                        }
                        distanceProvider.updateLocation(location)
                    }
                }
            }

            val locationStr = if (it.city != null && it.city.isNotEmpty()) {
                it.getLocation()
            } else {
                it.country
            }

            v6locationUIData = LocationUIData(it.ipAddress, locationStr, if (it.isIvpnServer) "IVPN" else it.isp)
            if (!isIPv4InfoActive()) {
                locationUIData.set(v6locationUIData)
            }

            v4locationUIData?.let {v4location ->
                isLocationNotMatch.set(isIPv6Available.get() && (locationStr != v4location.location))
            }

            if (stateL != null
                    && (stateL == ConnectionState.NOT_CONNECTED || stateL == ConnectionState.PAUSED)) {
                isIPv6MapUIAvailable.set(true)
            }
        }
    }

    private fun onError() {
        v4dataLoading = false
        v4LocationAPIError = true
        if (isIPv4InfoActive()) {
            LOGGER.info("Set data loading as ${v4dataLoading}")
            dataLoading.set(v4dataLoading)
            isLocationAPIError.set(v4LocationAPIError)
        }

        v4locationUIData = LocationUIData("Connection error",
                "Connection error",
                "Connection error")
        if (isIPv4InfoActive()) {
            locationUIData.set(v4locationUIData)
        }
        uiListener?.onLocationUpdated()
    }

    private fun onErrorV6() {
        v6dataLoading = false
        v6LocationAPIError = true
        if (!isIPv4InfoActive()) {
            LOGGER.info("Set data loading as ${v6dataLoading}")
            dataLoading.set(v6dataLoading)
            isLocationAPIError.set(v6LocationAPIError)
        }

        v6locationUIData = LocationUIData("Connection error",
                "Connection error",
                "Connection error")
        if (!isIPv4InfoActive()) {
            locationUIData.set(v6locationUIData)
        }

        uiListener?.onLocationUpdated()
    }

    private fun isIPv4InfoActive(): Boolean {
        return ipState.get() == IPState.IPv4 || !isIPv6Available.get()
    }

    private fun getOnProtocolChangeListener(): OnProtocolChangedListener {
        return OnProtocolChangedListener { locations.set(serversRepository.locations) }
    }

    private fun getOnServerListUpdatedListener(): OnServerListUpdatedListener {
        return object : OnServerListUpdatedListener {
            override fun onSuccess(servers: List<Server>, isForced: Boolean) {
                locations.set(serversRepository.locations)
            }
            override fun onError(throwable: Throwable) {}
            override fun onError() {}
        }
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

    enum class IPState {
        IPv4,
        IPv6
    }

    interface OnIPStateChangedListener {
        fun onIPStateChanged(uiState: IPState)
    }

    data class LocationUIData(val ip: String?, val location: String?, val isp: String?)
}