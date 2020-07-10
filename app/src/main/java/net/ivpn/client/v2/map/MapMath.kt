package net.ivpn.client.v2.map

import kotlin.math.ln
import kotlin.math.log
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

    var cities = mutableListOf<City>()

    //(longitude, latitude) latitude from - 90 to 90, longitude from -180 to 180;y from -90 to 90; x from -180 t0 180
    //longitude will transform into x coordinate and latitude into y coordinate
    init {
        cities.add(City("Kyiv", 30.542721f, 50.447731f))
        cities.add(City("Melbourne", 144.96332f, -37.814f))
//        cities.add(City("New York", -74.00597f, 40.71427f))
//        cities.add(City("London", -0.12574f, 51.50853f))
//        cities.add(City("Vienna", 16.37208f, 48.20849f))
//        cities.add(City("Sydney", 151.20732f, -33.86785f))
        cities.add(City("Brussels", 4.34878f, 50.85045f))
//        cities.add(City("Sofia", 23.32415f, 42.69751f))
//        cities.add(City("Franca", -47.40083f, -20.53861f))
//        cities.add(City("Montreal", -73.58781f, 45.50884f))
//        cities.add(City("Tokyo", 139.69171f, 35.6895f))
        cities.add(City("Dubai", 55.17128f, 25.0657f))
        cities.add(City("Cape Town", 18.423300f, -33.918861f))
        cities.add(City("Colombo", 79.861244f, 6.927079f))
        cities.add(City("Durban", 31.049999f, -29.883333f))
        cities.add(City("Maputo", 32.588711f, -25.953724f))
        cities.add(City("Luanda", 13.23432f, -8.83682f))
        cities.add(City("Pointe-Noire", 11.86352f, -4.77609f))
        cities.add(City("Accra", -0.020000f, 5.550000f))
        cities.add(City("Conacry", -13.712222f, 9.509167f))
        cities.add(City("Tangier", -5.79975f, 35.76727f))
//        cities.add(City("Lisbon", -9.142685f, 38.736946f))
        cities.add(City("Havana", -82.366592f, 23.113592f))
        cities.add(City("Paramaribo", -55.199089f, 5.839398f))
        cities.add(City("Recife", -34.900002f, -8.050000f))
        cities.add(City("Salvador", -38.476665f, -12.974722f))
        cities.add(City("Lima", -77.042793f, -12.046374f))
        cities.add(City("Guayaquil", -79.897453f, -2.203816f))
        cities.add(City("Panama City", -79.516670f, 8.983333f))
        cities.add(City("San Francisco", -122.431297f, 37.773972f))
        cities.add(City("Zero", 0f, 0f))
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
        if (totalY > tileHeight * tilesCount - screenHeight) {
            totalY = tileHeight * tilesCount - screenHeight
        }
    }

    fun getCoordinatesBy(longitude: Float, latitude: Float): Pair<Float, Float> {
        var x: Double = toRadian(longitude) - 0.18
        var y: Double = toRadian(latitude).toDouble()

        val yStrech = 0.542
        val yOffset = 0.053

        y = yStrech * ln(tan(0.25 * Math.PI + 0.4 * y)) + yOffset

        x = ((bitmapWidth) / 2) + (bitmapWidth / (2 * Math.PI)) * x
        y = (bitmapHeight / 2) - (bitmapHeight / 2) * y

        return Pair(x.toFloat(), y.toFloat())


//        var x: Float = toRadian(longitude) - 0.18f
//        var y: Float = toRadian(latitude) + 0.124f
//
//        y = (1.25f * ln(tan(0.25f * Math.PI + 0.4f * y))).toFloat()
//
//        x = (((bitmapWidth) / 2f) + (bitmapWidth / (2 * Math.PI)) * x).toFloat()
//        y = ((bitmapHeight / 2f) - (bitmapHeight / (2 * 2.383412543)) * y).toFloat()
//
//        return Pair(x, y)
    }

    private fun toRadian(value: Float): Float {
        return (value * Math.PI / 180.0f).toFloat()
    }

    companion object {
        const val tileHeight = 1031
        const val tileWidth = 1412

        //        const val tileHeight = 574
//        const val tileWidth = 763
        const val tilesCount = 8
    }
}