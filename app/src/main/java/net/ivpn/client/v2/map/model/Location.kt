package net.ivpn.client.v2.map.model

data class Location(
        val longitude: Float,
        val latitude: Float,
        var isConnected: Boolean,
        var description: String?,
        var countryCode: String?) {
    var coordinate: Pair<Float, Float>? = null
}