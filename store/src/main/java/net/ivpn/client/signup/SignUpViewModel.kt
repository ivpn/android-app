package net.ivpn.client.signup

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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.databinding.ObservableField
import androidx.navigation.NavController
import com.android.billingclient.api.SkuDetails
import net.ivpn.client.R
import net.ivpn.client.billing.BillingListener
import net.ivpn.client.billing.BillingManagerWrapper
import net.ivpn.client.dagger.BillingScope
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.billing.addfunds.Period
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.common.prefs.EncryptedUserPreference
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.common.utils.DateUtil
import net.ivpn.core.rest.HttpClientFactory
import net.ivpn.core.rest.IVPNApi
import net.ivpn.core.rest.RequestListener
import net.ivpn.core.rest.Responses
import net.ivpn.core.rest.data.addfunds.NewAccountRequestBody
import net.ivpn.core.rest.data.addfunds.NewAccountResponse
import net.ivpn.core.rest.requests.common.Request
import net.ivpn.core.rest.requests.common.RequestWrapper
import net.ivpn.core.v2.signup.SignUpController
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.floor

@BillingScope
class SignUpViewModel @Inject constructor(
        private val billingManager: BillingManagerWrapper,
        private val userPreference: EncryptedUserPreference,
        private val settings: Settings,
        private val serversRepository: ServersRepository,
        private val httpClientFactory: HttpClientFactory
) : BillingListener, SignUpController() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BillingListener::class.java)
    }

    val selectedPeriod = ObservableField<Period?>()
    val selectedPlan = ObservableField<Plan>()
