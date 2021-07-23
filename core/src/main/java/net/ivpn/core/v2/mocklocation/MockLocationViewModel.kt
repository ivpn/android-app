package net.ivpn.core.v2.mocklocation

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
import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class MockLocationViewModel @Inject constructor(
        private val controller: MockLocationController
) : ViewModel() {

    var mockLocationListener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> tryEnableMockLocation(value) }

    var navigator: MockLocationNavigator? = null

    fun isDeveloperOptionsEnabled(): Boolean {
        return controller.isDeveloperOptionsEnabled()
    }

    fun isMockLocationEnabled(): Boolean {
        return controller.isMockLocationEnabled()
    }

     fun isMockLocationFeatureEnabled(): Boolean {
         return controller.isMockLocationFeatureEnabled()
     }

    fun enableMockLocation(isEnabled: Boolean) {
        controller.enableMockLocation(isEnabled)
    }

    private fun tryEnableMockLocation(isEnabled: Boolean) {
        if (isEnabled) {
            if (isDeveloperOptionsEnabled() && isMockLocationFeatureEnabled()) {
                enableMockLocation(isEnabled)
            } else {
                navigator?.setupMockLocation()
            }
        } else {
            enableMockLocation(isEnabled)
        }
    }
}