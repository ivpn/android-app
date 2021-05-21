package net.ivpn.client.v2.viewmodel

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

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.R
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.common.utils.ConnectivityUtil
import net.ivpn.client.common.utils.StringUtil
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.v2.connect.createSession.ConnectionNavigator
import net.ivpn.client.v2.connect.createSession.ConnectionState
import net.ivpn.client.v2.dialog.Dialogs
import net.ivpn.client.v2.connect.MapDialogs
import net.ivpn.client.vpn.controller.DefaultVPNStateListener
import net.ivpn.client.vpn.controller.VpnBehaviorController
import net.ivpn.client.vpn.controller.VpnStateListener
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import javax.inject.Inject

@ApplicationScope
class ConnectionViewModel @Inject constructor(
        private val context: Context,
        private val vpnBehaviorController: VpnBehaviorController
) : ViewModel(), SessionController.SessionListener {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectionViewModel::class.java)
    }

    val isProtected = ObservableBoolean()
    val isConnected = ObservableBoolean()
    val connectionStatus = ObservableField<String>()
    val serverConnectionHint = ObservableField<String>()

    val connectionState = ObservableField<ConnectionState>()
    val isPauseAvailable = ObservableBoolean()
    val isPaused = ObservableBoolean()
    val timeUntilResumed = ObservableField<String>()

    var touchListener = View.OnTouchListener { _, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            if (!isPaused.get()) {
                navigator?.askConnectionPermission()
            }
            return@OnTouchListener true
        }

        false
    }

    var navigator: ConnectionNavigator? = null
    var resumeDialog: MapDialogs.ResumeDialogListener? = null

    init {
        vpnBehaviorController.addVpnStateListener(getVPNStateListener())
    }

    fun onConnectRequest() {
        vpnBehaviorController.connectionActionByUser()
    }

    fun resume() {
        vpnBehaviorController.resumeActionByUser()
    }

    fun connectIfNot() {
        if (!isConnected.get()) {
            vpnBehaviorController.connectActionByRules()
        }
    }

    fun connectOrReconnect() {
        vpnBehaviorController.onServerUpdated(true)
    }

    fun reconnectOrNothing() {
        vpnBehaviorController.onServerUpdated(false)
    }

    fun onPauseRequest() {
        vpnBehaviorController.pauseActionByUser()
    }

    fun onStopRequest() {
        vpnBehaviorController.stopActionByUser()
    }

    fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    fun disconnect() {
        vpnBehaviorController.disconnect()
    }

    fun reset() {
    }

    private fun getVPNStateListener(): VpnStateListener {
        return object : DefaultVPNStateListener() {
            override fun onAuthFailed() {
                navigator?.onAuthFailed()
            }

            override fun onTimeTick(millisUntilResumed: Long) {
                timeUntilResumed.set(StringUtil.formatTimeUntilResumed(millisUntilResumed))
            }

            override fun onTimerFinish() {
                resumeDialog?.onTimerFinish()
            }

            override fun notifyAnotherPortUsedToConnect() {
                navigator?.notifyAnotherPortUsedToConnect()
            }

            override fun onTimeOut() {
                navigator?.onTimeOut()
            }

            override fun onRegenerationError(errorDialog: Dialogs?) {
                navigator?.openErrorDialog(errorDialog)
            }

            override fun notifyNoNetworkConnection() {
                navigator?.openNoNetworkDialog()
            }

            override fun onConnectionStateChanged(state: ConnectionState?) {
                if (state == null) {
                    return
                }
                connectionState.set(state)
                when (state) {
                    ConnectionState.CONNECTED -> {
                        isProtected.set(true)
                        isConnected.set(true)
                        isPaused.set(false)
                        isPauseAvailable.set(true)
                        connectionStatus.set(context.getString(R.string.connect_status_connected))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_connected))
                    }
                    ConnectionState.CONNECTING -> {
                        isProtected.set(true)
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_connecting))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_connecting))
                    }
                    ConnectionState.DISCONNECTING -> {
                        isProtected.set(false)
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_disconnecting))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_disconnecting))
                    }
                    ConnectionState.NOT_CONNECTED -> {
                        isProtected.set(false)
                        isConnected.set(false)
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_not_connected))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_disconnected))
                    }
                    ConnectionState.PAUSING -> {
                        isProtected.set(true)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_pausing))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_pausing))
                    }
                    ConnectionState.PAUSED -> {
                        isProtected.set(true)
                        isPaused.set(true)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_paused))
                        serverConnectionHint.set(context.getString(R.string.connect_server_hint_paused))
                    }
                }
            }
        }
    }

    override fun onRemoveSuccess() {
    }

    override fun onRemoveError() {
    }

    override fun onCreateSuccess(response: SessionNewResponse) {
        if (response.status == null) {
            return
        }

        LOGGER.info("Status = " + response.status)
        if (response.status != Responses.SUCCESS) {
            navigator?.openErrorDialog(Dialogs.SERVER_ERROR)
        }
    }

    override fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        if (!ConnectivityUtil.isOnline(context)) {
            navigator?.openErrorDialog(Dialogs.CONNECTION_ERROR)
            return
        }

        throwable?.let {
            if (it is InterruptedIOException) {
                navigator?.openErrorDialog(Dialogs.TOO_MANY_ATTEMPTS_ERROR)
                return
            }
        }

        when (errorResponse?.status) {
            Responses.INVALID_CREDENTIALS -> {
                navigator?.openErrorDialog(Dialogs.AUTHENTICATION_ERROR)
                navigator?.logout()
            }
            Responses.SESSION_TOO_MANY -> {
                navigator?.openSessionLimitReachedDialogue()
            }
        }
    }

    override fun onUpdateSuccess() {
    }

    override fun onUpdateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        errorResponse?.let {
            if (it.status == Responses.SESSION_NOT_FOUND){
                navigator?.logout()
            }
        }
    }
}