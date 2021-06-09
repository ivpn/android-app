package net.ivpn.client.v2.login

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
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.R
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.EncryptedUserPreference
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.common.session.SessionListenerImpl
import net.ivpn.client.common.utils.ConnectivityUtil
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.v2.dialog.Dialogs
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import javax.inject.Inject

@ApplicationScope
class LoginViewModel @Inject constructor(
        private val context: Context,
        private val userPreference: EncryptedUserPreference,
        private val sessionController: SessionController
) : ViewModel() {

    companion object {
        private const val ivpnPrefix = "ivpn"
        private const val ivpnNewPrefix = "i-"
        private val LOGGER = LoggerFactory.getLogger(LoginViewModel::class.java)
    }

    val username = ObservableField<String>()
    val error = ObservableField<String>()

    val captchaInputState = ObservableField<InputState>()
    val tfaInputState = ObservableField<InputState>()
    val loginInputState = ObservableField<InputState>()

    val tfaToken = ObservableField<String>()
    val captcha = ObservableField<String>()
    var captchaId: String? = null
    var captchaImage = ObservableField<String>()

    val dataLoading = ObservableBoolean()

    var paymentMethod: String? = null

    var navigator: LoginNavigator? = null

    var pendingCaptcha: String? = null
    var pending2FAToken: String? = null

    var tfaFocusListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            tfaInputState.get()?.let {
                if (it == InputState.NORMAL) {
                    tfaInputState.set(InputState.FOCUSED)
                }
            } ?: kotlin.run {
                tfaInputState.set(InputState.FOCUSED)
            }
        } else {
            tfaInputState.set(InputState.NORMAL)
        }
    }

    var loginFocusListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            loginInputState.get()?.let {
                if (it == InputState.NORMAL) {
                    loginInputState.set(InputState.FOCUSED)
                }
            } ?: kotlin.run {
                loginInputState.set(InputState.FOCUSED)
            }
        } else {
            loginInputState.set(InputState.NORMAL)
        }
    }

    val captchaFocusListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            captchaInputState.get()?.let {
                if (it == InputState.NORMAL) {
                    captchaInputState.set(InputState.FOCUSED)
                }
            } ?: kotlin.run {
                captchaInputState.set(InputState.FOCUSED)
            }
        } else {
            captchaInputState.set(InputState.NORMAL)
        }
    }

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

        username.set(userPreference.getUserLogin())
    }

    fun onResume() {
        paymentMethod = getPaymentMethodValue()
    }

    fun login(force: Boolean) {
        LOGGER.info("Trying to login")
        username.get()?.let {
            if (!(it.startsWith(ivpnPrefix) || it.startsWith(ivpnNewPrefix))) {
                loginInputState.set(InputState.ERROR)
                error.set("Your account ID has to be in 'i-XXXX-XXXX-XXXX' or 'ivpnXXXXXXXX'" +
                        " format. You can find it on other devices where you are logged in and in the" +
                        " client area of the IVPN website.")
                return
            }
        } ?: kotlin.run {
            loginInputState.set(InputState.ERROR)
            return
        }

        dataLoading.set(true)
        resetErrors()
        username.get()?.let { usernameObj ->
            pendingCaptcha?.let { captchaObj ->
                captchaId?.let { captchaIdObj ->
                    sessionController.createSessionWithCaptcha(force, usernameObj.trim(), captchaIdObj, captchaObj)
                    pendingCaptcha = null
                    return
                }
            }
            pending2FAToken?.let { tfaTokenObj ->
                sessionController.createSessionWith2FAToken(force, usernameObj.trim(), tfaTokenObj)
                pending2FAToken = null
                return
            }
            login(usernameObj.trim(), force)
        }
    }

    fun submit2FAToken() {
        LOGGER.info("submit2FAToken")
        tfaToken.get()?.let {
            if (it.length != 6 || !it.isDigitsOnly()) {
                error.set("Please enter 6-digit verification code")
                tfaInputState.set(InputState.ERROR)
                return
            }
            username.get()?.let { usernameObj ->
                dataLoading.set(true)
                sessionController.createSessionWith2FAToken(false, usernameObj, it)
            }
            resetErrors()
        } ?: kotlin.run {
            error.set("Please enter 6-digit verification code")
            tfaInputState.set(InputState.ERROR)
        }
    }

    fun submitCaptcha() {
        LOGGER.info("submitCaptcha navigator = $navigator")
        captcha.get()?.let {
            if (it.isEmpty()) {
                error.set("Please enter 6-digit verification code")
                captchaInputState.set(InputState.ERROR)
                return
            }
            username.get()?.let { usernameObj ->
                captchaId?.let { captchaIdObj ->
                    dataLoading.set(true)
                    sessionController.createSessionWithCaptcha(false, usernameObj, captchaIdObj, it)
                }
            }
            resetErrors()
        } ?: kotlin.run {
            error.set("Please enter 6-digit verification code")
            captchaInputState.set(InputState.ERROR)
        }
    }

    fun login(username: String, force: Boolean) {
        sessionController.createSession(force, username)
    }

    fun isAccountNewStyle(): Boolean {
        return paymentMethod?.let {
            it == "prepaid"
        } ?: false
    }

    fun getAccountType(): String? {
        return userPreference.getCurrentPlan()
    }

    fun cancel() {
        LOGGER.info("cancel")
        dataLoading.set(false)
        sessionController.cancel()
    }

    private fun onSuccess(response: SessionNewResponse) {
        response.status?.let {
            if (it != Responses.SUCCESS) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: return

        LOGGER.info("Status = ${response.status}")
        username.get()?.let { accountId ->
            if (accountId.isNotEmpty()) {
                userPreference.putUserLogin(accountId)
            }
        }
        if (userPreference.getIsActive()) {
            navigator?.onLogin()
        } else {
            navigator?.onLoginWithInactiveAccount()
        }
    }

    private fun handleErrorResponse(errorResponse: ErrorResponse?) {
        errorResponse?.let {
            if (it.status == null) {
                navigator?.openErrorDialogue(Dialogs.SERVER_ERROR)
                return
            }
        } ?: kotlin.run {
            navigator?.openErrorDialogue(Dialogs.CREATE_SESSION_ERROR)
            return
        }

        when (errorResponse.status) {
            Responses.ACCOUNT_NOT_ACTIVE -> {
                navigator?.onLoginWithBlankAccount()
            }
            Responses.ENTER_TOTP_TOKEN -> {
                navigator?.openTFAScreen()
            }
            Responses.INVALID_TOTP_TOKEN -> {
                tfaToken.set(null)
                error.set("Specified two-factor authentication token is not valid")
                tfaInputState.set(InputState.ERROR)
            }
            Responses.INVALID_CREDENTIALS -> {
                navigator?.onInvalidAccount()
                navigator?.openErrorDialogue(Dialogs.AUTHENTICATION_ERROR)
            }
            Responses.SESSION_TOO_MANY -> {
                pendingCaptcha = captcha.get()
                pending2FAToken = tfaToken.get()
                navigator?.openSessionLimitReachedDialogue()
            }
            Responses.INVALID_CAPTCHA -> {
                error.set("Invalid captcha, please try again")
                captchaInputState.set(InputState.ERROR)
                captcha.set(null)
                captchaId = errorResponse.captchaId
                captchaImage.set(errorResponse.captchaImage)
                println("captchaId = $captchaId")
                println("captchaImage = $captchaImage")
                navigator?.openCaptcha()
            }
            Responses.CAPTCHA_REQUIRED -> {
                captchaId = errorResponse.captchaId
                captchaImage.set(errorResponse.captchaImage)
                println("captchaId = $captchaId")
                println("captchaImage = $captchaImage")
                navigator?.openCaptcha()
            }
            Responses.WIREGUARD_KEY_INVALID, Responses.WIREGUARD_PUBLIC_KEY_EXIST, Responses.BAD_REQUEST, Responses.SESSION_SERVICE_ERROR -> {
                navigator?.openCustomErrorDialogue("${context.getString(R.string.dialogs_error)} ${errorResponse.status}",
                        if (errorResponse.message != null) errorResponse.message else "")
            }
            else -> {
                navigator?.openCustomErrorDialogue("${context.getString(R.string.dialogs_error)} ${errorResponse.status}",
                        if (errorResponse.message != null) errorResponse.message else "")
            }
        }
    }

    fun reset() {
        error.set(null)
        captcha.set(null)
        tfaToken.set(null)
        captchaInputState.set(InputState.NORMAL)
        tfaInputState.set(InputState.NORMAL)
        loginInputState.set(InputState.NORMAL)
    }

    private fun resetErrors() {
        error.set(null)
        captchaInputState.set(InputState.NORMAL)
        tfaInputState.set(InputState.NORMAL)
        loginInputState.set(InputState.NORMAL)
    }

    private fun getPaymentMethodValue(): String {
        return userPreference.getPaymentMethod()
    }

    enum class InputState {
        NORMAL,
        FOCUSED,
        ERROR
    }

}