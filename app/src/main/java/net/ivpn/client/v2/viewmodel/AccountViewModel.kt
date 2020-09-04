package net.ivpn.client.v2.viewmodel

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
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
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
        isActive.set(isActive())
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

    fun isAccountPro(): Boolean {
        return accountType.get()?.equals("IVPN Pro") ?: false
    }

    fun isAccountNewStyle(): Boolean {
        return username.get()?.startsWith("i-") ?: false
    }

    private fun clearLocalCache() {
        authenticated.set(false)
        navigator?.onLogOut()
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

    private fun isActive(): Boolean {
        return userPreference.isActive
    }

    interface AccountNavigator {
        fun onLogOut()
    }
}