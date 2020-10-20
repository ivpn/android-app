package net.ivpn.client.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.R
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.multihop.MultiHopController
import javax.inject.Inject

@ApplicationScope
class MultiHopViewModel @Inject constructor(
        private val multiHopController: MultiHopController) : ViewModel() {

    val isEnabled = ObservableBoolean()
    val isSupported = ObservableBoolean()

    var multiHopTouchListener = OnTouchListener { _, motionEvent ->
        val state = multiHopController.getState()
        if (state == MultiHopController.State.ENABLED) {
            return@OnTouchListener false
        }

        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            applyActionFor(state)
        }

        return@OnTouchListener true
    }
    var enableMultiHopListener = CompoundButton.OnCheckedChangeListener {
        _: CompoundButton?, value: Boolean -> enableMultiHop(value)
    }

    var navigator: MultiHopNavigator? = null

    init {
    }

    fun onResume() {
        isEnabled.set(multiHopController.getIsEnabled())
        isSupported.set(multiHopController.isSupportedByPlan())
    }

    fun reset() {
        isEnabled.set(multiHopController.getIsEnabled())
        isSupported.set(multiHopController.isSupportedByPlan())
    }

    fun enableMultiHop(state: Boolean) {
        if (isEnabled.get() == state) return
        when (multiHopController.getState()) {
            MultiHopController.State.NOT_AUTHENTICATED,
            MultiHopController.State.SUBSCRIPTION_NOT_ACTIVE,
            MultiHopController.State.VPN_ACTIVE,
            MultiHopController.State.DISABLED_BY_PROTOCOL -> {
                return
            }
        }

        isEnabled.set(state)

        multiHopController.enable(state)

        navigator?.onMultiHopStateChanged(state)
    }

    private fun applyActionFor(state : MultiHopController.State) {
        when (state) {
            MultiHopController.State.NOT_AUTHENTICATED -> {
                navigator?.authenticate()
            }
            MultiHopController.State.SUBSCRIPTION_NOT_ACTIVE ->  {
                navigator?.subscribe()
            }
            MultiHopController.State.VPN_ACTIVE -> {
                navigator?.notifyUser(R.string.snackbar_to_change_multihop_disconnect_first_msg,
                        R.string.snackbar_disconnect_first)
            }
            MultiHopController.State.DISABLED_BY_PROTOCOL -> {
                navigator?.notifyUser(R.string.snackbar_multihop_not_allowed_for_wg,
                        R.string.snackbar_disconnect_first)
            }
            MultiHopController.State.ENABLED -> {
            }
        }
    }

    interface MultiHopNavigator {
        fun onMultiHopStateChanged(state: Boolean)
        fun subscribe()
        fun authenticate()
        fun notifyUser(msgId : Int, actionId : Int)
    }
}