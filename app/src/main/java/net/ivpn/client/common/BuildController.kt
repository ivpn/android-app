package net.ivpn.client.common

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

import android.os.Build
import net.ivpn.client.BuildConfig
import net.ivpn.client.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class BuildController @Inject constructor() {

    companion object {
        const val SITE = "site"
        const val F_DROID = "fdroid"
        const val STORE = "store"
    }

    val isStartOnBootSupported = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val isAlwaysOnVpnSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isAdvancedKillSwitchModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    //TODO increase target build SDK and use this line:
    //Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    @kotlin.jvm.JvmField
    var isSystemDefaultNightModeSupported = Build.VERSION.SDK_INT >= 29

    val isAntiTrackerSupported = BuildConfig.BUILD_VARIANT == SITE || BuildConfig.BUILD_VARIANT == F_DROID
    val isSentrySupported = BuildConfig.BUILD_VARIANT != F_DROID
    val isUpdatesSupported = BuildConfig.BUILD_VARIANT == SITE
    val isIAPEnabled = BuildConfig.BUILD_VARIANT == STORE

    init {
    }
}