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

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.BuildController
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class AntiTrackerViewModel @Inject constructor(
        private val buildController: BuildController,
        private val settings: Settings
) : ViewModel() {

    val isAntiTrackerSupported = ObservableBoolean()
    val isAntiSurveillanceEnabled = ObservableBoolean()
    val isHardcoreModeEnabled = ObservableBoolean()
    val isHardcoreModeUIEnabled = ObservableBoolean()

    val state = ObservableField<AntiTrackerState>()

    var enableAntiSurveillance = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAntiSurveillance(value) }
    var enableHardcoreMode = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableHardcoreMode(value) }

    init {
        initStates()
    }

    fun reset() {
        initStates()
    }

    private fun initStates() {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())

        getAntiTrackerState()
    }

    private fun getAntiTrackerSupport(): Boolean {
        return IVPNApplication.config.isAntiTrackerSupported
    }

    private fun enableAntiSurveillance(value: Boolean) {
        isAntiSurveillanceEnabled.set(value)
        settings.isAntiSurveillanceEnabled = value
        isHardcoreModeUIEnabled.set(value)
        getAntiTrackerState()
    }

    private fun enableHardcoreMode(value: Boolean) {
        isHardcoreModeEnabled.set(value)
        settings.isAntiSurveillanceHardcoreEnabled = value
        getAntiTrackerState()
    }

    private fun getAntiTrackerState() {
        if (!isAntiSurveillanceEnabled.get()) {
            state.set(AntiTrackerState.DISABLED)
            return
        }

        state.set(if(isHardcoreModeEnabled.get()) AntiTrackerState.HARDCORE else AntiTrackerState.NORMAL)
    }

    enum class AntiTrackerState {
        DISABLED,
        NORMAL,
        HARDCORE
    }
}