package net.ivpn.core.v2.map.location

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.graphics.Rect
import net.ivpn.core.v2.map.animation.MapAnimator
import net.ivpn.core.v2.map.model.Location

data class LocationData(
        var isReady: Boolean = false,
        var drawCurrentLocation: Boolean = true,
        var appearProgress: Float = 1f,
        var waveAnimationProgress: Float = 0f,
        var moveAnimationProgress: Float = 0f,
        var hideAnimationProgress: Float = 0f,
        var location: Location? = null,
        var oldLocation: Location? = null,
        var inProgress: Boolean = false,
        var screen: Rect = Rect(),
        var scale: Float = 1f,
        var locationAnimationState: MapAnimator.AnimationState = MapAnimator.AnimationState.NONE
)