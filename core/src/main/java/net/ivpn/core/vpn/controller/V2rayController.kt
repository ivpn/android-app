package net.ivpn.core.vpn.controller

import libV2ray.CoreCallbackHandler
import libV2ray.CoreController
import libV2ray.LibV2ray
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.vpn.model.ObfuscationType
import net.ivpn.core.vpn.model.V2RayConfig
import net.ivpn.core.vpn.model.V2RaySettingsController
import javax.inject.Inject

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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

@ApplicationScope
class V2rayController @Inject constructor(
    private val encryptedSettingsPreference: EncryptedSettingsPreference,
    private val v2RaySettingsController: V2RaySettingsController
) : CoreCallbackHandler {

    private val controller: CoreController by lazy {
        LibV2ray.newCoreController(this)
    }

    fun makeConfig(): V2RayConfig? {
        val settings = v2RaySettingsController.load() ?: return null
        val obfuscationType = encryptedSettingsPreference.obfuscationType

        return when (obfuscationType) {
            ObfuscationType.V2RAY_TCP -> V2RayConfig.createTcp(
                context = IVPNApplication.application,
                outboundIp = settings.outboundIp,
                outboundPort = settings.outboundPort,
                inboundIp = settings.inboundIp,
                inboundPort = settings.inboundPort,
                outboundUserId = settings.id
            )

            ObfuscationType.V2RAY_QUIC -> V2RayConfig.createQUIC(
                context = IVPNApplication.application,
                outboundIp = settings.outboundIp,
                outboundPort = settings.outboundPort,
                inboundIp = settings.inboundIp,
                inboundPort = settings.inboundPort,
                outboundUserId = settings.id,
                tlsSrvName = settings.tlsSrvName
            )

            ObfuscationType.DISABLED -> null
        }
    }

    fun start() {
        val config = makeConfig() ?: return
        controller.startLoop(config.jsonString())
    }

    fun stop() {
        if (controller.isRunning) {
            controller.stopLoop()
        }
    }

    override fun onEmitStatus(p0: Long, p1: String?): Long {
        return 0
    }

    override fun shutdown(): Long {
        return 0
    }

    override fun startup(): Long {
        return 0
    }
}