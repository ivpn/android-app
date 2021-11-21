package net.ivpn.core.v2.mocklocation

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

import android.app.AppOpsManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.*
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.multihop.MultiHopController
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.rest.data.model.Server
import javax.inject.Inject

@ApplicationScope
class MockLocationController @Inject constructor(
        private val settingsPreference: EncryptedSettingsPreference,
        private val serversRepository: ServersRepository,
        private val multiHopController: MultiHopController
) {
    var isEnabled: Boolean = false
    var isTestProviderAdded = false

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var manager: LocationManager

    init {
        val context = IVPNApplication.application
        manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (isDeveloperOptionsEnabled() && isMockLocationFeatureEnabled()) {
            isEnabled = settingsPreference.mockLocationSettings
        }
    }

    fun enableMockLocation(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        settingsPreference.mockLocationSettings = isEnabled
    }

    fun isDeveloperOptionsEnabled(): Boolean {
        val context = IVPNApplication.application
        val developerSettings = android.provider.Settings.Secure.getInt(context.contentResolver,
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        return developerSettings != 0
    }

    fun isMockLocationEnabled(): Boolean {
        return isEnabled && isMockLocationFeatureEnabled()
    }

    fun isMockLocationFeatureEnabled(): Boolean {
        var isMockLocation = false
        val context = IVPNApplication.application

        isMockLocation = try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val opsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), IVPNApplication.config.applicationId) == AppOpsManager.MODE_ALLOWED
            } else {
                // in marshmallow this will always return true
                android.provider.Settings.Secure.getString(context.contentResolver, "mock_location") != "0"
            }
        } catch (e: Exception) {
            return isMockLocation
        }
        return isMockLocation
    }

    fun mock() {
        if (!isEnabled) {
            return
        }

        getServer()?.let {
            mockLocationWith(it)
        }
    }

    private fun mockLocationWith(server: Server) {
        addTestProviders(LocationManager.GPS_PROVIDER)
        addTestProviders(LocationManager.NETWORK_PROVIDER)
        isTestProviderAdded = true
        runnable = Runnable {
            if (isMockLocationFeatureEnabled()) {
                setMock(LocationManager.GPS_PROVIDER, server.latitude, server.longitude)
                setMock(LocationManager.NETWORK_PROVIDER, server.latitude, server.longitude)
            }
            runnable?.let {
                handler.postDelayed(it, 50)
            }
        }

        runnable?.let {
            handler.post(it)
        }
    }

    fun stop() {
        runnable?.let {
            handler.removeCallbacks(it)
        }
        if (isMockLocationFeatureEnabled() && isTestProviderAdded) {
            removeProvider(LocationManager.GPS_PROVIDER)
            removeProvider(LocationManager.NETWORK_PROVIDER)
            isTestProviderAdded = false
        }
    }

    fun reset() {
        isEnabled = false
    }

    private fun removeProvider(provider: String) {
        try {
            manager.removeTestProvider(provider)
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun getServer(): Server? {
        return if (multiHopController.getIsEnabled()) {
            serversRepository.getCurrentServer(ServerType.EXIT)
        } else {
            serversRepository.getCurrentServer(ServerType.ENTRY)
        }
    }

    private fun addTestProviders(provider: String) {
        try {
            manager.addTestProvider(provider, false, false,
                    false, false, false, true,
                    true, 1, 2)
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun setMock(provider: String, latitude: Double, longitude: Double) {
        val newLocation = Location(provider)
        newLocation.latitude = latitude
        newLocation.longitude = longitude
        newLocation.altitude = 3.0
        newLocation.time = System.currentTimeMillis()
        newLocation.speed = 0.01f
        newLocation.bearing = 1f
        newLocation.accuracy = 3f
        newLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newLocation.bearingAccuracyDegrees = 0.1f
            newLocation.verticalAccuracyMeters = 0.1f
            newLocation.speedAccuracyMetersPerSecond = 0.01f
        }
        manager.setTestProviderEnabled(provider, true)
        manager.setTestProviderLocation(provider, newLocation)
    }
}