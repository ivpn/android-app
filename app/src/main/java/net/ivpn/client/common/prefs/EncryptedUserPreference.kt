package net.ivpn.client.common.prefs

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

import android.content.SharedPreferences
import net.ivpn.client.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class EncryptedUserPreference @Inject constructor(val preference: Preference) {

    companion object {
        private const val IS_MIGRATED = "IS_MIGRATED"

        private const val USER_LOGIN = "USER_LOGIN"
        private const val USER_TRIAL = "USER_TRIAL"
        private const val USER_AVAILABLE_UNTIL = "USER_AVAILABLE_UNTIL"
        private const val USER_BETA_PRIVATE_EMAIL = "USER_BETA_PRIVATE_EMAIL"
        private const val USER_MULTI_HOP = "USER_MULTI_HOP"
        private const val PAYMENT_METHOD = "PAYMENT_METHOD"
        private const val CURRENT_PLAN = "CURRENT_PLAN"
        private const val IS_ACTIVE = "IS_ACTIVE"

        private const val SESSION_TOKEN = "SESSION_TOKEN"
        private const val SESSION_VPN_USERNAME = "SESSION_VPN_USERNAME"
        private const val SESSION_VPN_PASSWORD = "SESSION_VPN_PASSWORD"

        private const val BLANK_USERNAME = "BLANK_USERNAME"
        private const val BLANK_USERNAME_GENERATED_DATE = "BLANK_USERNAME_GENERATED_DATE"
    }

    private val sharedPreferences: SharedPreferences = preference.accountPreference

    init {
        migrate()
    }

    fun putSessionToken(sessionToken: String?) {
        sharedPreferences.edit()
                .putString(SESSION_TOKEN, sessionToken)
                .apply()
    }

    fun putSessionUsername(sessionVpnUsername: String?) {
        sharedPreferences.edit()
                .putString(SESSION_VPN_USERNAME, sessionVpnUsername)
                .apply()
    }

    fun putBlankUsername(blankUsername: String?) {
        sharedPreferences.edit()
                .putString(BLANK_USERNAME, blankUsername)
                .apply()
    }

    fun putBlankUsernameGenerationDate(timestamp: Long) {
        sharedPreferences.edit()
                .putLong(BLANK_USERNAME_GENERATED_DATE, timestamp)
                .apply()
    }

    fun putSessionPassword(sessionVpnPassword: String?) {
        sharedPreferences.edit()
                .putString(SESSION_VPN_PASSWORD, sessionVpnPassword)
                .apply()
    }

    fun putCapabilityMultiHop(isAvailable: Boolean) {
        sharedPreferences.edit()
                .putBoolean(USER_MULTI_HOP, isAvailable)
                .apply()
    }

    fun putPaymentMethod(paymentMethod: String?) {
        sharedPreferences.edit()
                .putString(PAYMENT_METHOD, paymentMethod)
                .apply()
    }

    fun putCurrentPlan(accountType: String?) {
        sharedPreferences.edit()
                .putString(CURRENT_PLAN, accountType)
                .apply()
    }

    fun getCurrentPlan(): String? {
        return sharedPreferences.getString(CURRENT_PLAN, "")
    }

    fun getCapabilityMultiHop(): Boolean {
        return sharedPreferences.getBoolean(USER_MULTI_HOP, false)
    }

    fun getPaymentMethod(): String {
        return sharedPreferences.getString(PAYMENT_METHOD, "") ?: ""
    }

    fun getUserLogin(): String? {
        return sharedPreferences.getString(USER_LOGIN, "")
    }

    fun getIsActive(): Boolean {
        return sharedPreferences.getBoolean(IS_ACTIVE, true)
    }

    fun getSessionVpnUsername(): String? {
        return sharedPreferences.getString(SESSION_VPN_USERNAME, "")
    }

    fun getBlankUsername(): String? {
        return sharedPreferences.getString(BLANK_USERNAME, "")
    }

    fun getBlankUsernameGeneratedDate(): Long {
        return sharedPreferences.getLong(BLANK_USERNAME_GENERATED_DATE, 0)
    }

    fun getSessionVpnPassword(): String? {
        return sharedPreferences.getString(SESSION_VPN_PASSWORD, "")
    }

    fun getSessionToken(): String {
        return sharedPreferences.getString(SESSION_TOKEN, "") ?: ""
    }

    fun isUserOnTrial(): Boolean {
        return sharedPreferences.getBoolean(USER_TRIAL, false)
    }

    fun getAvailableUntil(): Long {
        return sharedPreferences.getLong(USER_AVAILABLE_UNTIL, 0)
    }

    fun putUserLogin(login: String?) {
        sharedPreferences.edit()
                .putString(USER_LOGIN, login)
                .apply()
    }

    fun putIsUserOnTrial(isOnTrial: Boolean) {
        sharedPreferences.edit()
                .putBoolean(USER_TRIAL, isOnTrial)
                .apply()
    }

    fun putAvailableUntil(availableUntil: Long) {
        sharedPreferences.edit()
                .putLong(USER_AVAILABLE_UNTIL, availableUntil)
                .apply()
    }

    fun putIsUserOnPrivateEmailBeta(isOnPrivateEmailBeta: Boolean) {
        sharedPreferences.edit()
                .putBoolean(USER_BETA_PRIVATE_EMAIL, isOnPrivateEmailBeta)
                .apply()
    }

    fun putIsActive(isActive: Boolean) {
        sharedPreferences.edit()
                .putBoolean(IS_ACTIVE, isActive)
                .apply()
    }

    private fun putIsMigrated(isMigrated: Boolean) {
        sharedPreferences.edit()
                .putBoolean(IS_MIGRATED, isMigrated)
                .apply()
    }

    private fun isMigrated(): Boolean {
        return sharedPreferences.getBoolean(IS_MIGRATED, false)
    }

    private fun migrate() {
        if (isMigrated()) return

        val oldPreference = preference.oldAccountSharedPreferences

        if (oldPreference.all.isEmpty()) {
            putIsMigrated(true)
            return
        }

        if (oldPreference.contains(USER_LOGIN)) {
            putUserLogin(oldPreference.getString(USER_LOGIN, ""))
        }
        if (oldPreference.contains(USER_TRIAL)) {
            putIsUserOnTrial(oldPreference.getBoolean(USER_TRIAL, false))
        }
        if (oldPreference.contains(USER_MULTI_HOP)) {
            putCapabilityMultiHop(oldPreference.getBoolean(USER_MULTI_HOP, false))
        }
        if (oldPreference.contains(PAYMENT_METHOD)) {
            putPaymentMethod(oldPreference.getString(PAYMENT_METHOD, ""))
        }
        if (oldPreference.contains(CURRENT_PLAN)) {
            putCurrentPlan(oldPreference.getString(CURRENT_PLAN, ""))
        }
        if (oldPreference.contains(IS_ACTIVE)) {
            putIsActive(oldPreference.getBoolean(IS_ACTIVE, false))
        }
        if (oldPreference.contains(SESSION_TOKEN)) {
            putSessionToken(oldPreference.getString(SESSION_TOKEN, ""))
        }
        if (oldPreference.contains(SESSION_VPN_USERNAME)) {
            putSessionUsername(oldPreference.getString(SESSION_VPN_USERNAME, ""))
        }
        if (oldPreference.contains(SESSION_VPN_PASSWORD)) {
            putSessionPassword(oldPreference.getString(SESSION_VPN_PASSWORD, ""))
        }
        if (oldPreference.contains(BLANK_USERNAME)) {
            putBlankUsername(oldPreference.getString(BLANK_USERNAME, ""))
        }
        if (oldPreference.contains(BLANK_USERNAME_GENERATED_DATE)) {
            putBlankUsernameGenerationDate(oldPreference.getLong(BLANK_USERNAME_GENERATED_DATE, 0L))
        }

        oldPreference.edit().clear().apply()

        putIsMigrated(true)
    }
}