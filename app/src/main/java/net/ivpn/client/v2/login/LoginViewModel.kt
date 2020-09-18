package net.ivpn.client.v2.login

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.R
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.common.session.SessionListenerImpl
import net.ivpn.client.common.utils.ConnectivityUtil
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.login.LoginNavigator
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        private val context: Context,
        private val userPreference: UserPreference,
        private val sessionController: SessionController
) : ViewModel() {

    companion object {
        private const val ivpnPrefix = "ivpn"
        private const val ivpnNewPrefix = "i-"
        private val LOGGER = LoggerFactory.getLogger(LoginViewModel::class.java)
    }

    val username = ObservableField<String>()
    val usernameError = ObservableField<String>()
    val dataLoading = ObservableBoolean()

    var navigator: LoginNavigator? = null

    init {
        dataLoading.set(false)
        sessionController.subscribe(object : SessionListenerImpl() {
            override fun onCreateSuccess(response: SessionNewResponse) {
                LOGGER.info("Login process: SUCCESS. Response = $response")
                dataLoading.set(false)
                this@LoginViewModel.onSuccess(response)
            }

            override fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
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

                LOGGER.error("Login process: ERROR: $errorResponse")
                dataLoading.set(false)
                handleErrorResponse(errorResponse)
            }
        })

        username.set(userPreference.userLogin)
    }

    fun login(force: Boolean) {
        LOGGER.info("Trying to login")
        username.get()?.let {
            if (!(it.startsWith(ivpnPrefix) || it.startsWith(ivpnNewPrefix))) {
                usernameError.set("Your account ID has to be in 'i-XXXX-XXXX-XXXX' or 'ivpnXXXXXXXX'" +
                        " format. You can find it on other devices where you are logged in and in the" +
                        " client area of the IVPN website.")
                return
            }
        } ?: return

        dataLoading.set(true)
        resetErrors()
        username.get()?.let {
            login(it.trim(), force)
        }
    }

    fun login(username: String, force: Boolean) {
        sessionController.createSession(force, username)
    }

    fun isAccountNewStyle(): Boolean {
        return username.get()?.startsWith("i-") ?: false
    }

    fun getAccountType(): String? {
        return userPreference.currentPlan
    }

    fun cancel() {
        LOGGER.info("cancel")
        dataLoading.set(false)
        sessionController.cancel()
//        request.cancel()
    }

    private fun onSuccess(response: SessionNewResponse) {
        response.status?.let {
            if (it != Responses.SUCCESS) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: return
//        response.serviceStatus?.let {
//            if (!it.isActive) {
//                navigator?.openAccountNotActiveBetaDialogue()
//                return
//            }
//        } ?: return

        LOGGER.info("Status = ${response.status}")
        username.get()?.let { accountId ->
            if (accountId.isNotEmpty()) {
                userPreference.putUserLogin(accountId)
            }
        }
        if (userPreference.isActive) {
            navigator?.onLogin()
        } else {
            navigator?.onLoginWithInactiveAccount()
        }
        //Disable only for beta
//            if (buildController.isIAPEnabled) {
//                navigator?.openSubscriptionScreen()
//            } else {
//                navigator?.openSite()
//            }
//        }
    }

    private fun handleErrorResponse(errorResponse: ErrorResponse?) {
        errorResponse?.let {
            if (it.status == null) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: return

        when (errorResponse.status) {
            Responses.ACCOUNT_NOT_ACTIVE -> {
                navigator?.onLoginWithInactiveAccount()
            }
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

    private fun resetErrors() {
        usernameError.set(null)
    }
}