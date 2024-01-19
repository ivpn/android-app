package net.ivpn.core.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableLong
import androidx.lifecycle.ViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedUserPreference
import net.ivpn.core.common.qr.QRController
import net.ivpn.core.common.session.SessionController
import net.ivpn.core.common.session.SessionListenerImpl
import net.ivpn.core.common.utils.DateUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class AccountViewModel @Inject constructor(
    private val userPreference: EncryptedUserPreference,
    private val sessionController: SessionController
) : ViewModel() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AccountViewModel::class.java)
    }

    val dataLoading = ObservableBoolean()

    val username = ObservableField<String>()
    val subscriptionPlan = ObservableField<String>()
    val accountType = ObservableField<String>()
    val qrCode = ObservableField<Bitmap>()
    val authenticated = ObservableBoolean()
    val isOnFreeTrial = ObservableBoolean()
    val isNativeSubscription = ObservableBoolean()
    val availableUntil = ObservableLong()
    val isActive = ObservableBoolean()
    val deviceManagement = ObservableBoolean()
    val deviceName = ObservableField<String>()

    val isExpired = ObservableBoolean()
    val isExpiredIn = ObservableBoolean()
    val textIsExpiredIn = ObservableField<String>()
    var paymentMethod: String? = null

    var navigator: AccountNavigator? = null

    var logoutType: Type? = null

    init {
        sessionController.subscribe(object : SessionListenerImpl() {
            override fun onRemoveSuccess() {
                onRemoveSessionSuccess()
            }

            override fun onRemoveError() {
                onRemoveSessionFailed()
            }

            override fun onDeviceLoggedOut() {
                navigator?.onDeviceLoggedOut()
            }
        })
    }

    fun onResume() {
        username.set(getUsernameValue())
        accountType.set(getUserAccountType())
        isOnFreeTrial.set(isOnFreeTrial())
        availableUntil.set(getAvailableUntilValue())
        authenticated.set(isAuthenticated())
        isNativeSubscription.set(isNativeSubscription())
        subscriptionPlan.set(getSubscriptionPlan())
        isActive.set(getIsActiveValue())
        paymentMethod = getPaymentMethodValue()
        deviceManagement.set(getDeviceManagement())
        deviceName.set(getDeviceName())

        updateExpireData()
    }

    fun updateSessionStatus() {
        sessionController.updateSessionStatus()
    }

    fun drawQR(foregroundColor: Int, backgroundColor: Int, dimension: Int) {
        qrCode.set(QRController.getQR(username.get(), foregroundColor, backgroundColor, dimension))
    }

    fun logOut(type: Type) {
        logoutType = type
        dataLoading.set(true)
        sessionController.logOut()
    }

    fun forceLogout() {
        when (logoutType) {
            Type.LOGOUT -> logoutType = Type.FORCE_LOGOUT
            Type.LOGOUT_AND_CLEAR -> logoutType = Type.FORCE_LOGOUT_AND_CLEAR
            else -> {}
        }
        dataLoading.set(true)
        sessionController.logOut()
    }

    private fun onRemoveSessionSuccess() {
        dataLoading.set(false)
        clearLocalCache()
        when (logoutType) {
            Type.LOGOUT -> sessionController.clearSessionData()
            Type.LOGOUT_AND_CLEAR -> sessionController.clearData()
            Type.FORCE_LOGOUT -> sessionController.clearSessionData()
            Type.FORCE_LOGOUT_AND_CLEAR -> sessionController.clearData()
            else -> {}
        }
        navigator?.onLogOut()
    }

    private fun onRemoveSessionFailed() {
        dataLoading.set(false)
        when (logoutType) {
            Type.LOGOUT -> navigator?.onLogOutFailed()
            Type.LOGOUT_AND_CLEAR -> navigator?.onLogOutFailed()
            Type.FORCE_LOGOUT -> {
                sessionController.clearSessionData()
                navigator?.onLogOut()
            }
            Type.FORCE_LOGOUT_AND_CLEAR -> {
                sessionController.clearData()
                navigator?.onLogOut()
            }
            else -> {}
        }
    }

    fun cancel() {
        sessionController.cancel()
    }

    fun reset() {
        username.set(getUsernameValue())
        accountType.set(getUserAccountType())
        isOnFreeTrial.set(isOnFreeTrial())
        availableUntil.set(getAvailableUntilValue())
        authenticated.set(isAuthenticated())
        isNativeSubscription.set(isNativeSubscription())
        subscriptionPlan.set(getSubscriptionPlan())
    }

    fun isAccountStandard(): Boolean {
        return accountType.get()?.equals("IVPN Standard") ?: false
    }

    fun isAccountLegacy(): Boolean {
        return accountType.get()?.equals("Member VPN Pro Account") ?: false
    }

    fun isAccountNewStyle(): Boolean {
        return paymentMethod?.let {
            it == "prepaid"
        } ?: false
    }

    private fun clearLocalCache() {
        authenticated.set(false)
    }

    private fun updateExpireData() {
        val currentTime = System.currentTimeMillis()
        val expireTime = availableUntil.get() * 1000

        if (!authenticated.get()) {
            isExpired.set(false)
            isExpiredIn.set(false)
            return
        }

        when {
            expireTime < currentTime -> {
                isExpired.set(true)
                isExpiredIn.set(false)
            }
            (expireTime - currentTime) < DateUtil.DAYS_4 -> {
                isExpired.set(false)
                isExpiredIn.set(true)
                textIsExpiredIn.set(
                    "Subscription will expire in ${
                        DateUtil.formatSubscriptionTimeLeft(
                            expireTime
                        )
                    }"
                )
            }
            else -> {
                isExpired.set(false)
                isExpiredIn.set(false)
            }
        }
    }

    private fun getUsernameValue(): String? {
        return userPreference.getUserLogin()
    }

    private fun getUserAccountType(): String? {
        return userPreference.getCurrentPlan()
    }

    private fun isOnFreeTrial(): Boolean {
        return userPreference.isUserOnTrial()
    }

    private fun getAvailableUntilValue(): Long {
        return userPreference.getAvailableUntil()
    }

    private fun isAuthenticated(): Boolean {
        val token = userPreference.getSessionToken()
        return token.isNotEmpty()
    }

    private fun isNativeSubscription(): Boolean {
        val paymentMethod = userPreference.getPaymentMethod()
        if (paymentMethod != "ivpnandroidiap") {
            return false
        }

        return IVPNApplication.signUpController.isPurchaseAutoRenewing()
    }

    private fun getSubscriptionPlan(): String? {
        var plan = userPreference.getCurrentPlan()
        if (!userPreference.getIsActive()) {
            plan += " (inactive)"
            return plan
        }
        return plan
    }

    private fun getIsActiveValue(): Boolean {
        return userPreference.getIsActive()
    }

    private fun getPaymentMethodValue(): String {
        return userPreference.getPaymentMethod()
    }

    private fun getDeviceManagement(): Boolean {
        return userPreference.getDeviceManagement()
    }

    private fun getDeviceName(): String? {
        return userPreference.getDeviceName()
    }

    interface AccountNavigator {
        fun onLogOut()

        fun onLogOutFailed()

        fun onDeviceLoggedOut()
    }

    enum class Type {
        LOGOUT,
        LOGOUT_AND_CLEAR,
        FORCE_LOGOUT,
        FORCE_LOGOUT_AND_CLEAR
    }
}