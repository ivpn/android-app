package net.ivpn.core.common.prefs

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

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.vpn.Protocol
import javax.inject.Inject

@ApplicationScope
class StickyPreference @Inject constructor(
        private val preference: Preference
) {

    companion object {
        private const val CURRENT_PROTOCOL = "CURRENT_PROTOCOL"
        private const val SETTINGS_NIGHT_MODE = "NIGHT_MODE"
    }

    var currentProtocol: Protocol?
        get() {
            val sharedPreferences = preference.stickySharedPreferences
            val current = sharedPreferences.getString(CURRENT_PROTOCOL, Protocol.WIREGUARD.name)
            return current?.let {
                Protocol.valueOf(it)
            } ?: Protocol.WIREGUARD
        }
        set(protocol) {
            val sharedPreferences = preference.stickySharedPreferences
            sharedPreferences.edit()
                    .putString(CURRENT_PROTOCOL, protocol?.name)
                    .apply()
        }

    val isProtocolSelected: Boolean
        get() {
            val sharedPreferences = preference.stickySharedPreferences
            return sharedPreferences.contains(CURRENT_PROTOCOL)
        }

    var nightMode: String?
        get() {
            val sharedPreferences = preference.stickySharedPreferences
            return sharedPreferences.getString(SETTINGS_NIGHT_MODE, null)
        }
        set(mode) {
            val sharedPreferences = preference.stickySharedPreferences
            sharedPreferences.edit()
                    .putString(SETTINGS_NIGHT_MODE, mode)
                    .apply()
        }

    fun partlyReset() {
        currentProtocol = null
    }
}