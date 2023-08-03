package net.ivpn.core.rest.data.model

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 Privatus Limited.

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

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.ivpn.core.common.prefs.EncryptedUserPreference
import net.ivpn.core.common.prefs.Settings

class AntiTracker {

    @SerializedName("Name")
    @Expose
    var name: String = ""

    @SerializedName("Description")
    @Expose
    var description: String = ""

    @SerializedName("Normal")
    @Expose
    var normal: String = ""

    @SerializedName("Hardcore")
    @Expose
    var hardcore: String = ""

    override fun equals(other: Any?): Boolean {
        if (other !is AntiTracker) return false
        return name == other.name && normal == other.normal
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }

    fun toThumbnail(): String {
        return description
    }

    fun getDefaultList(lists: List<AntiTracker>, settings: Settings, userPreference: EncryptedUserPreference): AntiTracker? {
        if (userPreference.getSessionToken().isNotEmpty() || settings.isAntiSurveillanceEnabled || settings.isAntiSurveillanceHardcoreEnabled) {
            val filteredList = lists.filter { it.name == oisdbigList }
            return filteredList.firstOrNull()
        }

        val filteredList = lists.filter { it.name == basicList }
        return filteredList.firstOrNull()
    }

    companion object {

        const val basicList = "Basic"
        const val oisdbigList = "Oisdbig"

        fun from(json: String): AntiTracker {
            return Gson().fromJson(json, AntiTracker::class.java)
        }

    }

}

class AntiTrackerPlus {

    @SerializedName("DnsServers")
    @Expose
    lateinit var list: List<AntiTracker>

}
