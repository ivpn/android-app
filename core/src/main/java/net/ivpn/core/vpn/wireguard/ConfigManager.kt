package net.ivpn.core.vpn.wireguard

import android.util.Log
import com.wireguard.android.config.Config
import com.wireguard.android.config.Peer
import com.wireguard.android.model.Tunnel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.multihop.MultiHopController
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import net.ivpn.core.common.prefs.ServersPreference
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Host
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.vpn.controller.V2rayController
import net.ivpn.core.vpn.model.ObfuscationType
import net.ivpn.core.vpn.model.V2RayConfig
import net.ivpn.core.vpn.model.V2RaySettings
import org.slf4j.LoggerFactory
import javax.inject.Inject

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

@ApplicationScope
class ConfigManager @Inject constructor(
    private val settings: Settings,
    private val serversRepository: ServersRepository,
    private val multiHopController: MultiHopController,
    private val encryptedSettingsPreference: EncryptedSettingsPreference,
    private val serversPreference: ServersPreference,
    private val v2rayController: V2rayController
) {
    var tunnel: Tunnel? = null
    var listener: Tunnel.OnStateChangedListener? = null
        set(value) {
            tunnel?.listener = value
            field = value
        }

    fun init() {
        LOGGER.info("init")
    }


    fun startWireGuard() {
        LOGGER.info("Starting WireGuard connection...")
        
        // Step 1: Update V2Ray settings with current server info
        updateV2raySettings()

        // Step 2: Add V2Ray server IP to bypass routes (prevents circular routing)
        if (v2rayController.isV2RayEnabled()) {
            try {
                val v2raySettings = serversPreference.getV2RaySettings()
                if (v2raySettings != null) {
                    val serverIp = v2raySettings.outboundIp
                    LOGGER.info("Adding V2Ray server IP to bypass routes: $serverIp")
                    // This will be handled in the WireGuard configuration generation
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to get V2Ray settings for bypass routes: ${e.message}", e)
            }
        }

        // Step 3: Start V2Ray if enabled (before WireGuard)
        if (v2rayController.isV2RayEnabled()) {
            val v2rayStarted = v2rayController.startIfEnabled()
            if (!v2rayStarted) {
                LOGGER.error("Failed to start V2Ray, but V2Ray is enabled. Aborting connection.")
                return
            }
            LOGGER.info("V2Ray started successfully, WireGuard will use local proxy: ${v2rayController.getLocalProxyEndpoint()}")
        } else {
            LOGGER.info("V2Ray is disabled, using direct WireGuard connection")
        }

        // Step 4: Generate WireGuard config (will use V2Ray proxy if enabled)
        val config = generateConfig()
        if (config == null) {
            LOGGER.error("Failed to generate WireGuard configuration")
            v2rayController.stop()  // Clean up V2Ray if it was started
            return
        }
        
        applyConfigToTunnel(config)

        // Step 5: Start WireGuard tunnel
        GlobalScope.launch {
            try {
                tunnel?.setState(Tunnel.State.UP)
                LOGGER.info("WireGuard tunnel started successfully")
            } catch (e: Exception) {
                LOGGER.error("Failed to start WireGuard tunnel: ${e.message}", e)
                v2rayController.stop()  // Clean up V2Ray if tunnel fails
            }
        }
    }

    fun stopWireGuard() {
        // Step 1: Stop WireGuard tunnel
        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.DOWN)
        }

        // Step 2: Stop V2Ray after WireGuard
        v2rayController.stop()
    }


    private fun applyConfigToTunnel(config: Config?) {
        tunnel?.let {
            it.config = config
        } ?: kotlin.run {
            tunnel = Tunnel(WIREGUARD_TUNNEL_NAME, config, Tunnel.State.DOWN)
            tunnel?.listener = listener
        }
    }

    private fun generateConfig(): Config? {
        val server = serversRepository.getCurrentServer(ServerType.ENTRY)
        Log.d("HACKER", "Entry ip ${server?.ipAddress}")
        return if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
            Log.d("HACKER", "Exit ip ${server?.ipAddress}")
            generateConfigForMultiHop(server, exitServer)
        } else {
            val port = settings.wireGuardPort
            generateConfig(server, port)
        }
    }

    private fun updateV2raySettings() {
        // Check if V2Ray is enabled
        val obfuscationType = encryptedSettingsPreference.obfuscationType
        if (obfuscationType == ObfuscationType.DISABLED) {
            LOGGER.debug("V2Ray is disabled, skipping settings update")
            return
        }

        // Get current V2Ray settings (base configuration)
        val currentSettings = serversPreference.getV2RaySettings()
        if (currentSettings == null) {
            LOGGER.error("No V2Ray base configuration found - this is critical!")
            return
        }
        
        if (currentSettings.id.isEmpty()) {
            LOGGER.error("V2Ray user ID is empty - this will cause authentication failures")
            return
        }

        // ALWAYS get entry server (like iOS getHost())
        val entryServer = serversRepository.getCurrentServer(ServerType.ENTRY)
        if (entryServer?.hosts.isNullOrEmpty()) {
            LOGGER.error("No entry server or hosts available - cannot configure V2Ray")
            return
        }

        val entryHost = entryServer.hosts[0] // Use first host
        
        // Validate entry host has required V2Ray fields
        if (entryHost.v2ray.isNullOrEmpty()) {
            LOGGER.error("Entry host does not have V2Ray configuration - V2Ray will not work")
            return
        }

        // Initialize V2Ray settings with entry server (like iOS)
        var v2rayInboundIp = entryHost.host ?: ""
        var v2rayInboundPort = currentSettings.singleHopInboundPort
        val v2rayOutboundIp = entryHost.v2ray ?: ""  // ALWAYS entry server
        val v2rayOutboundPort = settings.wireGuardPort.portNumber
        val v2rayDnsName = entryHost.dnsName ?: entryHost.hostname ?: ""  // ALWAYS entry server
        
        // Validate critical fields
        if (v2rayInboundIp.isEmpty() || v2rayOutboundIp.isEmpty()) {
            LOGGER.error("Critical V2Ray IPs are empty - inbound: '$v2rayInboundIp', outbound: '$v2rayOutboundIp'")
            return
        }

        // Multi-hop override (like iOS) - ONLY changes inbound settings
        if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
            if (exitServer?.hosts?.isNotEmpty() == true) {
                val exitHost = exitServer.hosts[0]
                // ONLY override inbound settings for multi-hop
                v2rayInboundIp = exitHost.host ?: ""
                v2rayInboundPort = exitHost.multihopPort

                LOGGER.info("Multi-hop V2Ray override: inbound=${exitHost.host}:${exitHost.multihopPort}")
            } else {
                LOGGER.error("Multi-hop enabled but no exit server available")
                return
            }
        }

        // Create updated V2Ray settings
        val v2raySettings = V2RaySettings(
            id = currentSettings.id,
            outboundIp = v2rayOutboundIp,      // Always entry server
            outboundPort = v2rayOutboundPort,   // WireGuard port
            inboundIp = v2rayInboundIp,        // Entry server (single-hop) or Exit server (multi-hop)
            inboundPort = v2rayInboundPort,     // Single-hop port or Exit multihop port
            dnsName = v2rayDnsName,            // Always entry server
            wireguard = currentSettings.wireguard
        )

        serversPreference.putV2RaySettings(v2raySettings)

        LOGGER.info("Updated V2Ray settings:")
        LOGGER.info("  Outbound (always entry): ${v2rayOutboundIp}:${v2rayOutboundPort}")
        LOGGER.info("  Inbound: ${v2rayInboundIp}:${v2rayInboundPort}")
        LOGGER.info("  DNS (always entry): ${v2rayDnsName}")
        LOGGER.info("  Mode: ${if (multiHopController.isReadyToUse()) "Multi-hop" else "Single-hop"}")
        LOGGER.info("  User ID: ${currentSettings.id.take(8)}...")  // Only log first 8 chars for security

        // Validate the final configuration
        val finalValidationError = validateV2RaySettings(v2raySettings)
        if (finalValidationError != null) {
            LOGGER.error("V2Ray settings validation failed: $finalValidationError")
            return
        }

        // Generate the actual V2Ray config to verify it works
        val testConfig = when (obfuscationType) {
            ObfuscationType.V2RAY_TCP -> V2RayConfig.createTcp(
                context = IVPNApplication.application,
                outboundIp = v2raySettings.outboundIp,
                outboundPort = v2raySettings.outboundPort,
                inboundIp = v2raySettings.inboundIp,
                inboundPort = v2raySettings.inboundPort,
                outboundUserId = v2raySettings.id
            )

            ObfuscationType.V2RAY_QUIC -> V2RayConfig.createQUIC(
                context = IVPNApplication.application,
                outboundIp = v2raySettings.outboundIp,
                outboundPort = v2raySettings.outboundPort,
                inboundIp = v2raySettings.inboundIp,
                inboundPort = v2raySettings.inboundPort,
                outboundUserId = v2raySettings.id,
                tlsSrvName = v2raySettings.tlsSrvName
            )

            ObfuscationType.DISABLED -> null
        }

        val configValidationError = testConfig?.isValid()
        if (configValidationError != null) {
            LOGGER.error("Generated V2Ray configuration is invalid: $configValidationError")
            return
        }

        LOGGER.info("V2Ray settings updated and validated successfully")
    }

    private fun validateV2RaySettings(settings: V2RaySettings): String? {
        if (settings.id.isEmpty()) return "User ID is empty"
        if (settings.outboundIp.isEmpty()) return "Outbound IP is empty"
        if (settings.outboundPort <= 0) return "Outbound port is invalid"
        if (settings.inboundIp.isEmpty()) return "Inbound IP is empty"
        if (settings.inboundPort <= 0) return "Inbound port is invalid"
        return null
    }

    private fun generateConfig(server: Server?, port: Port): Config? {
        LOGGER.info("Generating WireGuard config for single-hop connection")
        
        if (server == null || server.hosts == null || server.hosts.isEmpty()) {
            LOGGER.error("Server or hosts are null/empty")
            return null
        }

        val config = Config()
        val privateKey = settings.wireGuardPrivateKey
        if (privateKey.isNullOrEmpty()) {
            LOGGER.error("WireGuard private key is null or empty")
            return null
        }

        val host = server.hosts[0] // Use first host as determined
        LOGGER.info("Using server host: ${host.hostname} (${host.host})")

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        setAddress(config, listOf(host))

        val dnsString = getDNS(host)
        LOGGER.info("DNS configuration: $dnsString")
        config.getInterface().setDnsString(dnsString)

        // CRITICAL: Modify endpoint based on V2Ray status
        val endpoint = if (v2rayController.isV2RayEnabled()) {
            // Use local V2Ray proxy instead of direct server connection
            val proxyEndpoint = v2rayController.getLocalProxyEndpoint()
            LOGGER.info("V2Ray enabled - using local proxy endpoint: $proxyEndpoint")
            proxyEndpoint
        } else {
            // Direct connection to WireGuard server
            val directEndpoint = "${host.host}:${port.portNumber}"
            LOGGER.info("V2Ray disabled - using direct endpoint: $directEndpoint")
            directEndpoint
        }

        val peer = Peer().also {
            it.setAllowedIPsString("128.0.0.0/1, 0.0.0.0/1")
            it.setEndpointString(endpoint)  // This is the key change!
            it.publicKey = host.publicKey
            LOGGER.info("Peer configuration - endpoint: $endpoint, publicKey: ${host.publicKey}")
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
            LOGGER.info("Using pre-shared key")
        }

        config.peers = listOf(peer)

        LOGGER.info("WireGuard config generated successfully")
        return config
    }
    private fun generateConfigForMultiHop(entryServer: Server?, exitServer: Server?): Config? {
        LOGGER.info("Generating WireGuard config for multi-hop connection")
        
        if (entryServer == null || entryServer.hosts == null || entryServer.hosts.isEmpty()) {
            LOGGER.error("Entry server or hosts are null/empty")
            return null
        }
        
        if (exitServer == null || exitServer.hosts == null || exitServer.hosts.isEmpty()) {
            LOGGER.error("Exit server or hosts are null/empty")
            return null
        }

        val config = Config()
        val privateKey = settings.wireGuardPrivateKey
        if (privateKey.isNullOrEmpty()) {
            LOGGER.error("WireGuard private key is null or empty")
            return null
        }

        val entryHost = entryServer.hosts[0]
        val exitHost = exitServer.hosts[0]
        
        LOGGER.info("Multi-hop: Entry server: ${entryHost.hostname} (${entryHost.host})")
        LOGGER.info("Multi-hop: Exit server: ${exitHost.hostname} (${exitHost.host})")

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        setAddress(config, listOf(entryHost, exitHost))

        val dnsString = getDNS(entryHost)
        LOGGER.info("DNS configuration: $dnsString")
        config.getInterface().setDnsString(dnsString)

        // CRITICAL: Modify endpoint for multi-hop with V2Ray
        val endpoint = if (v2rayController.isV2RayEnabled()) {
            // Use local V2Ray proxy
            val proxyEndpoint = v2rayController.getLocalProxyEndpoint()
            LOGGER.info("V2Ray enabled - multi-hop using local proxy endpoint: $proxyEndpoint")
            proxyEndpoint
        } else {
            // Direct multi-hop connection
            val directEndpoint = "${entryHost.host}:${exitHost.multihopPort}"
            LOGGER.info("V2Ray disabled - multi-hop using direct endpoint: $directEndpoint")
            directEndpoint
        }

        val peer = Peer().also {
            it.setAllowedIPsString("0.0.0.0/0, ::/0")
            it.setEndpointString(endpoint)  // This is the key change!
            it.publicKey = exitHost.publicKey  // Use exit server's public key for multi-hop
            LOGGER.info("Multi-hop peer configuration - endpoint: $endpoint, publicKey: ${exitHost.publicKey}")
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
            LOGGER.info("Using pre-shared key")
        }

        config.peers = listOf(peer)

        LOGGER.info("Multi-hop WireGuard config generated successfully")
        return config
    }
    private fun setAddress(config: Config, hosts: List<Host>) {
        val ipAddress = settings.wireGuardIpAddress
        val ipv6Setting = settings.ipv6Setting

        if (hosts.isEmpty()) {
            return
        }

        if (!ipv6Setting) {
            config.getInterface().setAddressString(ipAddress)
            return
        }

        for (host in hosts) {
            if (host.ipv6 == null || host.ipv6.local_ip.isNullOrEmpty()) {
                config.getInterface().setAddressString(ipAddress)
                return
            }
        }

        val entryHost = hosts[0]
        val localIPv6AddressForEntry = entryHost.ipv6.local_ip

        config.getInterface()
            .setAddressString("$ipAddress/32,${localIPv6AddressForEntry!!.split('/')[0]}$ipAddress/128")
    }

    private fun getDNS(host: Host): String {
        val dns = settings.dns
        if (dns != null) {
            return dns
        }
        return if (host.localIp == null) {
            DEFAULT_DNS
        } else host.localIp.split("/".toRegex()).toTypedArray()[0]
    }

    fun onTunnelStateChanged(state: Tunnel.State) {
        GlobalScope.launch {
            tunnel?.setState(state)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigManager::class.java)
        private const val WIREGUARD_TUNNEL_NAME = "IVPN"
        private const val DEFAULT_DNS = "172.16.0.1"
    }
}