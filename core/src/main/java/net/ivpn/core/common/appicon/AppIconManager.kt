package net.ivpn.core.common.appicon

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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.ivpn.core.R
import net.ivpn.core.common.dagger.ApplicationScope
import javax.inject.Inject

private const val ACTIVITY_ALIAS_PREFIX = "net.ivpn.client"

@ApplicationScope
class AppIconManager @Inject constructor(
    private val context: Context
) {

    private var currentIcon: CustomAppIconData? = null

    fun setNewAppIcon(desiredAppIcon: CustomAppIconData) {
        val currentIconData = getCurrentIconData()
        
        // Disable current icon
        context.packageManager.setComponentEnabledSetting(
            currentIconData.getComponentName(context),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        
        // Enable new icon
        context.packageManager.setComponentEnabledSetting(
            desiredAppIcon.getComponentName(context),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        
        currentIcon = desiredAppIcon
    }

    fun getCurrentIconData(): CustomAppIconData {
        currentIcon?.let { return it }
        
        val activeIcon = CustomAppIconData.entries.firstOrNull {
            context.packageManager.getComponentEnabledSetting(it.getComponentName(context)) == 
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        
        currentIcon = activeIcon ?: CustomAppIconData.DEFAULT
        return currentIcon!!
    }
}

enum class CustomAppIconData(
    private val componentName: String,
    @DrawableRes val iconPreviewResId: Int,
    @StringRes val labelResId: Int,
    val category: IconCategory
) {
    DEFAULT(".MainActivity", R.mipmap.ic_launcher, R.string.app_icon_name_default, IconCategory.IVPN),
    WEATHER(".MainActivityWeather", R.mipmap.ic_launcher_weather, R.string.app_icon_name_weather, IconCategory.Discreet),
    NOTES(".MainActivityNotes", R.mipmap.ic_launcher_notes, R.string.app_icon_name_notes, IconCategory.Discreet),
    CALCULATOR(".MainActivityCalculator", R.mipmap.ic_launcher_calculator, R.string.app_icon_name_calculator, IconCategory.Discreet);

    fun getComponentName(context: Context): ComponentName {
        val applicationContext = context.applicationContext
        return ComponentName(applicationContext, ACTIVITY_ALIAS_PREFIX + componentName)
    }

    enum class IconCategory {
        IVPN,
        Discreet
    }
}

