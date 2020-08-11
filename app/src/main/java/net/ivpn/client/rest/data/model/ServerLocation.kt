package net.ivpn.client.rest.data.model

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

//    var comparator = Comparator { location1: ServerLocation, location2: ServerLocation ->
//        location1.distanceToTap.compareTo(location2.distanceToTap)
//    }

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