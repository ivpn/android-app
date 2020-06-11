package net.ivpn.client.rest.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class ServerLocation(
        @SerializedName("city") var city: String,
        @SerializedName("latitude") var latitude: Double,
        @SerializedName("longitude") var longitude: Double
) {

    var x: Float = 0f
    var y: Float = 0f

    companion object {
        fun stringFrom(locations: List<ServerLocation>): String {
            return Gson().toJson(locations)
        }

        fun stringFrom(locations: ServerLocation): String {
            return Gson().toJson(locations)
        }

        fun from(json: String?): List<ServerLocation>? {
            if (json == null) return null
            val type = object : TypeToken<List<ServerLocation>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun filter(locations: List<ServerLocation>): List<ServerLocation> {
            return locations.filter {
                it.city != "Bratislava" && it.city != "Brussels" && it.city != "New Jersey, NJ"
            }
        }
    }
}