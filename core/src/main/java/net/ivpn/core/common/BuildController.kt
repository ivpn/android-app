package net.ivpn.core.common

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
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class BuildController @Inject constructor() {

    companion object {
        const val PRODUCTION = "production"
        const val STAGE = "stage"
    }

    val isStartOnBootSupported = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val isAlwaysOnVpnSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isAdvancedKillSwitchModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    var isSystemDefaultNightModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun getBaseUrl(): String {
        return IVPNApplication.config.urlAPI
    }
}