package net.ivpn.client.vpn.wireguard

import com.wireguard.android.config.Config
import com.wireguard.android.config.Peer
import com.wireguard.android.model.Tunnel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.v2.protocol.port.Port
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
        val port = settings.wireGuardPort
        val server = serversRepository.getCurrentServer(ServerType.ENTRY)
        return generateConfig(server, port)
    }

    private fun generateConfig(server: Server?, port: Port): Config? {
        val config = Config()
        val privateKey = settings.wireGuardPrivateKey
        val publicKey = settings.wireGuardPublicKey
        val ipAddress = settings.wireGuardIpAddress

        LOGGER.info("Generating config:")
        if (server == null || server.hosts == null) {
            return null
        }
        if (config.getInterface().publicKey == null) {
            config.getInterface().privateKey = privateKey
        }
        val isIPv6Supported = server.hosts[0].ipv6 != null && settings.ipv6Setting

        val dnsString = getDNS(server)
        if (isIPv6Supported) {
            config.getInterface().setAddressString("$ipAddress/32,fd00:4956:504e:ffff::$ipAddress/128")
        } else {
            config.getInterface().setAddressString(ipAddress)
        }
        config.getInterface().setDnsString(dnsString)
        val peers = ArrayList<Peer>()
        var peer: Peer
        for (host in server.hosts) {
            peer = Peer()
            peer.setAllowedIPsString("0.0.0.0/0, ::/0")
            peer.setEndpointString(host.host + ":" + port.portNumber)
            peer.publicKey = host.publicKey
            peers.add(peer)
        }
        config.peers = peers
        return config
    }

    private fun getDNS(server: Server?): String {
        val dns = settings.dns
        if (dns != null) {
            return dns
        }
        return if (server!!.hosts == null || server.hosts[0] == null || server.hosts[0].localIp == null) {
            DEFAULT_DNS
        } else server.hosts[0].localIp.split("/".toRegex()).toTypedArray()[0]
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