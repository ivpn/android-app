package net.ivpn.core.vpn.controller

import com.wireguard.android.util.SharedLibraryLoader
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.common.prefs.ServersPreference
import net.ivpn.core.vpn.model.ObfuscationType
import net.ivpn.core.vpn.model.V2RayConfig
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
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

@ApplicationScope
class V2rayController @Inject constructor(
    private val encryptedSettingsPreference: EncryptedSettingsPreference,
    private val serversPreference: ServersPreference
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(V2rayController::class.java)
        private const val V2RAY_LOCAL_HOST = "127.0.0.1"
        private const val V2RAY_LOCAL_PORT_BASE = 16661

    }

    // Native c-go bindings
    private external fun wgV2rayStart(jsonConfig: String): Int
    private external fun wgV2rayStop(handle: Int): Int
    private external fun wgV2rayIsRunning(): Boolean
    private external fun wgGetFreePort(): Int

    @Volatile
    private var isRunning = false

    @Volatile
    private var currentLocalPort = 0

    @Volatile
    private var currentHandle = -1

    fun makeConfig(): V2RayConfig? {
        val settings = serversPreference.getV2RaySettings() ?: return null
        val obfuscationType = encryptedSettingsPreference.obfuscationType

        if (settings.inboundIp.isEmpty()) {
            LOGGER.error("V2Ray inbound IP is empty")
            return null
        }
        if (settings.outboundIp.isEmpty()) {
            LOGGER.error("V2Ray outbound IP is empty")
            return null
        }
        if (settings.inboundPort <= 0) {
            LOGGER.error("V2Ray inbound port is invalid: ${settings.inboundPort}")
            return null
        }
        if (settings.outboundPort <= 0) {
            LOGGER.error("V2Ray outbound port is invalid: ${settings.outboundPort}")
            return null
        }
        if (settings.id.isEmpty()) {
            LOGGER.error("V2Ray user ID is empty")
            return null
        }

        LOGGER.info("Creating V2Ray config with settings: inbound=${settings.inboundIp}:${settings.inboundPort}, outbound=${settings.outboundIp}:${settings.outboundPort}")

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
        return obfuscationType != ObfuscationType.DISABLED
    }

    fun startIfEnabled(): Boolean {
        if (!isV2RayEnabled()) {
            LOGGER.debug("V2Ray is disabled, skipping start")
            return false
        }

        if (wgV2rayIsRunning() && currentHandle > 0) {
            LOGGER.info("V2Ray is running; restarting to apply latest settings (handle=$currentHandle)")
            try {
                wgV2rayStop(currentHandle)
            } catch (e: Exception) {
                LOGGER.warn("Error while stopping V2Ray before restart: ${e.message}", e)
            }
            isRunning = false
            currentHandle = -1
            currentLocalPort = 0
        }

        return start()
    }

    fun start(): Boolean {
        try {
            if (wgV2rayIsRunning() && currentHandle > 0) {
                LOGGER.info("Stopping previous V2Ray instance before start (handle=$currentHandle)")
                try {
                    wgV2rayStop(currentHandle)
                } catch (e: Exception) {
                    LOGGER.warn("Error while stopping previous V2Ray instance: ${e.message}", e)
                }
                isRunning = false
                currentHandle = -1
                currentLocalPort = 0
            }

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
            LOGGER.info("  Configuration JSON: ${config.jsonString()}")

            currentHandle = wgV2rayStart(config.jsonString())
            if (currentHandle <= 0) {
                LOGGER.error("Failed to start V2Ray (invalid handle returned) $currentHandle")
                return false
            }

            isRunning = true
            LOGGER.info("V2Ray started successfully with handle=$currentHandle")
            return true

        } catch (e: Exception) {
            LOGGER.error("Failed to start V2Ray: ${e.message}", e)
            cleanup()
            return false
        }
    }

    fun stop() {
        try {
            if (wgV2rayIsRunning() && currentHandle > 0) {
                LOGGER.info("Stopping V2Ray proxy service with handle=$currentHandle")
                wgV2rayStop(currentHandle)
            }
            isRunning = false
            currentHandle = -1
            LOGGER.info("V2Ray stopped successfully")
        } catch (e: Exception) {
            LOGGER.error("Error stopping V2Ray: ${e.message}", e)
        }
    }

    fun getLocalProxyEndpoint(): String {
        return "$V2RAY_LOCAL_HOST:$currentLocalPort"
    }

    private fun findFreePort(): Int {
        return try {
            val port = wgGetFreePort()
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
        currentHandle = -1
        currentLocalPort = 0
    }
}