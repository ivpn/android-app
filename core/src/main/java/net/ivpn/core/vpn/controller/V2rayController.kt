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

/**
 * V2Ray controller for managing V2Ray proxy connections.
 * 
 * This controller handles:
 * - Starting and stopping V2Ray proxy service
 * - Dynamic port allocation to avoid conflicts
 * - Configuration validation and error handling
 * - Integration with WireGuard for obfuscated connections
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

    /**
     * Creates V2Ray configuration based on current settings.
     *
     * @return V2Ray configuration or null if V2Ray is disabled or settings are invalid
     */
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

    /**
     * Checks if V2Ray obfuscation is enabled based on current settings.
     *
     * @return true if V2Ray is enabled, false otherwise
     */
    fun isV2RayEnabled(): Boolean {
        val obfuscationType = encryptedSettingsPreference.obfuscationType
        val isEnabled = obfuscationType != ObfuscationType.DISABLED

        LOGGER.debug("V2Ray obfuscation status: ${if (isEnabled) "enabled (${obfuscationType.name})" else "disabled"}")
        
        return isEnabled
    }

    /**
     * Starts V2Ray if it is enabled in settings.
     *
     * @return true if V2Ray started successfully or was already running, false otherwise
     */
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

    /**
     * Starts V2Ray proxy service.
     *
     * @return true if V2Ray started successfully, false otherwise
     */
    fun start(): Boolean {
        try {
            // Find a free port for V2Ray local proxy
            currentLocalPort = findFreePort()
            LOGGER.info("V2Ray allocated local port: $currentLocalPort")
            
            val config = makeConfig()
            if (config == null) {
                LOGGER.error("Failed to create V2Ray configuration")
                return false
            }

            // Update config to use the dynamic port (false = UDP)
            config.setLocalPort(currentLocalPort, false)

            val validationError = config.isValid()
            if (validationError != null) {
                LOGGER.error("V2Ray configuration validation failed: $validationError")
                return false
            }

            LOGGER.info("Starting V2Ray proxy service:")
            LOGGER.info("  Local endpoint: ${V2RAY_LOCAL_HOST}:${currentLocalPort}")
            LOGGER.info("  Obfuscation type: ${encryptedSettingsPreference.obfuscationType.name}")

            // Log the full V2Ray JSON configuration for debugging
            val v2rayJsonConfig = config.jsonString()
            android.util.Log.d("HACKER", "V2Ray Full JSON Configuration:\n$v2rayJsonConfig")

            controller.startLoop(v2rayJsonConfig)
            isRunning = true

            LOGGER.info("V2Ray started successfully - traffic will be routed through local proxy")
            return true

        } catch (e: Exception) {
            LOGGER.error("Failed to start V2Ray: ${e.message}", e)
            cleanup()
            return false
        }
    }

    /**
     * Stops V2Ray proxy service.
     */
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

    /**
     * Gets the local proxy endpoint for WireGuard to connect to.
     *
     * @return local proxy endpoint in format "host:port"
     */
    fun getLocalProxyEndpoint(): String {
        return "$V2RAY_LOCAL_HOST:$currentLocalPort"
    }

    /**
     * Finds a free port for V2Ray local proxy using libV2ray's port allocation.
     *
     * @return available port number or base port as fallback
     */
    private fun findFreePort(): Int {
        return try {
            // Use libV2ray's GetFreePorts function to get a free port
            val freePorts = LibV2ray.getFreePorts(1)
            if (freePorts.isNotEmpty()) {
                val port = freePorts[0].toInt()
                LOGGER.info("libV2ray allocated free port: $port")
                port
            } else {
                LOGGER.warn("libV2ray returned no free ports, using base port $V2RAY_LOCAL_PORT_BASE")
                V2RAY_LOCAL_PORT_BASE
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to get free port from libV2ray: ${e.message}, using base port $V2RAY_LOCAL_PORT_BASE")
            V2RAY_LOCAL_PORT_BASE
        }
    }

    /**
     * Cleans up V2Ray state variables.
     */
    private fun cleanup() {
        isRunning = false
        currentLocalPort = 0
    }

    // CoreCallbackHandler implementation
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