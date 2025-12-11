package net.ivpn.core.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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

import androidx.databinding.ObservableField
import net.ivpn.core.common.appicon.AppIconManager
import net.ivpn.core.common.appicon.CustomAppIconData
import net.ivpn.core.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class AppIconViewModel @Inject constructor(
    private val appIconManager: AppIconManager
) {

    val currentIcon = ObservableField<CustomAppIconData>()
    
    val ivpnIcons: List<CustomAppIconData> = CustomAppIconData.entries
        .filter { it.category == CustomAppIconData.IconCategory.IVPN }
    
    val discreetIcons: List<CustomAppIconData> = CustomAppIconData.entries
        .filter { it.category == CustomAppIconData.IconCategory.Discreet }

    fun onResume() {
        currentIcon.set(appIconManager.getCurrentIconData())
    }

    fun selectIcon(icon: CustomAppIconData) {
        if (currentIcon.get() != icon) {
            appIconManager.setNewAppIcon(icon)
            currentIcon.set(icon)
        }
    }

    fun isSelected(icon: CustomAppIconData): Boolean {
        return currentIcon.get() == icon
    }
}

