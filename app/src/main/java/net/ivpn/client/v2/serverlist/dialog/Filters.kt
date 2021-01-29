package net.ivpn.client.v2.serverlist.dialog

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

import net.ivpn.client.R
import net.ivpn.client.rest.data.model.Server

enum class Filters(val id: Int) {
    CITY(R.id.city_filter) {
        override fun getServerComparator(): Comparator<Server> {
            return Comparator { server1, server2 -> server1.city.compareTo(server2.city) }
        }
    },
    COUNTRY(R.id.country_filter) {
        override fun getServerComparator(): Comparator<Server> {
            return Comparator { server1, server2 ->
                val compareByCountry = server1.countryCode.compareTo(server2.countryCode)
                if (compareByCountry != 0) {
                    compareByCountry
                } else {
                    server1.city.compareTo(server2.city)
                }
            }
        }
    },
    LATENCY(R.id.latency_filter) {
        override fun getServerComparator(): Comparator<Server> {
            return Comparator { server1, server2 -> server1.latency.compareTo(server2.latency) }
        }
    };

    abstract fun getServerComparator(): Comparator<Server>

    companion object {
        fun getById(id: Int) : Filters {
            for (mode in values()) {
                if (mode.id == id) {
                    return mode
                }
            }

            return COUNTRY
        }
    }
}