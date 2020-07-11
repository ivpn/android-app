package net.ivpn.client.v2.map.location

import android.graphics.Rect
import net.ivpn.client.v2.map.model.Location

data class LocationData(
        var drawCurrentLocation: Boolean = true,
        var appearProgress: Float = 1f,
        var waveAnimationProgress: Float = 0f,
        var moveAnimationProgress: Float = 0f,
        var location: Location? = null,
        var oldLocation: Location? = null,
        var inProgress: Boolean = false,
        var screen: Rect = Rect()
)