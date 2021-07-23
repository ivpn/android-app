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
import net.ivpn.core.vpn.ProtocolController
import net.ivpn.core.vpn.controller.VpnBehaviorController
import javax.inject.Inject

@ApplicationScope
class MultiHopController @Inject constructor(
        val userPreference: EncryptedUserPreference,
        val vpnBehaviorController: VpnBehaviorController,
        val protocolController: ProtocolController,
        val settings: Settings
) {

    var isEnabled: Boolean

    var listeners = ArrayList<OnValueChangeListener>()

    init {
        isEnabled = getIsEnabled()
    }

    fun enable(value : Boolean) {
        if (isEnabled == value) {
            return
        }

        isEnabled = value
        settings.isMultiHopEnabled = value

        notifyValueChanges()
    }

    fun isSupportedByPlan(): Boolean {
        return userPreference.getCapabilityMultiHop() && isAuthenticated() && isMultihopAllowedByProtocol()
    }

    fun getIsEnabled(): Boolean {
        isEnabled = settings.isMultiHopEnabled && isMultihopAllowedByProtocol()
        return isEnabled
    }

    fun getState() : State {
        return if (!isAuthenticated()) {
            State.NOT_AUTHENTICATED
        } else if (!isActive()) {
            State.SUBSCRIPTION_NOT_ACTIVE
        } else if (isVpnActive()) {
            State.VPN_ACTIVE
        } else if (!isMultihopAllowedByProtocol()) {
            State.DISABLED_BY_PROTOCOL
        } else {
            State.ENABLED
        }
    }

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

    private fun isAuthenticated() : Boolean {
        val token: String = userPreference.getSessionToken()
        return token.isNotEmpty()
    }

    private fun isActive(): Boolean {
        return userPreference.getIsActive()
    }

    private fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    private fun isMultihopAllowedByProtocol(): Boolean {
        return protocolController.currentProtocol.isMultihopEnabled
    }

    enum class State {
        ENABLED,
        NOT_AUTHENTICATED,
        SUBSCRIPTION_NOT_ACTIVE,
        VPN_ACTIVE,
        DISABLED_BY_PROTOCOL
    }

    interface OnValueChangeListener {
        fun onValueChange(value: Boolean)
    }
}