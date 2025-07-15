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
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Host
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.rest.data.model.ServerType
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

/**
 * WireGuard configuration manager with V2Ray obfuscation support.
 * 
 * This class handles:
 * - WireGuard tunnel configuration generation
 * - V2Ray proxy integration for obfuscated connections
 * - Single-hop and multi-hop connection setup
 * - Server configuration validation and error handling
 * 
 * V2Ray Data Flow:
 * - Single-hop: Client → V2Ray Local Proxy → Entry Server V2Ray → Entry Server WireGuard → Internet
 * - Multi-hop: Client → V2Ray Local Proxy → Entry Server V2Ray → Exit Server WireGuard → Internet
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
    
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigManager::class.java)
        private const val WIREGUARD_TUNNEL_NAME = "IVPN"
        private const val DEFAULT_DNS = "172.16.0.1"
        
        // V2Ray standard ports (matching desktop implementation)
        private const val V2RAY_TCP_PORT = 80    // HTTP/VMess/TCP
        private const val V2RAY_QUIC_PORT = 443  // HTTPS/VMess/QUIC
    }
    
    var tunnel: Tunnel? = null
    var listener: Tunnel.OnStateChangedListener? = null
        set(value) {
            tunnel?.listener = value
            field = value
        }

    /**
     * Initialize the configuration manager.
     */
    fun init() {
        LOGGER.info("ConfigManager initialized")
    }

    /**
     * Starts WireGuard connection with optional V2Ray obfuscation.
     * 
     * Process:
     * 1. Updates V2Ray settings with current server information
     * 2. Starts V2Ray proxy if obfuscation is enabled
     * 3. Generates WireGuard configuration (routing through V2Ray if enabled)
     * 4. Applies configuration and starts WireGuard tunnel
     */
    fun startWireGuard() {
        LOGGER.info("Starting WireGuard connection...")
        
        // Update V2Ray settings with current server information
        updateV2raySettings()

        // Start V2Ray proxy if obfuscation is enabled
        if (v2rayController.isV2RayEnabled()) {
            val v2rayStarted = v2rayController.startIfEnabled()
            if (!v2rayStarted) {
                LOGGER.error("Failed to start V2Ray proxy service, aborting connection")
                return
            }
            LOGGER.info("V2Ray proxy started, WireGuard will use endpoint: ${v2rayController.getLocalProxyEndpoint()}")
        } else {
            LOGGER.info("V2Ray obfuscation disabled, using direct WireGuard connection")
        }

        // Generate WireGuard configuration (will route through V2Ray proxy if enabled)
        val config = generateConfig()
        if (config == null) {
            LOGGER.error("Failed to generate WireGuard configuration")
            v2rayController.stop()
            return
        }
        
        applyConfigToTunnel(config)

        // Start WireGuard tunnel
        GlobalScope.launch {
            try {
                tunnel?.setState(Tunnel.State.UP)
                LOGGER.info("WireGuard tunnel started successfully")
            } catch (e: Exception) {
                LOGGER.error("Failed to start WireGuard tunnel: ${e.message}", e)
                v2rayController.stop()
            }
        }
    }

    /**
     * Stops WireGuard connection and V2Ray proxy service.
     */
    fun stopWireGuard() {
        LOGGER.info("Stopping WireGuard connection...")
        
        // Stop WireGuard tunnel
        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.DOWN)
        }

        // Stop V2Ray proxy service
        v2rayController.stop()
        
        LOGGER.info("WireGuard connection stopped")
    }

    private fun applyConfigToTunnel(config: Config?) {
        // Log the full WireGuard configuration for debugging
        config?.let { wgConfig ->
            val configString = wgConfig.format()
            android.util.Log.d("HACKER", "WireGuard Full Configuration:\n$configString")
        }

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
            Log.d("HACKER", "Exit ip ${exitServer?.ipAddress}")
            generateConfigForMultiHop(server, exitServer)
        } else {
            val port = settings.wireGuardPort
            generateConfig(server, port)
        }
    }

    /**
     * Updates V2Ray settings with current server configuration for obfuscated connections.
     * 
     * V2Ray Configuration Logic:
     * - Outbound: Always connects to entry server V2Ray endpoint
     * - Inbound: Entry server (single-hop) or exit server (multi-hop) WireGuard endpoint
     * - Ports: Uses standard V2Ray ports (80 for TCP, 443 for QUIC)
     * 
     * Data Flow:
     * - Single-hop: Local V2Ray → Entry V2Ray → Entry WireGuard
     * - Multi-hop: Local V2Ray → Entry V2Ray → Exit WireGuard
     */
    private fun updateV2raySettings() {
        val obfuscationType = encryptedSettingsPreference.obfuscationType
        if (obfuscationType == ObfuscationType.DISABLED) {
            LOGGER.debug("V2Ray obfuscation disabled, skipping settings update")
            return
        }

        // Validate base V2Ray configuration
        val currentSettings = serversPreference.getV2RaySettings()
        if (currentSettings == null) {
            LOGGER.error("V2Ray base configuration not found")
            return
        }
        
        if (currentSettings.id.isEmpty()) {
            LOGGER.error("V2Ray user ID is empty, authentication will fail")
            return
        }

        // Get entry server configuration (required for all V2Ray connections)
        val entryServer = serversRepository.getCurrentServer(ServerType.ENTRY)
        if (entryServer?.hosts.isNullOrEmpty()) {
            LOGGER.error("Entry server not available, cannot configure V2Ray")
            return
        }

        val entryHost = entryServer.hosts[0]
        
        // Validate entry host has V2Ray configuration
        if (entryHost.v2ray.isNullOrEmpty()) {
            LOGGER.error("Entry host missing V2Ray configuration")
            return
        }

        // Configure V2Ray settings with entry server defaults
        var v2rayInboundIp = entryHost.host ?: ""
        var v2rayInboundPort = currentSettings.singleHopInboundPort
        val v2rayOutboundIp = entryHost.v2ray ?: ""
        
        // Use standard V2Ray ports based on obfuscation type (matching desktop implementation)
        val v2rayOutboundPort = when (obfuscationType) {
            ObfuscationType.V2RAY_TCP -> V2RAY_TCP_PORT   // HTTP/VMess/TCP
            ObfuscationType.V2RAY_QUIC -> V2RAY_QUIC_PORT // HTTPS/VMess/QUIC  
            else -> settings.wireGuardPort.portNumber
        }
        
        val v2rayDnsName = entryHost.dnsName ?: entryHost.hostname ?: ""
        
        // Validate critical fields
        if (v2rayInboundIp.isEmpty() || v2rayOutboundIp.isEmpty()) {
            LOGGER.error("Critical V2Ray IPs are empty - inbound: '$v2rayInboundIp', outbound: '$v2rayOutboundIp'")
            return
        }

        // Multi-hop override - changes inbound settings to exit server
        if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
            if (exitServer?.hosts?.isNotEmpty() == true) {
                val exitHost = exitServer.hosts[0]
                // For multi-hop: inbound connects to exit server, port same as outbound (like desktop)
                v2rayInboundIp = exitHost.host ?: ""
                v2rayInboundPort = v2rayOutboundPort  // Use same port as outbound (80 for TCP, 443 for QUIC)

                LOGGER.info("Multi-hop V2Ray override: inbound=${exitHost.host}:${v2rayOutboundPort}")
            } else {
                LOGGER.error("Multi-hop enabled but no exit server available")
                return
            }
        }

        // Create updated V2Ray settings
        val v2raySettings = V2RaySettings(
            id = currentSettings.id,
            outboundIp = v2rayOutboundIp,      // Always entry server
            outboundPort = v2rayOutboundPort,   // Standard V2Ray port
            inboundIp = v2rayInboundIp,        // Entry server (single-hop) or Exit server (multi-hop)
            inboundPort = v2rayInboundPort,     // Single-hop port or Exit multihop port
            dnsName = v2rayDnsName,            // Always entry server
            wireguard = currentSettings.wireguard
        )

        serversPreference.putV2RaySettings(v2raySettings)

        // Log complete V2Ray settings for debugging
        android.util.Log.d("HACKER", "V2Ray Settings Object:\n" +
            "ID: ${v2raySettings.id}\n" +
            "Outbound IP: ${v2raySettings.outboundIp} (Entry Server V2Ray)\n" +
            "Outbound Port: ${v2raySettings.outboundPort} (Standard V2Ray port)\n" +
            "Inbound IP: ${v2raySettings.inboundIp} (${if (multiHopController.isReadyToUse()) "Exit Server" else "Entry Server"} WireGuard)\n" +
            "Inbound Port: ${v2raySettings.inboundPort} (${if (multiHopController.isReadyToUse()) "Same as outbound" else "Internal V2Ray port"})\n" +
            "DNS Name: ${v2raySettings.dnsName}\n" +
            "TLS Srv Name: ${v2raySettings.tlsSrvName}\n" +
            "Wireguard: ${v2raySettings.wireguard}\n" +
            "Mode: ${if (multiHopController.isReadyToUse()) "Multi-hop" else "Single-hop"}\n" +
            "Obfuscation: ${obfuscationType.name}")

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

    /**
     * Validates V2Ray settings to ensure all required fields are properly configured.
     * 
     * @param settings The V2Ray settings to validate
     * @return Error message if validation fails, null if valid
     */
    private fun validateV2RaySettings(settings: V2RaySettings): String? {
        return when {
            settings.id.isEmpty() -> "V2Ray user ID is empty"
            settings.outboundIp.isEmpty() -> "V2Ray outbound IP is empty"
            settings.outboundPort <= 0 -> "V2Ray outbound port is invalid: ${settings.outboundPort}"
            settings.inboundIp.isEmpty() -> "V2Ray inbound IP is empty"
            settings.inboundPort <= 0 -> "V2Ray inbound port is invalid: ${settings.inboundPort}"
            else -> null
        }
    }

    /**
     * Generates WireGuard configuration for single-hop connections.
     * 
     * @param server The VPN server to connect to
     * @param port The port configuration to use
     * @return WireGuard configuration or null if generation fails
     */
    private fun generateConfig(server: Server?, port: Port): Config? {
        LOGGER.info("Generating WireGuard configuration for single-hop connection")
        
        if (server == null || server.hosts.isNullOrEmpty()) {
            LOGGER.error("Server or hosts are null/empty")
            return null
        }

        val privateKey = settings.wireGuardPrivateKey
        if (privateKey.isNullOrEmpty()) {
            LOGGER.error("WireGuard private key is null or empty")
            return null
        }

        val host = server.hosts[0]
        LOGGER.info("Using server host: ${host.hostname} (${host.host})")

        return createWireGuardConfig(
            peerHost = host,
            portNumber = port.portNumber,
            privateKey = privateKey,
            hosts = listOf(host)
        )
    }

    /**
     * Generates WireGuard configuration for multi-hop connections.
     * 
     * @param entryServer The entry VPN server
     * @param exitServer The exit VPN server
     * @return WireGuard configuration or null if generation fails
     */
    private fun generateConfigForMultiHop(entryServer: Server?, exitServer: Server?): Config? {
        LOGGER.info("Generating WireGuard configuration for multi-hop connection")
        
        if (entryServer == null || entryServer.hosts.isNullOrEmpty()) {
            LOGGER.error("Entry server or hosts are null/empty")
            return null
        }
        
        if (exitServer == null || exitServer.hosts.isNullOrEmpty()) {
            LOGGER.error("Exit server or hosts are null/empty")
            return null
        }

        val privateKey = settings.wireGuardPrivateKey
        if (privateKey.isNullOrEmpty()) {
            LOGGER.error("WireGuard private key is null or empty")
            return null
        }

        val entryHost = entryServer.hosts[0]
        val exitHost = exitServer.hosts[0]
        
        LOGGER.info("Multi-hop: Entry server: ${entryHost.hostname} (${entryHost.host})")
        LOGGER.info("Multi-hop: Exit server: ${exitHost.hostname} (${exitHost.host})")

        // For multi-hop, we use the exit server's public key and multihop port
        val portNumber = exitHost.multihopPort
        return createWireGuardConfig(
            peerHost = exitHost,            // Exit server's public key for peer configuration
            entryHost = entryHost,          // Entry server's host for direct connection endpoint
            portNumber = portNumber,
            privateKey = privateKey,
            hosts = listOf(entryHost, exitHost),
            isMultiHop = true
        )
    }

    /**
     * Creates a WireGuard configuration with the specified parameters.
     * 
     * @param peerHost The host to use for peer configuration (exit server for multi-hop)
     * @param entryHost The host to use for direct connection endpoint (entry server for multi-hop)
     * @param portNumber The port number to use for direct connections
     * @param privateKey The WireGuard private key
     * @param hosts The list of hosts for address configuration
     * @param isMultiHop Whether this is a multi-hop connection
     * @return WireGuard configuration
     */
    private fun createWireGuardConfig(
        peerHost: Host,
        entryHost: Host? = null,
        portNumber: Int,
        privateKey: String,
        hosts: List<Host>,
        isMultiHop: Boolean = false
    ): Config {
        val config = Config()
        
        // Configure interface
        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }
        
        setAddress(config, hosts)
        
        val dnsString = getDNS(hosts[0])
        LOGGER.info("DNS configuration: $dnsString")
        config.getInterface().setDnsString(dnsString)

        // Configure endpoint based on V2Ray obfuscation status
        val endpoint = if (v2rayController.isV2RayEnabled()) {
            // Route through local V2Ray proxy
            val proxyEndpoint = v2rayController.getLocalProxyEndpoint()
            LOGGER.info("${if (isMultiHop) "Multi-hop" else "Single-hop"} using V2Ray proxy endpoint: $proxyEndpoint")
            android.util.Log.d("HACKER", "${if (isMultiHop) "Multi-hop" else "Single-hop"} WireGuard will connect to V2Ray proxy at: $proxyEndpoint")
            proxyEndpoint
        } else {
            // Direct connection to WireGuard server
            val endpointHost = if (isMultiHop && entryHost != null) {
                // For multi-hop direct connection: connect to entry server, but use exit server's public key
                entryHost.host
            } else {
                // For single-hop direct connection: connect to the same server whose public key we use
                peerHost.host
            }
            val directEndpoint = "${endpointHost}:$portNumber"
            LOGGER.info("${if (isMultiHop) "Multi-hop" else "Single-hop"} using direct endpoint: $directEndpoint")
            android.util.Log.d("HACKER", "${if (isMultiHop) "Multi-hop" else "Single-hop"} WireGuard will connect directly to: $directEndpoint")
            if (isMultiHop) {
                android.util.Log.d("HACKER", "Multi-hop: Connecting to entry server (${entryHost?.host}) but using exit server's public key (${peerHost.publicKey})")
            }
            directEndpoint
        }

        // Configure peer
        val peer = Peer().also {
            // Use same AllowedIPs for both single-hop and multi-hop to disable WireGuard's internal firewall
            // Android VPN service handles routing, so we need to disable WireGuard's firewall
            it.setAllowedIPsString("128.0.0.0/1, 0.0.0.0/1")
            it.setEndpointString(endpoint)
            it.publicKey = peerHost.publicKey
            LOGGER.info("${if (isMultiHop) "Multi-hop" else "Single-hop"} peer configuration - endpoint: $endpoint, publicKey: ${peerHost.publicKey}")
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
            LOGGER.info("Using pre-shared key")
        }

        config.peers = listOf(peer)

        LOGGER.info("${if (isMultiHop) "Multi-hop" else "Single-hop"} WireGuard configuration generated successfully")
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

    /**
     * Handles tunnel state changes by updating the tunnel state.
     * 
     * @param state The new tunnel state
     */
    fun onTunnelStateChanged(state: Tunnel.State) {
        GlobalScope.launch {
            tunnel?.setState(state)
        }
    }
}