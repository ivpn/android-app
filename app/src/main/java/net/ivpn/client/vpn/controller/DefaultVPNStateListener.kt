package net.ivpn.client.vpn.controller

import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.ui.dialog.Dialogs

open class DefaultVPNStateListener: VpnStateListener {
    override fun onAuthFailed() {
    }

    override fun onRegenerationError(errorDialog: Dialogs?) {
    }

    override fun onFindingFastestServer() {
    }

    override fun notifyAnotherPortUsedToConnect() {
    }

    override fun notifyServerAsFastest(server: Server) {
    }

    override fun onTimeTick(millisUntilResumed: Long) {
    }

    override fun onRegeneratingKeys() {
    }

    override fun notifyNoNetworkConnection() {
    }

    override fun onCheckSessionState() {
    }

    override fun onConnectionStateChanged(state: ConnectionState?) {
    }

    override fun onTimeOut() {
    }

    override fun onRegenerationSuccess() {
    }
}