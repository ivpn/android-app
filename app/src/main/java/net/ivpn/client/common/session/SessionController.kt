package net.ivpn.client.common.session

import net.ivpn.client.IVPNApplication
import net.ivpn.client.common.Mapper
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.model.ServiceStatus
import net.ivpn.client.rest.data.model.WireGuard
import net.ivpn.client.rest.data.session.*
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.rest.requests.common.Request
import net.ivpn.client.v2.viewmodel.AccountViewModel
import net.ivpn.client.vpn.Protocol
import net.ivpn.client.vpn.ProtocolController
import net.ivpn.client.vpn.controller.VpnBehaviorController
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SessionController @Inject constructor(
        private val userPreference: UserPreference,
        private val settings: Settings,
        private val vpnBehaviorController: VpnBehaviorController,
        private val protocolController: ProtocolController,
        clientFactory: HttpClientFactory,
        serversRepository: ServersRepository
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(AccountViewModel::class.java)
    }

    private val deleteSessionRequest: Request<DeleteSessionResponse> = Request(settings, clientFactory, serversRepository, Request.Duration.SHORT)
    private val sessionStatusRequest: Request<SessionStatusResponse> = Request(settings, clientFactory, serversRepository, Request.Duration.SHORT)
    private val sessionNewRequest: Request<SessionNewResponse> = Request(settings, clientFactory, serversRepository, Request.Duration.SHORT)

    private val listeners = arrayListOf<SessionListener>()

    fun subscribe(listener: SessionListener) {
        listeners.add(listener)
    }

    fun createSession(force: Boolean) {
        val body = SessionNewRequestBody(getUsername(), getWireGuardPublicKey(), force)
        sessionNewRequest.start({ api: IVPNApi -> api.newSession(body) },
                object : RequestListener<SessionNewResponse> {
                    override fun onSuccess(response: SessionNewResponse) {
                        LOGGER.info(response.toString())
                        onCreateSuccess(response)
                    }

                    override fun onError(throwable: Throwable) {
                        LOGGER.error("On create session throwable = $throwable")
                        onCreateError(throwable, null)
                    }

                    override fun onError(error: String) {
                        LOGGER.error("On create session error = $error")
                        val errorResponse = Mapper.errorResponseFrom(error)

                        if (errorResponse == null || errorResponse.status == null) {
                            vpnBehaviorController.connectionActionByUser()
                            return
                        }
                        when (errorResponse.status) {
                            Responses.INVALID_CREDENTIALS, Responses.SESSION_TOO_MANY -> {
                            }
                            else -> {
                                vpnBehaviorController.connectionActionByUser()
                            }
                        }

                        onCreateError(null, errorResponse)
                    }
                })
    }

    fun updateSessionStatus() {
        val sessionToken = getSessionToken()
        if (sessionToken == null || sessionToken.isEmpty()) {
            return
        }

        val body = SessionStatusRequestBody(getSessionToken())
        LOGGER.info("SessionStatusRequestBody = $body")

        sessionStatusRequest.start({ api: IVPNApi -> api.sessionStatus(body) },
                object : RequestListener<SessionStatusResponse> {
                    override fun onSuccess(response: SessionStatusResponse) {
                        if (response.status != null && response.status == Responses.SUCCESS) {
                            LOGGER.info("Session status response received successfully")
                            LOGGER.info(response.toString())
                            saveSessionStatus(response.serviceStatus)
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        LOGGER.error("Failed updating session status throwable = $throwable")
                        onUpdateError(throwable, null)
                    }

                    override fun onError(error: String) {
                        LOGGER.error("Error while getting account status to see the confirmation$error")
                        val errorResponse = Mapper.errorResponseFrom(error)
                        errorResponse?.let {
                            if (it.status == Responses.SERVICE_IS_NOT_ACTIVE){
                                userPreference.putIsActive(false)
                            }
                        }
                        onUpdateError(null, errorResponse)
                    }
                })
    }

    fun logOut() {
        val token = userPreference.sessionToken
        val requestBody = DeleteSessionRequestBody(token)

        deleteSessionRequest.start({ api: IVPNApi -> api.deleteSession(requestBody) },
                object : RequestListener<DeleteSessionResponse?> {
                    override fun onSuccess(response: DeleteSessionResponse?) {
                        LOGGER.info("Deleting session from server state: SUCCESS")
                        LOGGER.info(response.toString())
                        onRemoveSuccess()
                    }

                    override fun onError(throwable: Throwable) {
                        LOGGER.error("Error while deleting session from server", throwable)
                        onRemoveError()
                    }

                    override fun onError(error: String) {
                        LOGGER.error("Error while deleting session from server", error)
                        onRemoveError()
                    }
                })
    }

    fun cancel() {
        deleteSessionRequest.cancel()
    }

    private fun onRemoveSuccess() {
        clearData()
        for (listener in listeners) {
            listener.onRemoveSuccess()
        }
    }

    private fun onRemoveError() {
        clearData()
        for (listener in listeners) {
            listener.onRemoveError()
        }
    }

    private fun onCreateSuccess(response: SessionNewResponse) {
        if (response.status == null) {
            vpnBehaviorController.connectionActionByUser()
            return
        }

        if (response.status == Responses.SUCCESS) {
            putUserData(response)
            handleWireGuardResponse(response.wireGuard)
            vpnBehaviorController.connectionActionByUser()
        }

        for (listener in listeners) {
            listener.onCreateSuccess(response)
        }
    }

    private fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        for (listener in listeners) {
            listener.onCreateError(throwable, errorResponse)
        }
    }

    private fun onUpdateSuccess() {
        for (listener in listeners) {
            listener.onUpdateSuccess()
        }
    }

    private fun onUpdateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        for (listener in listeners) {
            listener.onUpdateError(throwable, errorResponse)
        }
    }

    private fun clearData() {
        IVPNApplication.getApplication().appComponent.provideComponentUtil().resetComponents()
    }

    private fun getUsername(): String? {
        return userPreference.userLogin
    }

    private fun getWireGuardPublicKey(): String? {
        return settings.wireGuardPublicKey
    }

    private fun getSessionToken(): String? {
        return userPreference.sessionToken
    }

    private fun putUserData(response: SessionNewResponse) {
        LOGGER.info("Save account data")
        userPreference.putSessionToken(response.token)
        userPreference.putSessionUsername(response.vpnUsername)
        userPreference.putSessionPassword(response.vpnPassword)
        saveSessionStatus(response.serviceStatus)
    }

    private fun saveSessionStatus(serviceStatus: ServiceStatus) {
        if (serviceStatus.isOnFreeTrial == null
                || serviceStatus.activeUntil == 0L) {
            return
        }
        userPreference.putIsUserOnTrial(java.lang.Boolean.valueOf(serviceStatus.isOnFreeTrial))
        userPreference.putAvailableUntil(serviceStatus.activeUntil)
        userPreference.putCurrentPlan(serviceStatus.currentPlan)
        userPreference.putPaymentMethod(serviceStatus.paymentMethod)
        userPreference.putIsActive(serviceStatus.isActive)
        if (serviceStatus.capabilities != null) {
            userPreference.putIsUserOnPrivateEmailBeta(serviceStatus.capabilities.contains(Responses.PRIVATE_EMAILS))
            val multiHopCapabilities = serviceStatus.capabilities.contains(Responses.MULTI_HOP)
            userPreference.putCapabilityMultiHop(serviceStatus.capabilities.contains(Responses.MULTI_HOP))
            if (!multiHopCapabilities) {
                settings.enableMultiHop(false)
            }
        }
    }

    private fun handleWireGuardResponse(wireGuard: WireGuard?) {
        LOGGER.info("Handle WireGuard response: $wireGuard")
        if (wireGuard == null || wireGuard.status == null) {
            //ignore it right now
            resetWireGuard()
            return
        }

        if (wireGuard.status == Responses.SUCCESS) {
            putWireGuardData(wireGuard)
        } else {
            LOGGER.error("Error received: ${wireGuard.status} ${wireGuard.message}")
            resetWireGuard()
        }
    }

    private fun putWireGuardData(wireGuard: WireGuard) {
        LOGGER.info("Save WireGuard data")
        settings.wireGuardIpAddress = wireGuard.ipAddress
    }

    private fun resetWireGuard() {
        LOGGER.info("Reset WireGuard protocol")
        protocolController.currentProtocol = Protocol.OPENVPN
    }

    interface SessionListener {
        fun onRemoveSuccess()

        fun onRemoveError()

        fun onCreateSuccess(response: SessionNewResponse)

        fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?)

        fun onUpdateSuccess()

        fun onUpdateError(throwable: Throwable?, errorResponse: ErrorResponse?)
    }
}