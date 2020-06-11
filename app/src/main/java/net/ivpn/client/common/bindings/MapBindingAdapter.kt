package net.ivpn.client.common.bindings

import androidx.databinding.BindingAdapter
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.map.MapView

@BindingAdapter("app:locations")
fun setServerLocations(map: MapView, locations: List<ServerLocation>?) {
    map.setServerLocation(locations)
}