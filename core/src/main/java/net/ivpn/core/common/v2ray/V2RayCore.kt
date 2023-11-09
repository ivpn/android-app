package net.ivpn.core.common.v2ray

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
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

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class V2RayCore @Inject constructor(
    private val settings: Settings
) {

    fun start(): Error? {
        close()
        // var error: Error? = null
        // TODO: Start V2Ray
        val config = makeConfig()
        return null
    }

    fun close(): Error? {
        // var error: Error? = null
        // TODO: Stop V2Ray
        return null
    }

    private fun makeConfig(): V2RayConfig? {
        val v2raySettings = settings.v2raySettings
        if (v2raySettings != null) {
            return V2RayConfig.createQuick(
                v2raySettings.outboundIp,
                v2raySettings.outboundPort,
                v2raySettings.inboundIp,
                v2raySettings.inboundPort,
                v2raySettings.id,
                v2raySettings.tlsSrvName
            )
        }
        return null
    }

}
