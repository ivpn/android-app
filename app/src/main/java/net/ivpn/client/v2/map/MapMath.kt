package net.ivpn.client.v2.map

import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

class MapMath {
    //Coordinates that are used to understand what part on map should be shown.
    var totalX: Float = 0f
    var totalY: Float = 0f

    //Final width and height of map's bitmap
    var bitmapWidth: Float = 0f
    var bitmapHeight: Float = 0f

    var screenWidth: Float = 0f
    var screenHeight: Float = 0f

    //(longitude, latitude) latitude from - 90 to 90, longitude from -180 to 180;y from -90 to 90; x from -180 t0 180
    //longitude will transform into x coordinate and latitude into y coordinate

    fun appendX(distanceX: Float) {
        totalX += distanceX
        validateXCoordinate()
    }

    fun appendY(distanceY: Float) {
        totalY += distanceY
        validateYCoordinate()
    }

    fun setX(distanceX: Float){
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
        if (totalY > tileHeight * tilesCount - screenHeight) {
            totalY = tileHeight * tilesCount - screenHeight
        }
    }

    fun getCoordinatesBy(longitude: Float, latitude: Float) : Pair<Float, Float> {
        var y: Float

        val blackMagicCoef: Float

        //Using this coefficients to compensate for the curvature of the map
        val xMapCoefficient = 0.026f
        val yMapCoefficient = 0.965f

        //Logic to convert longitude, latitude into x, y. It's enough when we will have a accurate map
        var x: Float = ((longitude + 180.0) * (bitmapWidth / 360.0)).toFloat()
        val latRadius: Float = (latitude * Math.PI / 180f).toFloat()
        blackMagicCoef = ln(tan((Math.PI / 4) + (latRadius / 2))).toFloat()
        y = ((bitmapHeight / 2) - (bitmapWidth * blackMagicCoef / (2 * PI))).toFloat()

        //Trying to compensate for the curvature of the map
        x -= bitmapWidth * xMapCoefficient
        if (y < bitmapHeight / 2) {
            y *= yMapCoefficient
        }

        return Pair(x, y)
    }

    companion object {
        const val tileHeight = 615
        const val tileWidth = 818
        const val tilesCount = 14
    }
}