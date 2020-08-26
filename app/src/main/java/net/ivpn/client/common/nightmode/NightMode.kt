package net.ivpn.client.common.nightmode

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