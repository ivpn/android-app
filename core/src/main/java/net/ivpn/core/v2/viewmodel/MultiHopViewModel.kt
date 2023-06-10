package net.ivpn.core.v2.viewmodel

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

import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.core.R
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.multihop.MultiHopController
import net.ivpn.core.vpn.controller.VpnBehaviorController
import javax.inject.Inject

@ApplicationScope
class MultiHopViewModel @Inject constructor(
    private val multiHopController: MultiHopController,
    private val vpnBehaviorController: VpnBehaviorController
) : ViewModel() {

    val isEnabled = ObservableBoolean()
    val isSupported = ObservableBoolean()
    val isSameProviderAllowed = ObservableBoolean()

    var multiHopTouchListener = OnTouchListener { _, motionEvent ->
        val state = getState()
        if (state == MultiHopController.State.ENABLED) {
            return@OnTouchListener false
        }

        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            applyActionFor(state)
        }

        return@OnTouchListener true
    }
    var enableMultiHopListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean ->
            enableMultiHop(value)
        }
    var enableMultiHopSameProviderListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean ->
            enableSameProvider(value)
        }

    var navigator: MultiHopNavigator? = null

    fun onResume() {
        isEnabled.set(multiHopController.getIsEnabled())
        isSupported.set(multiHopController.isSupportedByPlan())
        isSameProviderAllowed.set(multiHopController.isSameProviderAllowed)
    }

    fun reset() {
        isEnabled.set(multiHopController.getIsEnabled())
        isSupported.set(multiHopController.isSupportedByPlan())
    }

    fun enableMultiHop(state: Boolean) {
        if (isEnabled.get() == state) return
        applyActionFor(getState())
        when (getState()) {
            MultiHopController.State.NOT_AUTHENTICATED,
            MultiHopController.State.SUBSCRIPTION_NOT_ACTIVE,
            MultiHopController.State.VPN_ACTIVE-> {
                return
            }
            else -> {}
        }

        isEnabled.set(state)
        multiHopController.enable(state)
        navigator?.onMultiHopStateChanged(state)
    }

    fun enableSameProvider(state: Boolean) {
        isSameProviderAllowed.set(state)
        multiHopController.setIsSameProviderAllowed(state)
    }

    private fun applyActionFor(state: MultiHopController.State) {
        when (state) {
            MultiHopController.State.NOT_AUTHENTICATED -> {
                navigator?.authenticate()
            }
            MultiHopController.State.SUBSCRIPTION_NOT_ACTIVE -> {
                navigator?.subscribe()
            }
            MultiHopController.State.VPN_ACTIVE -> {
                navigator?.notifyUser(
                    R.string.snackbar_to_change_multihop_disconnect_first_msg,
                    R.string.snackbar_disconnect_first
                )
            }
            MultiHopController.State.ENABLED -> {
            }
        }
    }

    private fun getState() : MultiHopController.State {
        return if (!multiHopController.isAuthenticated()) {
            MultiHopController.State.NOT_AUTHENTICATED
        } else if (!multiHopController.isActive()) {
            MultiHopController.State.SUBSCRIPTION_NOT_ACTIVE
        } else if (vpnBehaviorController.isVPNActive) {
            MultiHopController.State.VPN_ACTIVE
        } else {
            MultiHopController.State.ENABLED
        }
    }

    interface MultiHopNavigator {
        fun onMultiHopStateChanged(state: Boolean)
        fun subscribe()
        fun authenticate()
        fun notifyUser(msgId: Int, actionId: Int)
    }
}