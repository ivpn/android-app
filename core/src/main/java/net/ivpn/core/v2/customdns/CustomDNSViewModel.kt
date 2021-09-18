package net.ivpn.core.v2.customdns

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

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import net.ivpn.core.common.prefs.Settings
import javax.inject.Inject

class CustomDNSViewModel @Inject internal constructor(private val settings: Settings) {

    val isCustomDNSEnabled = ObservableBoolean()
    val dns = ObservableField<String>()
    val secondaryDns = ObservableField<String>()

    var enableCustomDNS =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean ->
            enableCustomDNS(value)
        }

    init {
        isCustomDNSEnabled.set(settings.isCustomDNSEnabled)
        val customDNS = settings.customDNSValue
        val customSecondaryDNS = settings.customDNSValue
        dns.set(customDNS ?: EMPTY_DNS)
        secondaryDns.set(customSecondaryDNS ?: EMPTY_DNS)
    }

    fun setDnsAs(dns: String?) {
        this.dns.set(dns)
    }

    fun setSecondaryDNSAs(dns: String?) {
        this.secondaryDns.set(dns)
    }

    private fun enableCustomDNS(value: Boolean) {
        settings.isCustomDNSEnabled = value
    }

    companion object {
        private const val EMPTY_DNS = "0.0.0.0"
    }

    enum class DNSType {
        PRIMARY,
        SECONDARY
    }
}