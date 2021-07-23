package net.ivpn.core.vpn

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

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.Inet6Address
import java.util.*

object NetworkUtils {

    @JvmStatic
    fun getLocalNetworks(context: Context, ipv6: Boolean): Vector<String> {
        val nets = Vector<String>()
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networks = conn.allNetworks
            for (network in networks) {
                val linkProperties = conn.getLinkProperties(network)
                val networkCapabilities = conn.getNetworkCapabilities(network)

                // Skip VPN and Mobile networks
                networkCapabilities?.let {
                    if (!it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                            && !it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        addLinkAddresses(linkProperties, nets, ipv6)
                    }
                }
            }

        return nets
    }

    private fun addLinkAddresses(linkProperties: LinkProperties?, nets: Vector<String>, ipv6: Boolean) {
        linkProperties?.let {
            for (linkAddress in linkProperties.linkAddresses) {
                if (linkAddress.address is Inet4Address && !ipv6 ||
                        linkAddress.address is Inet6Address && ipv6) {
                    nets.add(linkAddress.toString())
                }
            }
        }
    }
}