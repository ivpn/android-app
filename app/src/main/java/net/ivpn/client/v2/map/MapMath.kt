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

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
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

    var tileWidth: Int = 0
    var tileHeight: Int = 0

    private val minScaleFactor = 1.0f
    private var maxScaleFactor = 5.0f

    var scaleFactor: Float = 1f

    var borderGap: Float = 0f

    //(longitude, latitude) latitude from - 90 to 90, longitude from -180 to 180;y from -90 to 90; x from -180 t0 180
    //longitude will transform into x coordinate and latitude into y coordinate
    init {
        tileWidth = defaultTileWidth
        tileHeight = defaultTileHeight
    }

    fun applyScaleFactor(deltaScaleFactor: Float, xFocus: Float, yFocus: Float): Boolean {
        val newScaleFactor = scaleFactor * deltaScaleFactor

        if (scaleFactor == newScaleFactor) {
            return false
        }

        scaleFactor = max(minScaleFactor, min(newScaleFactor, maxScaleFactor))
//        println("New scale factor = $scaleFactor")

        tileWidth = (defaultTileWidth / scaleFactor).toInt()
        tileHeight = (defaultTileHeight / scaleFactor).toInt()

        val oldXPosition = xFocus + totalX
        val oldYPosition = yFocus + totalY

        val xRelativePosition = oldXPosition / bitmapWidth
        val yRelativePosition = oldYPosition / bitmapHeight

        bitmapWidth = (tileWidth * tilesCount).toFloat()
        bitmapHeight = (tileHeight * tilesCount).toFloat()

        totalX = xRelativePosition * bitmapWidth - xFocus
        totalY = yRelativePosition * bitmapHeight - yFocus
        validateXCoordinate()
        validateYCoordinate()

        return true
    }

    fun appendX(distanceX: Float) {
        totalX += distanceX
//        println("appendX totalX = $totalX distanceX = $distanceX")
        validateXCoordinate(true, distanceX)
//        println("appendX totalX = $totalX distanceX = $distanceX")
//        if (totalX < 0f) {
//            if (distanceX < 0f) {
////                println("Ignoring...")
//                return
//            } else {
//                totalX += distanceX
//                return
//            }
//        }
//
//        if (totalX > tileWidth * tilesCount - screenWidth) {
//            if (distanceX < 0f) {
//                totalX += distanceX
//                return
//            } else {
////                println("Ignoring...")
////                totalX += distanceX
//                return
//            }
////            totalX = (tileWidth * tilesCount - screenWidth)
//        }
//        totalX += distanceX
//        validateXCoordinate(true, distanceX)
//        println("After totalX = $totalX")
    }

    fun appendY(distanceY: Float) {
        totalY += distanceY
//        println("appendY totalY = $totalY distanceY = $distanceY")
        validateYCoordinate(true, distanceY)
//        if (totalY < 0f) {
//            if (distanceY < 0f) {
//                return
//            } else {
//                totalY += distanceY
//                return
//            }
//        }
//
//        if (totalY > tileHeight * visibleYCount - screenHeight) {
//            if (distanceY < 0f) {
//                totalY += distanceY
//                return
//            } else {
////                totalX += distanceX
//                return
//            }
//        }
//        totalY += distanceY
//        validateYCoordinate(true, distanceY)
    }

    fun setX(distanceX: Float) {
        totalX = distanceX
//        println("setX totalX = $totalX")
        validateXCoordinate()
    }

    fun setY(distanceY: Float) {
        totalY = distanceY
//        println("setY totalY = $totalY")

        validateYCoordinate()
    }

    fun setScreenSize(screenWidth: Float, screenHeight: Float) {
//        println("screenWidth = $screenWidth screenHeight = $screenHeight")
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight

        bitmapWidth = (tileWidth * tilesCount).toFloat()
        bitmapHeight = (tileHeight * tilesCount).toFloat()

        totalX = (bitmapWidth - screenWidth) / 2f
        totalY = (bitmapHeight - screenHeight) / 2f

//        maxScaleFactor = bitmapHeight / screenHeight
        maxScaleFactor = (tileHeight * visibleYCount) / screenHeight
//        println("maxScaleFactor = $maxScaleFactor")

        borderGap = min(screenWidth / 2f, screenHeight / 2f)

//        println("bitmapWidth = $bitmapWidth bitmapHeight = $bitmapHeight")
    }

    /* Negative direction == movement to the left
    Positive direction == movement to the right
    Zero direction == ignore this value*/
    private fun validateXCoordinate(onScroll: Boolean = false, direction: Float = 0f) {
        println("Before validation totalX = $totalX onScroll = $onScroll direction")
        if (totalX < -borderGap) {
//            if (onScroll && direction > 0) {
//                return
//            }
            totalX = -borderGap
        }
        if (totalX > tileWidth * tilesCount - screenWidth + borderGap) {
//            if (onScroll && direction < 0) {
//                return
//            }
            totalX = (tileWidth * tilesCount - screenWidth + borderGap)
        }
//        println("After validation totalX = $totalX onScroll = $onScroll direction")
    }

    /* Negative direction == movement to the left
    Positive direction == movement to the right
    Zero direction == ignore this value*/
    private fun validateYCoordinate(onScroll: Boolean = false, direction: Float = 0f) {
//        println("Before validation totalY = $totalY")
        if (totalY < -borderGap) {
//            if (onScroll && direction > 0) {
//                return
//            }
            totalY = -borderGap
        }

        if (totalY > tileHeight * visibleYCount - screenHeight + borderGap) {
//            if (onScroll && direction < 0) {
//                return
//            }
            totalY = tileHeight * visibleYCount - screenHeight + borderGap
        }
//        println("After validation totalY = $totalY")
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
        const val defaultTileHeight = 1031
        const val defaultTileWidth = 1412

        const val tilesCount = 8
        const val visibleYCount = 7
    }
}