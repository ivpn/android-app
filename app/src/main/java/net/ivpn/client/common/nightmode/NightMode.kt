package net.ivpn.client.common.nightmode

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

import androidx.appcompat.app.AppCompatDelegate
import net.ivpn.client.R

enum class NightMode(val id: Int, val systemId: Int, val stringId: Int) {
    LIGHT(R.id.light_mode, AppCompatDelegate.MODE_NIGHT_NO, R.string.settings_color_theme_light),
    DARK(R.id.dark_mode, AppCompatDelegate.MODE_NIGHT_YES, R.string.settings_color_theme_dark),
    SYSTEM_DEFAULT(R.id.system_default_mode, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.settings_color_theme_system_default),
    BY_BATTERY_SAVER(R.id.set_by_battery_mode, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.settings_color_theme_system_by_battery);

    companion object {
        fun getById(id: Int) : NightMode {
            for (mode in values()) {
                if (mode.id == id) {
                    return mode
                }
            }

            return LIGHT
        }
    }
}