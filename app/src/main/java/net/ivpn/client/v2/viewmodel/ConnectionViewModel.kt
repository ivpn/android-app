package net.ivpn.client.v2.viewmodel

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.R
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.common.utils.ConnectivityUtil
import net.ivpn.client.common.utils.StringUtil
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.ui.connect.ConnectionNavigator
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.vpn.controller.DefaultVPNStateListener
import net.ivpn.client.vpn.controller.VpnBehaviorController
import net.ivpn.client.vpn.controller.VpnStateListener
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import javax.inject.Inject

class ConnectionViewModel @Inject constructor(
        private val context: Context,
        private val sessionController: SessionController,
        private val vpnBehaviorController: VpnBehaviorController,
        private val userPreference: UserPreference
) : ViewModel(), SessionController.SessionListener {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectionViewModel::class.java)
    }

    val isConnected = ObservableBoolean()
    val connectionStatus = ObservableField<String>()
    val connectionState = ObservableField<ConnectionState>()
    val connectionUserHint = ObservableField<String>()
    val isPauseAvailable = ObservableBoolean()
    val isPaused = ObservableBoolean()
    val timeUntilResumed = ObservableField<String>()
    val connectionViewHint = ObservableField<String>()

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

    init {
        vpnBehaviorController.addVpnStateListener(getVPNStateListener())
    }

    fun onConnectRequest() {
        if (!isTokenExist()) {
            createNewSession(false)
            return;
        }
        vpnBehaviorController.connectionActionByUser()
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

    private fun createNewSession(force: Boolean) {
        connectionUserHint.set(context.getString(R.string.connect_hint_creating_session))
        sessionController.createSession(force)
    }

    private fun isTokenExist(): Boolean {
        return userPreference.sessionToken.isNotEmpty()
    }

    private fun getVPNStateListener(): VpnStateListener {
        return object : DefaultVPNStateListener() {
            override fun onAuthFailed() {
                navigator?.onAuthFailed()
            }

            override fun onTimeTick(millisUntilResumed: Long) {
                timeUntilResumed.set(StringUtil.formatTimeUntilResumed(millisUntilResumed))
            }

            override fun notifyAnotherPortUsedToConnect() {
                navigator?.notifyAnotherPortUsedToConnect()
            }

            override fun onTimeOut() {
                navigator?.onTimeOut()
            }

            override fun onFindingFastestServer() {
                connectionUserHint.set(context.getString(R.string.connect_hint_finding_fastest))
            }

            override fun onRegeneratingKeys() {
                connectionUserHint.set(context.getString(R.string.connect_hint_regeneration_wg_key))
            }

            override fun onRegenerationSuccess() {
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected))
            }

            override fun onRegenerationError(errorDialog: Dialogs?) {
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected))
                navigator?.openErrorDialog(errorDialog)
            }

            override fun notifyNoNetworkConnection() {
                navigator?.openNoNetworkDialog()
            }

            override fun onConnectionStateChanged(state: ConnectionState?) {
                if (state == null) {
                    return
                }
                when (state) {
                    ConnectionState.CONNECTED -> {
                        isConnected.set(true)
                        isPaused.set(false)
                        isPauseAvailable.set(true)
                        navigator?.onChangeConnectionStatus(state)
                        connectionStatus.set(context.getString(R.string.connect_status_connected))
                        connectionUserHint.set(context.getString(R.string.connect_hint_connected))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_connected))
                    }
                    ConnectionState.CONNECTING -> {
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_connecting))
                        connectionUserHint.set(context.getString(R.string.connect_hint_connecting))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_connecting))
                    }
                    ConnectionState.DISCONNECTING -> {
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_disconnecting))
                        connectionUserHint.set(context.getString(R.string.connect_hint_disconnecting))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_disconnecting))
                    }
                    ConnectionState.NOT_CONNECTED -> {
                        isConnected.set(false)
                        isPaused.set(false)
                        isPauseAvailable.set(false)
                        navigator?.onChangeConnectionStatus(state)
                        connectionStatus.set(context.getString(R.string.connect_status_not_connected))
                        connectionUserHint.set(context.getString(R.string.connect_hint_not_connected))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_not_connected))
                    }
                    ConnectionState.PAUSING -> {
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_pausing))
                        connectionUserHint.set(context.getString(R.string.connect_hint_pausing))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_pausing))
                    }
                    ConnectionState.PAUSED -> {
                        isPaused.set(true)
                        isPauseAvailable.set(false)
                        connectionStatus.set(context.getString(R.string.connect_status_paused))
                        connectionUserHint.set(context.getString(R.string.connect_hint_paused))
                        connectionViewHint.set(context.getString(R.string.connect_state_hint_paused))
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
        connectionUserHint.set(context.getString(R.string.connect_hint_not_connected))
        if (response.status == null) {
            return
        }

        LOGGER.info("Status = " + response.status)
        if (response.status != Responses.SUCCESS) {
            navigator?.openErrorDialog(Dialogs.SERVER_ERROR)
        }
    }

    override fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        connectionUserHint.set(context.getString(R.string.connect_hint_not_connected))

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