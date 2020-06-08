package net.ivpn.client.v2.viewmodel

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableLong
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.Purchase
import net.ivpn.client.IVPNApplication
import net.ivpn.client.common.billing.BillingManagerWrapper
import net.ivpn.client.common.billing.SubscriptionState
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.qr.QRController
import net.ivpn.client.common.session.SessionController
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.data.session.DeleteSessionRequestBody
import net.ivpn.client.rest.data.session.DeleteSessionResponse
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse
import net.ivpn.client.rest.requests.common.Request
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AccountViewModel @Inject constructor(
        private val userPreference: UserPreference,
        private val billingManager: BillingManagerWrapper,
        private val sessionController: SessionController
) : ViewModel(), SessionController.SessionListener {

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

    var navigator: AccountNavigator? = null

    init {
        sessionController.subscribe(this)
    }

    fun onResume() {
        username.set(getUsername())
        accountType.set(getUserAccountType())
        isOnFreeTrial.set(isOnFreeTrial())
        availableUntil.set(getAvailableUntil())
        authenticated.set(isAuthenticated())
        isNativeSubscription.set(isNativeSubscription())
        subscriptionState.set(getSubscriptionState())
        subscriptionPlan.set(getSubscriptionPlan())
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

    override fun onRemoveSuccess() {
        dataLoading.set(false)
        clearLocalCache()
    }

    override fun onRemoveError() {
        dataLoading.set(false)
        clearLocalCache()
    }

    override fun onCreateSuccess(response: SessionNewResponse) {
    }

    override fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
    }

    override fun onUpdateSuccess() {
    }

    override fun onUpdateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
        TODO("Not yet implemented")
    }

    private fun clearLocalCache() {
        authenticated.set(false)
        navigator?.onLogOut()
    }

    private fun getUsername(): String? {
        return userPreference.userLogin
    }

    private fun getUserAccountType(): String? {
        return userPreference.currentPlan
    }

    private fun isOnFreeTrial(): Boolean {
        return userPreference.isUserOnTrial
    }

    private fun getAvailableUntil(): Long {
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

    interface AccountNavigator {
        fun onLogOut()
    }
}