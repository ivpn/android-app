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

import android.widget.RadioGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class ColorThemeViewModel @Inject constructor(
        buildController: BuildController,
        private val settings: Settings
) : ViewModel() {

    var themeListener =  RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int -> onCheckedChanged(checkedId)
    }

    val isSystemDefaultNightModeSupported = ObservableBoolean()
    val nightMode = ObservableField<NightMode>()

    private val selectedNightMode = ObservableField<NightMode>()

    var navigator: ColorThemeNavigator? = null

    init {
        isSystemDefaultNightModeSupported.set(buildController.isSystemDefaultNightModeSupported)
    }

    fun onResume() {
        selectedNightMode.set(settings.nightMode)
        nightMode.set(settings.nightMode)
    }

    fun applyMode() {
        nightMode.set(selectedNightMode.get())
        settings.nightMode = nightMode.get()
        navigator?.onNightModeChanged(nightMode.get())
    }

    private fun onCheckedChanged(checkedId: Int) {
        val nightMode = NightMode.getById(checkedId)
        if (nightMode == this.selectedNightMode.get()) {
            return
        }

        this.selectedNightMode.set(nightMode)
    }

    interface ColorThemeNavigator {
        fun onNightModeChanged(mode: NightMode?)
    }

}