//    val dataLoading = ObservableBoolean()

    val oneWeek = ObservableField<SkuDetails>()
    val oneMonth = ObservableField<SkuDetails>()
    val oneYear = ObservableField<SkuDetails>()
    val twoYear = ObservableField<SkuDetails>()
    val threeYear = ObservableField<SkuDetails>()
    val oneYearDiscount = ObservableField<String>()
    val twoYearDiscount = ObservableField<String>()
    val threeYearDiscount = ObservableField<String>()

    val standardWeek = ObservableField<String>()
    val standardMonth = ObservableField<String>()
    val standardYear = ObservableField<String>()

    val proWeek = ObservableField<String>()
    val proMonth = ObservableField<String>()
    val proYear = ObservableField<String>()

    val activeUntil = ObservableField<String>()

    val blankAccountID = ObservableField<String>()
    var blankAccountGeneratedDate = 0L

    var navigator: SignUpNavigator? = null

    init {
        dataLoading.set(false)
        blankAccountID.set(userPreference.blankUsername)
        blankAccountGeneratedDate = userPreference.blankUsernameGeneratedDate
    }

    fun selectPeriod(period: Period) {
        selectedPeriod.set(period)
        activeUntil.set(getActiveUntilString(period))
    }

    fun initOffers() {
        dataLoading.set(true)
        selectedPeriod.set(null)
        oneWeek.set(null)
        oneMonth.set(null)
        oneYear.set(null)
        twoYear.set(null)
        threeYear.set(null)

        billingManager.setBillingListener(this)
    }

    fun purchase(activity: Activity) {
        getProperSkuDetail()?.let {
            billingManager.setSkuDetails(it)
//            billingManager.setProductName(getProperProductName())
            billingManager.startPurchase(activity)
        }
    }

    fun getPrice(skuDetails: SkuDetails?): String? {
        return skuDetails?.price
    }

    private fun isBlankAccountFresh(): Boolean {
        return System.currentTimeMillis() - blankAccountGeneratedDate < DateUtil.WEEK
    }

    override fun signUp(navController: NavController?) {
        blankAccountID.get()?.let { accountID ->
            if (accountID.isEmpty() || !isBlankAccountFresh()) {
                createNewAccount()
            } else {
                IVPNApplication.moduleNavGraph.startDestination = R.id.signUpAccountCreatedFragment
                navController?.navigate(IVPNApplication.moduleNavGraph.id)
            }
        } ?: kotlin.run {
            createNewAccount()
        }
    }

    override fun signUpWith(navController: NavController?, username: String?) {
        blankAccountID.set(username)

        IVPNApplication.moduleNavGraph.startDestination = R.id.signUpProductFragment
        navController?.navigate(IVPNApplication.moduleNavGraph.id)
    }

    override fun signUpWithInactiveAccount(navController: NavController?,
                                           plan: Plan, isAccountNewStyle: Boolean) {
        if (isAccountNewStyle) {
            blankAccountID.set(null)
            selectedPlan.set(plan)

            IVPNApplication.moduleNavGraph.startDestination = R.id.signUpPeriodFragment2
            navController?.navigate(IVPNApplication.moduleNavGraph.id)
        } else {
            openSite()
        }
    }


    override fun reset() {
        dataLoading.set(false)
        selectedPeriod.set(null)
        oneWeek.set(null)
        oneMonth.set(null)
        oneYear.set(null)
        twoYear.set(null)
        threeYear.set(null)
        blankAccountID.set(null)
        blankAccountGeneratedDate = 0L
    }

    override fun isPurchaseAutoRenewing(): Boolean {
        billingManager.purchase?.let {
            return it.isAutoRenewing
        } ?: return false
    }

    private fun openSite() {
        val url = "https://www.ivpn.net/account/login"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        IVPNApplication.application.startActivity(intent)
    }

    private fun createNewAccount() {
        dataLoading.set(true)
        val requestBody = NewAccountRequestBody("IVPN Standard")
        val request = Request<NewAccountResponse>(settings, httpClientFactory, serversRepository, Request.Duration.LONG, RequestWrapper.IpMode.IPv4)
        request.start({ api: IVPNApi -> api.newAccount(requestBody) }, object : RequestListener<NewAccountResponse> {
            override fun onSuccess(response: NewAccountResponse) {
                dataLoading.set(false)
                if (response.status == Responses.SUCCESS) {
                    userPreference.blankUsername = response.accountId
                    userPreference.blankUsernameGeneratedDate = System.currentTimeMillis()
                    blankAccountGeneratedDate = System.currentTimeMillis()
                    blankAccountID.set(response.accountId)
                    creationNavigator?.onAccountCreationSuccess()
                } else {
                    creationNavigator?.onAccountCreationError()
                }
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("ERROR, throwable = $throwable")
                dataLoading.set(false)
                creationNavigator?.onAccountCreationError()
            }

            override fun onError(error: String) {
                LOGGER.error("ERROR, error = $error")
                dataLoading.set(false)
                creationNavigator?.onAccountCreationError()
            }
        })
    }

    private fun getProperSkuDetail(): SkuDetails? {
        return when(selectedPeriod.get()) {
            Period.ONE_WEEK -> oneWeek.get()
            Period.ONE_MONTH -> oneMonth.get()
            Period.ONE_YEAR -> oneYear.get()
            Period.TWO_YEARS -> twoYear.get()
            Period.THREE_YEARS -> threeYear.get()
            null -> null
        }
    }

    private fun getActiveUntilString(period: Period): String {
        val activeUntil = userPreference.getAvailableUntil() * 1000
        val calendar = Calendar.getInstance()
        if (userPreference.getIsActive() && (activeUntil > System.currentTimeMillis())) {
            calendar.timeInMillis = activeUntil
        }

        when(period) {
            Period.ONE_WEEK -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            Period.ONE_MONTH -> calendar.add(Calendar.MONTH, 1)
            Period.ONE_YEAR -> calendar.add(Calendar.YEAR, 1)
            Period.TWO_YEARS -> calendar.add(Calendar.YEAR, 2)
            Period.THREE_YEARS -> calendar.add(Calendar.YEAR, 3)
        }
        return "(Will be active until ${DateUtil.formatDateTimeNotUnix(calendar.timeInMillis)})"
    }

    private fun getProperProductName(): String? {
        return selectedPlan.get()?.productName
    }

    private fun checkSkuDetails() {
        val skuList = ArrayList<String>()
        for (plan in Plan.values()) {
            for (period in Period.values()) {
                skuList.add(plan.skuPath + period.skuPath)
            }
        }
        billingManager.checkSkuDetails(skuList)
    }

    override fun onInitStateChanged(isInit: Boolean, errorCode: Int) {
        LOGGER.info("Is billing manager init? - $isInit, errorCode = $errorCode")
        if (isInit) {
            checkSkuDetails()
        } else {
            handleError(errorCode)
        }
    }

    override fun onCheckingSkuDetailsSuccess(skuDetailsList: MutableList<SkuDetails>?) {
        LOGGER.info("processSkuDetails")
        dataLoading.set(false)

        skuDetailsList?.let { details ->
            selectedPlan.get()?.let { plan ->
                for (skuDetails in details) {
                    LOGGER.info("Check ${skuDetails.sku}")
                    when (skuDetails.sku) {
                        plan.skuPath + Period.ONE_WEEK.skuPath -> {
                            oneWeek.set(skuDetails)
                        }
                        plan.skuPath + Period.ONE_MONTH.skuPath -> {
                            oneMonth.set(skuDetails)
                        }
                        plan.skuPath + Period.ONE_YEAR.skuPath -> {
                            oneYear.set(skuDetails)
                        }
                        plan.skuPath + Period.TWO_YEARS.skuPath -> {
                            twoYear.set(skuDetails)
                        }
                        plan.skuPath + Period.THREE_YEARS.skuPath -> {
                            threeYear.set(skuDetails)
                        }
                    }
                }

                calculateYearDiscount()
                calculateTwoYearDiscount()
                calculateThreeYearDiscount()

                selectPeriod(Period.ONE_YEAR)
            }

            for (skuDetails in details) {
                when (skuDetails.sku) {
                    Plan.STANDARD.skuPath + Period.ONE_WEEK.skuPath -> {
                        standardWeek.set(getPricePerPeriodString(skuDetails, "Week"))
                    }
                    Plan.STANDARD.skuPath + Period.ONE_MONTH.skuPath -> {
                        standardMonth.set(getPricePerPeriodString(skuDetails, "Month"))
                    }
                    Plan.STANDARD.skuPath + Period.ONE_YEAR.skuPath -> {
                        standardYear.set(getPricePerPeriodString(skuDetails, "Year"))
                    }
                    Plan.PRO.skuPath + Period.ONE_WEEK.skuPath -> {
                        proWeek.set(getPricePerPeriodString(skuDetails, "Week"))
                    }
                    Plan.PRO.skuPath + Period.ONE_MONTH.skuPath -> {
                        proMonth.set(getPricePerPeriodString(skuDetails, "Month"))
                    }
                    Plan.PRO.skuPath + Period.ONE_YEAR.skuPath -> {
                        proYear.set(getPricePerPeriodString(skuDetails, "Year"))
                    }
                }
            }
        }
    }

    private fun getPricePerPeriodString(skuDetails: SkuDetails, period: String): String {
        return "${skuDetails.price} / $period"
    }

    override fun onPurchaseError(errorStatus: Int, errorMessage: String?) {
    }

    override fun onPurchaseAlreadyDone() {
    }

    override fun onCreateAccountFinish() {
        blankAccountID.set(null)
        userPreference.blankUsername = null
        navigator?.onCreateAccountFinish()
    }

    override fun onAddFundsFinish() {
        navigator?.onAddFundsFinish()
    }

    override fun onPurchaseStateChanged(state: BillingManagerWrapper.PurchaseState?) {
    }

    private fun handleError(error: Int) {
        dataLoading.set(false)
        navigator?.onGoogleConnectFailure()
    }

    private fun calculateYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var yearPrice: Long = 0
        oneYear.get()?.let {
            yearPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - yearPrice / (monthPrice * 12.0)))
        oneYearDiscount.set("-${discount.toInt()}%")
    }

    private fun calculateTwoYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var twoYearsPrice: Long = 0
        twoYear.get()?.let {
            twoYearsPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - twoYearsPrice / (monthPrice * 24.0)))
        twoYearDiscount.set("-${discount.toInt()}%")
    }

    private fun calculateThreeYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var threeYearsPrice: Long = 0
        threeYear.get()?.let {
            threeYearsPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - threeYearsPrice / (monthPrice * 36.0)))
        threeYearDiscount.set("-${discount.toInt()}%")
    }

    interface SignUpNavigator {
        fun onCreateAccountFinish()

        fun onAddFundsFinish()

        fun onGoogleConnectFailure()
    }
}