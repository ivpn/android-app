package net.ivpn.client.rest.data.model

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

import android.graphics.Rect
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.util.*

data class ServerLocation(
        @SerializedName("city") var city: String,
        @SerializedName("countryCode") var countryCode: String,
        @SerializedName("latitude") var latitude: Double,
        @SerializedName("longitude") var longitude: Double
) {

    var x: Float = 0f
    var y: Float = 0f

    var distanceToTap: Float = 0f

    var pointRect: Rect? = null
    var labelRect: Rect? = null

    fun getDescription(): String {
        return "$city, $countryCode"
    }

    companion object {
        var tapComparator = Comparator { location1: ServerLocation, location2: ServerLocation ->
            location1.distanceToTap.compareTo(location2.distanceToTap)
        }

        var comparatorByX = Comparator { location1: ServerLocation, location2: ServerLocation ->
            location1.x.compareTo(location2.x)
        }

        fun stringFrom(locations: List<ServerLocation>): String {
            return Gson().toJson(locations)
        }

        fun from(json: String?): List<ServerLocation>? {
            if (json == null) return null
            val type = object : TypeToken<List<ServerLocation>>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}