package net.ivpn.client.v2.map

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import net.ivpn.client.v2.map.model.Location
import kotlin.math.ln
import kotlin.math.tan

class MapMath {
    //Coordinates that are used to understand what part on map should be shown.
    @Volatile
    var totalX: Float = 0f

    @Volatile
    var totalY: Float = 0f

    //Final width and height of map's bitmap
    var bitmapWidth: Float = 0f
    var bitmapHeight: Float = 0f

    var screenWidth: Float = 0f
    var screenHeight: Float = 0f

    //(longitude, latitude) latitude from - 90 to 90, longitude from -180 to 180;y from -90 to 90; x from -180 t0 180
    //longitude will transform into x coordinate and latitude into y coordinate
    init {
    }

    fun appendX(distanceX: Float) {
        totalX += distanceX
        validateXCoordinate()
    }

    fun appendY(distanceY: Float) {
        totalY += distanceY
        validateYCoordinate()
    }

    fun setX(distanceX: Float) {
        totalX = distanceX
        validateXCoordinate()
    }

    fun setY(distanceY: Float) {
        totalY = distanceY
        validateYCoordinate()
    }

    fun setScreenSize(screenWidth: Float, screenHeight: Float) {
        println("screenWidth = $screenWidth screenHeight = $screenHeight")
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight

        bitmapWidth = (tileWidth * tilesCount).toFloat()
        bitmapHeight = (tileHeight * tilesCount).toFloat()

        totalX = (bitmapWidth - screenWidth) / 2f
        totalY = (bitmapHeight - screenHeight) / 2f
        println("bitmapWidth = $bitmapWidth bitmapHeight = $bitmapHeight")
    }

    private fun validateXCoordinate() {
        if (totalX < 0f) {
            totalX = 0f
        }
        if (totalX > tileWidth * tilesCount - screenWidth) {
            totalX = tileWidth * tilesCount - screenWidth
        }
    }

    private fun validateYCoordinate() {
        if (totalY < 0f) {
            totalY = 0f
        }
//        if (totalY > tileHeight * tilesCount - screenHeight) {
//            totalY = tileHeight * tilesCount - screenHeight
//        }
        if (totalY > tileHeight * visibleYCount - screenHeight) {
            totalY = tileHeight * visibleYCount - screenHeight
        }
    }

    fun getCoordinatesBy(longitude: Float, latitude: Float): Pair<Float, Float> {
        var x: Double = toRadian(longitude) - 0.18
        var y: Double = toRadian(latitude).toDouble()

        val yStretch = 0.542
        val yOffset = 0.053

        y = yStretch * ln(tan(0.25 * Math.PI + 0.4 * y)) + yOffset

        x = ((bitmapWidth) / 2) + (bitmapWidth / (2 * Math.PI)) * x
        y = (bitmapHeight / 2) - (bitmapHeight / 2) * y

        return Pair(x.toFloat(), y.toFloat())
    }

    private fun toRadian(value: Float): Float {
        return (value * Math.PI / 180.0f).toFloat()
    }

    companion object {
        const val tileHeight = 515
        const val tileWidth = 706

        const val tilesCount = 16
        const val visibleYCount = 14
    }
}