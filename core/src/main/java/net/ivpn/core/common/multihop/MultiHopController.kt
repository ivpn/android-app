package net.ivpn.core.common.multihop

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

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.EncryptedUserPreference
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.vpn.controller.VpnBehaviorController
import javax.inject.Inject

@ApplicationScope
class MultiHopController @Inject constructor(
        val userPreference: EncryptedUserPreference,
        val settings: Settings
) {

    var isEnabled: Boolean
    var isSameProviderAllowed: Boolean

    var listeners = ArrayList<OnValueChangeListener>()

    init {
        isEnabled = getIsEnabled()
        isSameProviderAllowed = getIsSameProviderAllowed()
    }

    fun enable(value : Boolean) {
        if (isEnabled == value) {
            return
        }

        isEnabled = value
        settings.isMultiHopEnabled = value

        notifyValueChanges()
    }

    fun setIsSameProviderAllowed(value : Boolean) {
        if (isSameProviderAllowed == value) {
            return
        }
        isSameProviderAllowed = value

        settings.isMultiHopSameProviderAllowed = value
    }

    fun isSupportedByPlan(): Boolean {
        return userPreference.getCapabilityMultiHop() && isAuthenticated()
    }

    fun getIsEnabled(): Boolean {
        isEnabled = settings.isMultiHopEnabled
        return isEnabled
    }

    fun isReadyToUse(): Boolean {
        return isEnabled && isSupportedByPlan()
    }

    fun getIsSameProviderAllowed(): Boolean {
        return settings.isMultiHopSameProviderAllowed
    }

//    fun getState() : State {
//        return if (!isAuthenticated()) {
//            State.NOT_AUTHENTICATED
//        } else if (!isActive()) {
//            State.SUBSCRIPTION_NOT_ACTIVE
//        } else if (isVpnActive()) {
//            State.VPN_ACTIVE
//        } else {
//            State.ENABLED
//        }
//    }

    fun addListener(listener : OnValueChangeListener) {
        listeners.add(listener)
        listener.onValueChange(isEnabled)
    }

    fun removeListener(listener : OnValueChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyValueChanges() {
        for (listener in listeners) {
            listener.onValueChange(isEnabled)
        }
    }

    fun isAuthenticated() : Boolean {
        val token: String = userPreference.getSessionToken()
        return token.isNotEmpty()
    }

    fun isActive(): Boolean {
        return userPreference.getIsActive()
    }

    enum class State {
        ENABLED,
        NOT_AUTHENTICATED,
        SUBSCRIPTION_NOT_ACTIVE,
        VPN_ACTIVE
    }

    interface OnValueChangeListener {
        fun onValueChange(value: Boolean)
    }
}