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

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
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

    var enableAntiSurveillance = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAntiSurveillance(value) }
    var enableHardcoreMode = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableHardcoreMode(value) }

    init {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())
    }

    fun reset() {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())
    }

    private fun getAntiTrackerSupport(): Boolean {
        return buildController.isAntiTrackerSupported
    }

    private fun enableAntiSurveillance(value: Boolean) {
        isAntiSurveillanceEnabled.set(value)
        settings.enableAntiSurveillance(value)
        isHardcoreModeUIEnabled.set(value)
        if (!value) {
            isHardcoreModeEnabled.set(false)
            settings.enableAntiSurveillanceHardcore(false)
        }
    }

    private fun enableHardcoreMode(value: Boolean) {
        isHardcoreModeEnabled.set(value)
        settings.enableAntiSurveillanceHardcore(value)
    }
}