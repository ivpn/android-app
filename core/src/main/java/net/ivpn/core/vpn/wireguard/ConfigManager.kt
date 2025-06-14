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
        // Step 1: Update V2Ray settings with current server info
        updateV2raySettings()

        // Step 2: Start V2Ray if enabled (before WireGuard)
        val v2rayStarted = v2rayController.startIfEnabled()
        if (v2rayController.isV2RayEnabled() && !v2rayStarted) {
            LOGGER.error("Failed to start V2Ray, but V2Ray is enabled. Connection may fail.")
        }

        // Step 3: Generate WireGuard config (will use V2Ray proxy if enabled)
        applyConfigToTunnel(generateConfig())

        // Step 4: Start WireGuard tunnel
        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.UP)
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
        if (currentSettings == null || currentSettings.id.isEmpty()) {
            LOGGER.warn("No V2Ray base configuration found, skipping update")
            return
        }

        // ALWAYS get entry server (like iOS getHost())
        val entryServer = serversRepository.getCurrentServer(ServerType.ENTRY)
        if (entryServer?.hosts.isNullOrEmpty()) {
            LOGGER.warn("No entry server or hosts available")
            return
        }

        val entryHost = entryServer.hosts[0] // Use first host

        // Initialize V2Ray settings with entry server (like iOS)
        var v2rayInboundIp = entryHost.host ?: ""
        var v2rayInboundPort = currentSettings.singleHopInboundPort
        val v2rayOutboundIp = entryHost.v2ray ?: ""  // ALWAYS entry server
        val v2rayOutboundPort = settings.wireGuardPort.portNumber
        val v2rayDnsName = entryHost.dnsName ?: entryHost.hostname ?: ""  // ALWAYS entry server

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
                LOGGER.warn("Multi-hop enabled but no exit server available")
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


        val settings = serversPreference.getV2RaySettings()


        val config =  when (obfuscationType) {
            ObfuscationType.V2RAY_TCP -> V2RayConfig.createTcp(
                context = IVPNApplication.application,
                outboundIp = settings!!.outboundIp,
                outboundPort = settings.outboundPort,
                inboundIp = settings.inboundIp,
                inboundPort = settings.inboundPort,
                outboundUserId = settings.id
            )

            ObfuscationType.V2RAY_QUIC -> V2RayConfig.createQUIC(
                context = IVPNApplication.application,
                outboundIp = settings!!.outboundIp,
                outboundPort = settings.outboundPort,
                inboundIp = settings.inboundIp,
                inboundPort = settings.inboundPort,
                outboundUserId = settings.id,
                tlsSrvName = settings.tlsSrvName
            )

            ObfuscationType.DISABLED -> null
        }

        Log.d("HACKER", "updateV2raySettings: "+config!!.jsonString())
    }

    private fun generateConfig(server: Server?, port: Port): Config? {
        val config = Config()
        val privateKey = settings.wireGuardPrivateKey

        LOGGER.info("Generating config:")
        if (server == null || server.hosts == null) {
            return null
        }

        val host = server.hosts[0] // Use first host as determined

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        setAddress(config, listOf(host))

        val dnsString = getDNS(host)
        println("Config dns = $dnsString")
        config.getInterface().setDnsString(dnsString)

        // CRITICAL: Modify endpoint based on V2Ray status
        val endpoint = if (v2rayController.isV2RayEnabled()) {
            // Use local V2Ray proxy instead of direct server connection
            v2rayController.getLocalProxyEndpoint()
        } else {
            // Direct connection to WireGuard server
            "${host.host}:${port.portNumber}"
        }

        val peer = Peer().also {
            it.setAllowedIPsString("0.0.0.0/0, ::/0")
            it.setEndpointString(endpoint)  // This is the key change!
            it.publicKey = host.publicKey
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
        }

        config.peers = listOf(peer)

        LOGGER.info("WireGuard endpoint: $endpoint (V2Ray: ${v2rayController.isV2RayEnabled()})")

        return config
    }
    private fun generateConfigForMultiHop(entryServer: Server?, exitServer: Server?): Config? {
        val config = Config()
        val privateKey = settings.wireGuardPrivateKey

        LOGGER.info("Generating config for multihop:")
        if (entryServer == null || entryServer.hosts == null || exitServer == null || exitServer.hosts == null) {
            return null
        }

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        val entryHost = entryServer.hosts[0]
        val exitHost = exitServer.hosts[0]

        setAddress(config, listOf(entryHost, exitHost))

        val dnsString = getDNS(entryHost)
        println("Config dns = $dnsString")
        config.getInterface().setDnsString(dnsString)

        // CRITICAL: Modify endpoint for multi-hop with V2Ray
        val endpoint = if (v2rayController.isV2RayEnabled()) {
            // Use local V2Ray proxy
            v2rayController.getLocalProxyEndpoint()
        } else {
            // Direct multi-hop connection
            "${entryHost.host}:${exitHost.multihopPort}"
        }

        val peer = Peer().also {
            it.setAllowedIPsString("0.0.0.0/0, ::/0")
            it.setEndpointString(endpoint)  // This is the key change!
            it.publicKey = exitHost.publicKey
        }

        if (!settings.wireGuardPresharedKey.isNullOrEmpty()) {
            peer.preSharedKey = settings.wireGuardPresharedKey
        }

        config.peers = listOf(peer)

        LOGGER.info("WireGuard multi-hop endpoint: $endpoint (V2Ray: ${v2rayController.isV2RayEnabled()})")

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