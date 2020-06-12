package net.ivpn.client.common.bindings

import androidx.databinding.BindingAdapter
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.v2.map.MapView
import net.ivpn.client.v2.map.model.Location

@BindingAdapter("app:locations")
fun setServerLocations(map: MapView, locations: List<ServerLocation>?) {
    map.setGatewayLocations(locations)
}

@BindingAdapter(value = ["app:connectionState", "app:gateway"], requireAll = false)
fun setConnectionState(map: MapView, state: ConnectionState?, server: Server?) {
    val location = server?.let { Location(it.longitude.toFloat(), it.latitude.toFloat(), false) }
    map.setConnectionState(state, location)
}