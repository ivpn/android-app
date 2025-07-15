package net.ivpn.core.vpn.controller

import android.util.Log
import libV2ray.CoreCallbackHandler
import libV2ray.CoreController
import libV2ray.LibV2ray
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.common.prefs.ServersPreference
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
) : CoreCallbackHandler {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(V2rayController::class.java)
        private const val V2RAY_LOCAL_HOST = "127.0.0.1"
        private const val V2RAY_LOCAL_PORT_BASE = 16661
    }

    private val controller: CoreController by lazy {
        LibV2ray.newCoreController(this)
    }
    
    @Volatile
    private var isRunning = false
    
    @Volatile
    private var currentLocalPort = 0


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

        LOGGER.debug("V2Ray obfuscation status: ${if (isEnabled) "enabled (${obfuscationType.name})" else "disabled"}")
        
        return isEnabled
    }


    fun startIfEnabled(): Boolean {
        if (!isV2RayEnabled()) {
            LOGGER.debug("V2Ray is disabled, skipping start")
            return false
        }

        if (isRunning) {
            LOGGER.info("V2Ray is already running on port $currentLocalPort")
            return true
        }

        return start()
    }


    fun start(): Boolean {
        try {
            currentLocalPort = findFreePort()
            LOGGER.info("V2Ray allocated local port: $currentLocalPort")
            
            val config = makeConfig()
            if (config == null) {
                LOGGER.error("Failed to create V2Ray configuration")
                return false
            }

            config.setLocalPort(currentLocalPort, false)

            val validationError = config.isValid()
            if (validationError != null) {
                LOGGER.error("V2Ray configuration validation failed: $validationError")
                return false
            }

            LOGGER.info("Starting V2Ray proxy service:")
            LOGGER.info("  Local endpoint: ${V2RAY_LOCAL_HOST}:${currentLocalPort}")
            LOGGER.info("  Obfuscation type: ${encryptedSettingsPreference.obfuscationType.name}")
            controller.startLoop(config.jsonString())
            isRunning = true
            LOGGER.info("V2Ray started successfully - traffic will be routed through local proxy")
            return true

        } catch (e: Exception) {
            LOGGER.error("Failed to start V2Ray: ${e.message}", e)
            cleanup()
            return false
        }
    }


    fun stop() {
        try {
            if (controller.isRunning) {
                LOGGER.info("Stopping V2Ray proxy service")
                controller.stopLoop()
            }
            cleanup()
            LOGGER.info("V2Ray stopped successfully")
        } catch (e: Exception) {
            LOGGER.error("Error stopping V2Ray: ${e.message}", e)
            cleanup()
        }
    }


    fun getLocalProxyEndpoint(): String {
        return "$V2RAY_LOCAL_HOST:$currentLocalPort"
    }


    private fun findFreePort(): Int {
        return try {
            val port = LibV2ray.getFreePort().toInt()
            if (port > 0) {
                LOGGER.info("libV2ray allocated free port: $port")
                port
            } else {
                LOGGER.warn("libV2ray returned invalid port, using base port $V2RAY_LOCAL_PORT_BASE")
                V2RAY_LOCAL_PORT_BASE
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to get free port from libV2ray: ${e.message}, using base port $V2RAY_LOCAL_PORT_BASE")
            V2RAY_LOCAL_PORT_BASE
        }
    }


    private fun cleanup() {
        isRunning = false
        currentLocalPort = 0
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