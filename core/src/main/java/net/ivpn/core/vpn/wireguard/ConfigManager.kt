package net.ivpn.core.vpn.wireguard

import com.wireguard.android.config.Config
import com.wireguard.android.config.Peer
import com.wireguard.android.model.Tunnel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.multihop.MultiHopController
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Host
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.protocol.port.Port
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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
    private val multiHopController: MultiHopController
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
        applyConfigToTunnel(generateConfig())
        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.UP)
        }
    }

    fun stopWireGuard() {
        GlobalScope.launch {
            tunnel?.setState(Tunnel.State.DOWN)
        }
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
        return if (multiHopController.isReadyToUse()) {
            val exitServer = serversRepository.getCurrentServer(ServerType.EXIT)
            generateConfigForMultiHop(server, exitServer)
        } else {
            val port = settings.wireGuardPort
            generateConfig(server, port)
        }
    }

    private fun generateConfig(server: Server?, port: Port): Config? {
        val config = Config()
        val privateKey = settings.wireGuardPrivateKey

        LOGGER.info("Generating config:")
        if (server == null || server.hosts == null) {
            return null
        }

        val host = server.hosts.random()

        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }

        setAddress(config, listOf(host))

        val dnsString = getDNS(host)
        config.getInterface().setDnsString(dnsString)

        val peer = Peer().also {
            it.setAllowedIPsString("0.0.0.0/0, ::/0")
            it.setEndpointString(host.host + ":" + port.portNumber)
            it.publicKey = host.publicKey
        }

        config.peers = listOf(peer)
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

        val entryHost = entryServer.hosts.random()
        val exitHost = exitServer.hosts.random()

        setAddress(config, listOf(entryHost, exitHost))

        val dnsString = getDNS(entryHost)
        config.getInterface().setDnsString(dnsString)

        val peer = Peer().also {
            it.setAllowedIPsString("0.0.0.0/0, ::/0")
            it.setEndpointString(entryHost.host + ":" + exitHost.multihopPort)
            it.publicKey = exitHost.publicKey
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

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigManager::class.java)
        private const val WIREGUARD_TUNNEL_NAME = "IVPN"
        private const val DEFAULT_DNS = "172.16.0.1"
    }
}