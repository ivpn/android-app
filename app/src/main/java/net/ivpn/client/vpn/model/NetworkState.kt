package net.ivpn.client.vpn.model

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

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R

enum class NetworkState(val id: Int, val textRes: Int, val backgroundId: Int) {
    TRUSTED(R.id.trusted_state, R.string.network_trusted, R.drawable.trusted_state_background) {
        override fun getColor(): Int {
            return ResourcesCompat.getColor(IVPNApplication.getApplication().resources, R.color.color_trusted_text, null)
        }

    },
    UNTRUSTED(R.id.untrusted_state, R.string.network_untrusted, R.drawable.untrusted_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_untrusted_text)
        }

    },
    NONE(R.id.none_state, R.string.network_state_none, R.drawable.none_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_none_text)
        }

    },
    DEFAULT(R.id.default_state, R.string.network_default, R.drawable.none_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_default_text)
        }
    };

    abstract fun getColor(): Int

    companion object {
        fun getById(id: Int): NetworkState {
            for (mode in values()) {
                if (mode.id == id) {
                    return mode
                }
            }

            return TRUSTED
        }

        val defaultStates: Array<NetworkState>
            get() = arrayOf(TRUSTED, UNTRUSTED, NONE)

        val activeState: Array<NetworkState>
            get() = arrayOf(TRUSTED, UNTRUSTED, DEFAULT)
    }
}