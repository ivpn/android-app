package net.ivpn.client.v2.login

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.wireguard.android.crypto.Keypair
import net.ivpn.client.R
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.Mapper
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.utils.ConnectivityUtil
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.model.WireGuard
import net.ivpn.client.rest.data.session.SessionNewRequestBody
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.rest.requests.common.Request
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.login.LoginNavigator
import net.ivpn.client.vpn.Protocol
import net.ivpn.client.vpn.ProtocolController
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        private val context: Context,
        private val buildController: BuildController,
        private val userPreference: UserPreference,
        private val protocolController: ProtocolController,
        private val settings: Settings,
        clientFactory: HttpClientFactory,
        serversRepository: ServersRepository
) : ViewModel() {

    companion object {
        private const val ivpnPrefix = "ivpn"
        private val LOGGER = LoggerFactory.getLogger(LoginViewModel::class.java)
    }

    val username = ObservableField<String>()
    val usernameError = ObservableField<String>()
    val dataLoading = ObservableBoolean()

    var navigator: LoginNavigator? = null
    private var request: Request<SessionNewResponse> = Request(settings, clientFactory, serversRepository, Request.Duration.SHORT)

    init {
        username.set(userPreference.userLogin)
    }

    fun login(force: Boolean) {
        LOGGER.info("Trying to login")
        username.get()?.let {
            if (!it.startsWith(ivpnPrefix)) {
                usernameError.set("Account ID should start with \"ivpn\".")
                return
            }
        } ?: return

        dataLoading.set(true)
        resetErrors()
        username.get()?.let {
            login(it.trim(), getWgKeyPair(), force)
        }
    }

    fun login(username: String, keys: Keypair?, force: Boolean) {
        val publicKey = keys?.publicKey
        val body = SessionNewRequestBody(username, publicKey, force)
        request.start({ api: IVPNApi -> api.newSession(body) },
                object : RequestListener<SessionNewResponse> {
                    override fun onSuccess(response: SessionNewResponse) {
                        LOGGER.info("Login process: SUCCESS. Response = $response")
                        dataLoading.set(false)
                        this@LoginViewModel.onSuccess(username, keys, response)
                    }

                    override fun onError(throwable: Throwable) {
                        LOGGER.error("Login process: ERROR", throwable)
                        if (!ConnectivityUtil.isOnline(context)) {
                            navigator?.openErrorDialogue(Dialogs.CONNECTION_ERROR)
                            dataLoading.set(false)
                            return
                        }
                        if (throwable is InterruptedIOException) {
                            dataLoading.set(false)
                            navigator?.openErrorDialogue(Dialogs.TOO_MANY_ATTEMPTS_ERROR)
                            return
                        }
                    }

                    override fun onError(error: String) {
                        LOGGER.error("Login process: ERROR: $error")
                        val errorResponse = Mapper.errorResponseFrom(error)
                        dataLoading.set(false)
                        handleErrorResponse(errorResponse)
                    }

                })
    }

    fun cancel() {
        LOGGER.info("cancel")
        dataLoading.set(false)
        request.cancel()
    }

    private fun getWgKeyPair(): Keypair? {
        val currentProtocol = protocolController.currentProtocol
        return if (currentProtocol == Protocol.WIREGUARD) {
            Keypair()
        } else null
    }

    private fun onSuccess(username: String, keys: Keypair?, response: SessionNewResponse) {
        response.status?.let {
            if (it != Responses.SUCCESS) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: return

        LOGGER.info("Status = ${response.status}")
        putUserData(username, response)
        handleWireGuardResponse(keys, response.wireGuard)
        if (userPreference.isActive) {
            navigator?.onLogin()
        } else {
            if (buildController.isIAPEnabled) {
                navigator?.openSubscriptionScreen()
            } else {
                navigator?.openSite()
            }
        }
    }

    private fun handleErrorResponse(errorResponse: ErrorResponse?) {
        errorResponse?.let {
            if (it.status == null) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: return

        when (errorResponse.status) {
            Responses.INVALID_CREDENTIALS -> {
                navigator?.openErrorDialogue(Dialogs.AUTHENTICATION_ERROR)
            }
            Responses.SESSION_TOO_MANY -> {
                navigator?.openSessionLimitReachedDialogue()
            }
            Responses.WIREGUARD_KEY_INVALID, Responses.WIREGUARD_PUBLIC_KEY_EXIST, Responses.BAD_REQUEST, Responses.SESSION_SERVICE_ERROR -> {
                navigator?.openCustomErrorDialogue(context.getString(R.string.dialogs_error) + errorResponse.status,
                        if (errorResponse.message != null) errorResponse.message else "")
            }
            else -> {
                navigator?.openCustomErrorDialogue(context.getString(R.string.dialogs_error) + errorResponse.status,
                        if (errorResponse.message != null) errorResponse.message else "")
            }
        }
    }

    private fun handleWireGuardResponse(keys: Keypair?, wireGuard: WireGuard?) {
        wireGuard?.let {
            if (it.status == null) {
                resetWireGuard()
                return
            }
        } ?: return

        if (wireGuard.status == Responses.SUCCESS) {
            putWireGuardData(keys, wireGuard)
        } else {
            LOGGER.error("Error received: " + wireGuard.status + " "
                    + if (wireGuard.message != null) wireGuard.message else "")
            resetWireGuard()
        }
    }

    private fun putUserData(username: String, response: SessionNewResponse) {
        LOGGER.info("Save account data")
        userPreference.putSessionToken(response.token)
        userPreference.putSessionUsername(response.vpnUsername)
        userPreference.putSessionPassword(response.vpnPassword)
        userPreference.putAvailableUntil(response.serviceStatus.activeUntil)
        userPreference.putIsUserOnTrial(java.lang.Boolean.valueOf(response.serviceStatus.isOnFreeTrial))
        userPreference.putCurrentPlan(response.serviceStatus.currentPlan)
        userPreference.putPaymentMethod(response.serviceStatus.paymentMethod)
        userPreference.putIsActive(response.serviceStatus.isActive)

        if (response.serviceStatus.capabilities != null) {
            userPreference.putIsUserOnPrivateEmailBeta(response.serviceStatus.capabilities.contains(Responses.PRIVATE_EMAILS))
            val multiHopCapabilities = response.serviceStatus.capabilities.contains(Responses.MULTI_HOP)
            userPreference.putCapabilityMultiHop(response.serviceStatus.capabilities.contains(Responses.MULTI_HOP))
            if (!multiHopCapabilities) {
                settings.enableMultiHop(false)
            }
        }
        userPreference.putUserLogin(username)
    }

    private fun resetErrors() {
        usernameError.set(null)
    }

    private fun putWireGuardData(keys: Keypair?, wireGuard: WireGuard) {
        settings.saveWireGuardKeypair(keys)
        settings.wireGuardIpAddress = wireGuard.ipAddress
    }

    private fun resetWireGuard() {
        protocolController.currentProtocol = Protocol.OPENVPN
    }
}