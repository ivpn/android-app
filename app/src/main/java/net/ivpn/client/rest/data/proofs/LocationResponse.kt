package net.ivpn.client.rest.data.proofs

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("ip_address") val ipAddress : String?,
    @SerializedName("isp") val isp : String?,
    @SerializedName("organization") val organization : String?,
    @SerializedName("country") val country : String?,
    @SerializedName("country_code") val countryCode : String?,
    @SerializedName("city") val city : String?,
    @SerializedName("latitude") val latitude : Double,
    @SerializedName("longitude") val longitude : Double,
    @SerializedName("isIvpnServer") val isIvpnServer : Boolean
) {
    fun getLocation(): String {
        return "$city, $countryCode"
    }
}