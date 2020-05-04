package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.multihop.MultiHopController
import net.ivpn.client.common.pinger.OnPingFinishListener
import net.ivpn.client.common.pinger.PingProvider
import net.ivpn.client.common.pinger.PingResultFormatter
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.vpn.controller.VpnBehaviorController
import javax.inject.Inject

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
    val pingResultExitServer = ObservableField<PingResultFormatter>()
    val pingResultEnterServer = ObservableField<PingResultFormatter>()

    init {
        multiHopController.addListener(getOnMultihopValueChanges())
    }

    fun onResume() {
        entryServer.set(getCurrentServer(ServerType.ENTRY))
        exitServer.set(getCurrentServer(ServerType.EXIT))

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
            return false;
        }

        return settings.isFastestServerEnabled
    }

    private fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    private fun ping(server: Server?, listener: OnPingFinishListener) {
        pingProvider.ping(server, listener)
    }

    private fun getOnMultihopValueChanges() : MultiHopController.onValueChangeListener {
        return object : MultiHopController.onValueChangeListener {
            override fun onValueChange(value: Boolean) {
                fastestServer.set(isFastestServerEnabled())
            }
        }
    }

    private fun getPingFinishListener(serverType: ServerType): OnPingFinishListener {
        return OnPingFinishListener { result: PingResultFormatter? ->
            if (serverType == ServerType.ENTRY) {
                pingResultEnterServer.set(result)
            } else {
                pingResultExitServer.set(result)
            }
        }
    }
}