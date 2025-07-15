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
@ApplicationScope
class ConfigManager @Inject constructor(
    private val settings: Settings,
    private val serversRepository: ServersRepository,
    private val multiHopController: MultiHopController,
    private val encryptedSettingsPreference: EncryptedSettingsPreference,
    private val serversPreference: ServersPreference,
    private val v2rayController: V2rayController
) {
    /**
     * V2Ray Data Flow:
     * - Single-hop: Client → V2Ray Local Proxy → Entry Server V2Ray → Entry Server WireGuard → Internet
     * - Multi-hop: Client → V2Ray Local Proxy → Entry Server V2Ray → Exit Server WireGuard → Internet
     */
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigManager::class.java)
        private const val WIREGUARD_TUNNEL_NAME = "IVPN"
        private const val DEFAULT_DNS = "172.16.0.1"

        // HTTP/VMess/TCP (According to desktop-app)
        private const val V2RAY_TCP_PORT = 80

        // HTTPS/VMess/QUIC (According to desktop-app)
        private const val V2RAY_QUIC_PORT = 443
    }

    var tunnel: Tunnel? = null
    var listener: Tunnel.OnStateChangedListener? = null
        set(value) {
            tunnel?.listener = value
            field = value
        }


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
        updateV2raySettings()
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

        val config = generateConfig()
        if (config == null) {
            LOGGER.error("Failed to generate WireGuard configuration")
            v2rayController.stop()
            return
        }

        applyConfigToTunnel(config)

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


    fun stopWireGuard() {
        LOGGER.info("Stopping WireGuard connection...")

        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.DOWN)
        }

        v2rayController.stop()

        LOGGER.info("WireGuard connection stopped")
    }

    private fun applyConfigToTunnel(config: Config?) {
        config?.let { wgConfig ->
            val configString = wgConfig.format()
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
        return if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
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

        // validate base V2Ray configuration
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

        // Use standard V2Ray ports based on obfuscation type (matching desktop-pp implementation)
        val v2rayOutboundPort = when (obfuscationType) {
            ObfuscationType.V2RAY_TCP -> V2RAY_TCP_PORT
            ObfuscationType.V2RAY_QUIC -> V2RAY_QUIC_PORT
            else -> settings.wireGuardPort.portNumber
        }

        val v2rayDnsName = entryHost.dnsName ?: entryHost.hostname ?: ""

        if (v2rayInboundIp.isEmpty() || v2rayOutboundIp.isEmpty()) {
            LOGGER.error("Critical V2Ray IPs are empty - inbound: '$v2rayInboundIp', outbound: '$v2rayOutboundIp'")
            return
        }

        if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
            if (exitServer?.hosts?.isNotEmpty() == true) {
                val exitHost = exitServer.hosts[0]
                v2rayInboundIp = exitHost.host ?: ""
                v2rayInboundPort = v2rayOutboundPort
                LOGGER.info("Multi-hop V2Ray override: inbound=${exitHost.host}:${v2rayOutboundPort}")
            } else {
                LOGGER.error("Multi-hop enabled but no exit server available")
                return
            }
        }

        val v2raySettings = V2RaySettings(
            id = currentSettings.id,
            outboundIp = v2rayOutboundIp,
            outboundPort = v2rayOutboundPort,
            inboundIp = v2rayInboundIp,
            inboundPort = v2rayInboundPort,
            dnsName = v2rayDnsName,
            wireguard = currentSettings.wireguard
        )
        serversPreference.putV2RaySettings(v2raySettings)
        val finalValidationError = validateV2RaySettings(v2raySettings)
        if (finalValidationError != null) {
            LOGGER.error("V2Ray settings validation failed: $finalValidationError")
            return
        }
    }

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

        // for multi-hop, we use the exit server's public key and multihop port
        val portNumber = exitHost.multihopPort
        return createWireGuardConfig(
            peerHost = exitHost,
            entryHost = entryHost,
            portNumber = portNumber,
            privateKey = privateKey,
            hosts = listOf(entryHost, exitHost),
            isMultiHop = true
        )
    }

    private fun createWireGuardConfig(
        peerHost: Host,
        entryHost: Host? = null,
        portNumber: Int,
        privateKey: String,
        hosts: List<Host>,
        isMultiHop: Boolean = false
    ): Config {
        val config = Config()

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        setAddress(config, hosts)

        val dnsString = getDNS(hosts[0])
        config.getInterface().setDnsString(dnsString)

        val endpoint = if (v2rayController.isV2RayEnabled()) {
            v2rayController.getLocalProxyEndpoint()
        } else {
            val endpointHost = if (isMultiHop && entryHost != null) {
                entryHost.host
            } else {
                peerHost.host
            }
            val directEndpoint = "${endpointHost}:$portNumber"
            LOGGER.info("${if (isMultiHop) "Multi-hop" else "Single-hop"} using direct endpoint: $directEndpoint")
            directEndpoint
        }

        val peer = Peer().also {
            // uses same AllowedIPs for both single-hop and multi-hop to disable wg's internal firewall
            // Android VPN service handles routing, so we need to disable WireGuard's firewall
            it.setAllowedIPsString("128.0.0.0/1, 0.0.0.0/1")
            it.setEndpointString(endpoint)
            it.publicKey = peerHost.publicKey
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
            LOGGER.info("Using pre-shared key")
        }

        config.peers = listOf(peer)

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
}