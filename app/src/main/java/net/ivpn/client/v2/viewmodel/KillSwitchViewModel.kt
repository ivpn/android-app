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

import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.vpn.GlobalBehaviorController
import javax.inject.Inject

@ApplicationScope
class KillSwitchViewModel @Inject constructor(
        private val settings: Settings,
        private val userPreference: UserPreference,
        private val buildController: BuildController,
        private val globalBehaviorController: GlobalBehaviorController
) : ViewModel() {

    val isEnabled = ObservableBoolean()
    var enableKillSwitch = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> tryEnable(value) }
    var touchListener = OnTouchListener { view, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            if (!isAuthenticated()) {
                navigator?.authenticate()
            } else if (!isActive()) {
                navigator?.subscribe()
            } else {
                view.performClick()
            }
        }

        true
    }

    var isAdvancedModeSupported: Boolean = buildController.isAdvancedKillSwitchModeSupported

    var navigator: KillSwitchNavigator? = null

    fun onResume() {
        isEnabled.set(isKillSwitchEnabled())
    }

    fun enableAdvancedKillSwitchDialog(value: Boolean) {
        settings.enableAdvancedKillSwitchDialog(value)
    }

    fun enable(value: Boolean) {
        isEnabled.set(value)
        settings.enableKillSwitch(value)
        if (value) {
            globalBehaviorController.enableKillSwitch()
        } else {
            globalBehaviorController.disableKillSwitch()
        }
    }

    fun reset() {
        isEnabled.set(isKillSwitchEnabled())
    }

    private fun tryEnable(value: Boolean) {
        navigator?.tryEnableKillSwitch(value, isAdvancedKillSwitchDialogEnabled())
    }

    private fun isAdvancedKillSwitchDialogEnabled(): Boolean {
        return settings.isAdvancedKillSwitchDialogEnabled
    }

    private fun isKillSwitchEnabled(): Boolean {
        return settings.isKillSwitchEnabled
    }

    private fun isAuthenticated() : Boolean {
        val token: String = userPreference.sessionToken
        return token.isNotEmpty()
    }

    private fun isActive(): Boolean {
        return userPreference.isActive
    }

    interface KillSwitchNavigator {
        fun subscribe()
        fun authenticate()
        fun tryEnableKillSwitch(state: Boolean, advancedKillSwitchState: Boolean)
    }
}