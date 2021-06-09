package net.ivpn.client.v2.viewmodel

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.vpn.Protocol
import net.ivpn.client.vpn.ProtocolController
import javax.inject.Inject

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

@ApplicationScope
class IPv6ViewModel @Inject constructor(
        private val settings: Settings,
        protocolController: ProtocolController
){
    val isIPv6Enabled = ObservableBoolean()
    val isIPv6Supported = ObservableBoolean()
    val isAllServerShown = ObservableBoolean()
    val isIPv6BadgeEnabled = ObservableBoolean()

    var enableIPv6Listener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableIPv6(value) }
    var enableAllServerShownOptionListener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAllServerShownOption(value) }

    init {
        isIPv6Supported.set(protocolController.currentProtocol == Protocol.WIREGUARD)
        isIPv6Enabled.set(settings.ipv6Setting)
        isAllServerShown.set(settings.showAllServersSetting)
        isIPv6BadgeEnabled.set(isIPv6Enabled.get() && isAllServerShown.get())
    }

    private fun enableIPv6(value: Boolean) {
        isIPv6Enabled.set(value)
        settings.ipv6Setting = value
        isIPv6BadgeEnabled.set(isIPv6Enabled.get() && isAllServerShown.get())
    }

    private fun enableAllServerShownOption(value: Boolean) {
        isAllServerShown.set(value)
        settings.showAllServersSetting = value
        isIPv6BadgeEnabled.set(isIPv6Enabled.get() && isAllServerShown.get())
    }

}