package net.ivpn.client.v2.map.location

import android.graphics.Rect
import net.ivpn.client.v2.map.model.Location

data class LocationData(
        var isMoving: Boolean = false,
        var progress: Float = 0f,
        var location: Location? = null,
        var screen: Rect = Rect()
)