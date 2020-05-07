package net.ivpn.client.common.nightmode

import androidx.appcompat.app.AppCompatDelegate
import net.ivpn.client.R

enum class NightMode(val id: Int, val systemId: Int) {
    LIGHT(R.id.light_mode, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.id.dark_mode, AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM_DEFAULT(R.id.system_default_mode, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    BY_BATTERY_SAVER(R.id.set_by_battery_mode, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

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