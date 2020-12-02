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

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableLong
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.Purchase
import net.ivpn.client.common.billing.BillingManagerWrapper
import net.ivpn.client.common.billing.SubscriptionState
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.qr.QRController
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.common.session.SessionListenerImpl
import net.ivpn.client.common.utils.DateUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class AccountViewModel @Inject constructor(
        private val userPreference: UserPreference,
        private val billingManager: BillingManagerWrapper,
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
    val subscriptionState = ObservableField<SubscriptionState>()
    val authenticated = ObservableBoolean()
    val isOnFreeTrial = ObservableBoolean()
    val isNativeSubscription = ObservableBoolean()
    val availableUntil = ObservableLong()
    val isActive = ObservableBoolean()

    val isExpired = ObservableBoolean()
    val isExpiredIn = ObservableBoolean()
    val textIsExpiredIn = ObservableField<String>()
    var paymentMethod: String? = null

    var navigator: AccountNavigator? = null

    init {
        sessionController.subscribe(object : SessionListenerImpl() {
            override fun onRemoveSuccess() {
                dataLoading.set(false)
                clearLocalCache()
            }

            override fun onRemoveError() {
                dataLoading.set(false)
                clearLocalCache()
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
        subscriptionState.set(getSubscriptionState())
        subscriptionPlan.set(getSubscriptionPlan())
        isActive.set(getIsActiveValue())
        paymentMethod = getPaymentMethodValue()

        updateExpireData()
    }

    fun updateSessionStatus() {
        sessionController.updateSessionStatus()
    }

    fun drawQR(foregroundColor: Int, backgroundColor: Int, dimension: Int) {
        qrCode.set(QRController.getQR(username.get(), foregroundColor, backgroundColor, dimension))
    }

    fun logOut() {
        dataLoading.set(true)
        sessionController.logOut()
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
        subscriptionState.set(getSubscriptionState())
        subscriptionPlan.set(getSubscriptionPlan())
    }

    fun isAccountStandard(): Boolean {
        return accountType.get()?.equals("IVPN Standard") ?: false
    }

    fun isAccountNewStyle(): Boolean {
        return paymentMethod?.let {
            it == "prepaid"
        } ?: false
    }

    private fun clearLocalCache() {
        authenticated.set(false)
        navigator?.onLogOut()
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
                textIsExpiredIn.set("Subscription will expire in ${DateUtil.formatSubscriptionTimeLeft(expireTime)}")
            }
            else -> {
                isExpired.set(false)
                isExpiredIn.set(false)
            }
        }
    }

    private fun getUsernameValue(): String? {
        return userPreference.userLogin
    }

    private fun getUserAccountType(): String? {
        return userPreference.currentPlan
    }

    private fun isOnFreeTrial(): Boolean {
        return userPreference.isUserOnTrial
    }

    private fun getAvailableUntilValue(): Long {
        return userPreference.availableUntil
    }

    private fun isAuthenticated(): Boolean {
        val token = userPreference.sessionToken
        return token.isNotEmpty()
    }

    private fun isNativeSubscription(): Boolean {
        val paymentMethod = userPreference.paymentMethod
        if (paymentMethod != "ivpnandroidiap") {
            return false
        }

        val purchase: Purchase? = billingManager.purchase
        purchase?.let {
            return it.isAutoRenewing
        } ?: return false
    }

    private fun getSubscriptionState(): SubscriptionState? {
        if (!userPreference.isActive) {
            return SubscriptionState.INACTIVE
        }
        val purchase = billingManager.purchase ?: return SubscriptionState.ACTIVE
        return if (purchase.isAutoRenewing) {
            SubscriptionState.ACTIVE
        } else {
            SubscriptionState.CANCELLED
        }
    }

    private fun getSubscriptionPlan(): String? {
        var plan = userPreference.currentPlan
        if (!userPreference.isActive) {
            plan += " (inactive)"
            return plan
        }
        val purchase = billingManager.purchase
        if (plan == null || purchase == null) {
            return plan
        }
        if (!purchase.isAutoRenewing) {
            plan += " (cancelled)"
        }
        return plan
    }

    private fun getIsActiveValue(): Boolean {
        return userPreference.isActive
    }

    private fun getPaymentMethodValue(): String {
        return userPreference.paymentMethod
    }

    interface AccountNavigator {
        fun onLogOut()
    }
}