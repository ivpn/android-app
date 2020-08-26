package net.ivpn.client.v2.serverlist.dialog

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