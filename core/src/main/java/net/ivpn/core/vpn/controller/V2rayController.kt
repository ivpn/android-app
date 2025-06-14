package net.ivpn.core.vpn.controller

import libV2ray.CoreCallbackHandler
import libV2ray.CoreController
import libV2ray.LibV2ray
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.common.prefs.ServersPreference
import net.ivpn.core.v2.serverlist.ServerListTabFragment.Companion.LOGGER
import net.ivpn.core.vpn.model.ObfuscationType
import net.ivpn.core.vpn.model.V2RayConfig
import net.ivpn.core.vpn.model.V2RaySettings
import org.slf4j.LoggerFactory
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
    private val serversPreference: ServersPreference
)  : CoreCallbackHandler {

    private val controller: CoreController by lazy {
        LibV2ray.newCoreController(this)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(V2rayController::class.java)
        const val V2RAY_LOCAL_HOST = "127.0.0.1"
        const val V2RAY_LOCAL_PORT = 16661  // Same as iOS
    }

    private var isRunning = false

    fun makeConfig(): V2RayConfig? {
        val settings = serversPreference.getV2RaySettings() ?: return null
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


    fun isV2RayEnabled(): Boolean {
        val obfuscationType = encryptedSettingsPreference.obfuscationType
        val isEnabled = obfuscationType != ObfuscationType.DISABLED

        if (isEnabled) {
            LOGGER.info("V2Ray obfuscation is enabled: ${obfuscationType.name}")
        } else {
            LOGGER.debug("V2Ray obfuscation is disabled")
        }

        return isEnabled
    }


    fun startIfEnabled(): Boolean {
        if (!isV2RayEnabled()) {
            LOGGER.debug("V2Ray is disabled, skipping start")
            return false
        }

        if (isRunning) {
            LOGGER.info("V2Ray is already running")
            return true
        }

        return start()
    }

    fun start(): Boolean {
        try {
            val config = makeConfig()
            if (config == null) {
                LOGGER.error("Failed to create V2Ray configuration")
                return false
            }

            val validationError = config.isValid()
            if (validationError != null) {
                LOGGER.error("V2Ray configuration is invalid: $validationError")
                return false
            }

            LOGGER.info("Starting V2Ray with configuration:")
            LOGGER.info("  Local proxy: ${V2RAY_LOCAL_HOST}:${V2RAY_LOCAL_PORT}")
            LOGGER.info("  Obfuscation: ${encryptedSettingsPreference.obfuscationType.name}")

            controller.startLoop(config.jsonString())
            isRunning = true

            LOGGER.info("V2Ray started successfully")
            return true

        } catch (e: Exception) {
            LOGGER.error("Failed to start V2Ray: ${e.message}", e)
            isRunning = false
            return false
        }
    }
    fun stop() {
        try {
            if (controller.isRunning) {
                LOGGER.info("Stopping V2Ray")
                controller.stopLoop()
            }
            isRunning = false
            LOGGER.info("V2Ray stopped")
        } catch (e: Exception) {
            LOGGER.error("Error stopping V2Ray: ${e.message}", e)
            isRunning = false
        }
    }


    fun getLocalProxyEndpoint(): String {
        return "$V2RAY_LOCAL_HOST:$V2RAY_LOCAL_PORT"
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