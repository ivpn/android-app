package net.ivpn.client.common.bindings

import androidx.databinding.BindingAdapter
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.v2.map.MapView
import net.ivpn.client.v2.map.model.Location

@BindingAdapter("locations")
fun setServerLocations(map: MapView, locations: List<ServerLocation>?) {
    map.setGatewayLocations(locations)
}

@BindingAdapter(value = ["connectionState", "gateway"], requireAll = false)
fun setConnectionState(map: MapView, state: ConnectionState?, server: Server?) {
    val location = server?.let {
        Location(it.longitude.toFloat(),
                it.latitude.toFloat(),
                false,
                it.city,
                it.country,
                it.countryCode)
    }
    map.setConnectionState(state, location)
}