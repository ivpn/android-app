package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.multihop.MultiHopController
import net.ivpn.client.common.pinger.OnPingFinishListener
import net.ivpn.client.common.pinger.PingProvider
import net.ivpn.client.common.pinger.PingResultFormatter
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.vpn.OnProtocolChangedListener
import net.ivpn.client.vpn.Protocol
import net.ivpn.client.vpn.ProtocolController
import net.ivpn.client.vpn.controller.DefaultVPNStateListener
import net.ivpn.client.vpn.controller.VpnBehaviorController
import net.ivpn.client.vpn.controller.VpnStateListener
import javax.inject.Inject

@ApplicationScope
class ServersViewModel @Inject constructor(
        val serversRepository: ServersRepository,
        private val multiHopController: MultiHopController,
        val settings: Settings,
        val vpnBehaviorController: VpnBehaviorController,
        val pingProvider: PingProvider
) : ViewModel() {

    val fastestServer = ObservableBoolean()
    val entryServer = ObservableField<Server>()
    val exitServer = ObservableField<Server>()
    val mapServer = ObservableField<Server>()
    val pingResultExitServer = ObservableField<PingResultFormatter>()
    val pingResultEnterServer = ObservableField<PingResultFormatter>()

    init {
        multiHopController.addListener(getOnMultihopValueChanges())
        vpnBehaviorController.addVpnStateListener(getVPNStateListener())
    }

    fun onResume() {
        entryServer.set(getCurrentServer(ServerType.ENTRY))
        exitServer.set(getCurrentServer(ServerType.EXIT))
        mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())

        fastestServer.set(isFastestServerEnabled())

        pingResultExitServer.set(null)
        pingResultEnterServer.set(null)

        ping(entryServer.get(), getPingFinishListener(ServerType.ENTRY))
        ping(exitServer.get(), getPingFinishListener(ServerType.EXIT))
    }

    fun reset() {
        entryServer.set(getCurrentServer(ServerType.ENTRY))
        exitServer.set(getCurrentServer(ServerType.EXIT))
        mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())

        fastestServer.set(isFastestServerEnabled())

        pingResultExitServer.set(null)
        pingResultEnterServer.set(null)

        ping(entryServer.get(), getPingFinishListener(ServerType.ENTRY))
        ping(exitServer.get(), getPingFinishListener(ServerType.EXIT))
    }

    private fun getCurrentServer(serverType: ServerType): Server? {
        return serversRepository.getCurrentServer(serverType)
    }

    private fun isFastestServerEnabled(): Boolean {
        if (multiHopController.isEnabled || isVpnActive()) {
            return false
        }

        return settings.isFastestServerEnabled
    }

    private fun getVPNStateListener(): VpnStateListener {
        return object : DefaultVPNStateListener() {

            override fun onConnectionStateChanged(state: ConnectionState?) {
                if (state == null) {
                    return
                }
                updateFastestServer(state)
            }

            override fun notifyServerAsFastest(server: Server) {
                entryServer.set(server)
                mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())
            }
        }
    }

    private fun updateFastestServer(state: ConnectionState) {
        fastestServer.set(isFastestServerEnabled())
    }

    private fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    private fun ping(server: Server?, listener: OnPingFinishListener) {
        pingProvider.ping(server, listener)
    }

    private fun getOnMultihopValueChanges(): MultiHopController.OnValueChangeListener {
        return object : MultiHopController.OnValueChangeListener {
            override fun onValueChange(value: Boolean) {
                fastestServer.set(isFastestServerEnabled())
                mapServer.set(if (value) exitServer.get() else entryServer.get())
            }
        }
    }

    private fun getPingFinishListener(serverType: ServerType): OnPingFinishListener {
        return OnPingFinishListener { _, result: PingResultFormatter? ->
            if (serverType == ServerType.ENTRY) {
                pingResultEnterServer.set(result)
            } else {
                pingResultExitServer.set(result)
            }
        }
    }

    fun setServerLocation(serverLocation: ServerLocation) {
        val serverToConnect: Server = getServerFor(serverLocation) ?: return

        if (multiHopController.isEnabled) {
            if (serverToConnect.canBeUsedAsMultiHopWith(entryServer.get())) {
                exitServer.set(serverToConnect)
                serversRepository.serverSelected(serverToConnect, ServerType.EXIT)
            }
        } else {
            entryServer.set(serverToConnect)
            serversRepository.serverSelected(serverToConnect, ServerType.ENTRY)
        }
        mapServer.set(if (multiHopController.isEnabled) exitServer.get() else entryServer.get())
    }

    fun getServerFor(serverLocation: ServerLocation): Server? {
        var serverForLocation: Server? = null
        for (server in serversRepository.getServers(false)) {
            if (serverLocation.city == server.city) {
                serverForLocation = server
                break
            }
        }
        return serverForLocation
    }

    fun isLocationSuitable(serverLocation: ServerLocation): Boolean {
        if (multiHopController.isEnabled) {
            entryServer.get()?.let {
                return it.city != serverLocation.city
            }
        }

        return true
    }
}