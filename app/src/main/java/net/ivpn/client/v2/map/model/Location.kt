package net.ivpn.client.v2.map.model

data class Location(val longitude: Float, val latitude: Float, val isConnected: Boolean) {
    var coordinate: Pair<Float, Float>? = null
}