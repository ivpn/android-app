package net.ivpn.client.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

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
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class BypassVpnViewModel @Inject constructor(
        private val settings: Settings
){
    val isLocalBypassEnabled = ObservableBoolean()

    var enableLocalBypassListener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableLocalBypass(value) }

    init {
        isLocalBypassEnabled.set(settings.isLocalBypassEnabled)
    }

    private fun enableLocalBypass(value: Boolean) {
        isLocalBypassEnabled.set(value)
        settings.enableLocalBypass(value)
    }
}