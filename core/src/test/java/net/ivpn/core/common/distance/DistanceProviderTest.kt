package net.ivpn.core.common.distance

import org.junit.Assert.*
import org.junit.Test

class DistanceProviderTest {


    companion object {
        const val DISTANCE_DELTA = 1.0f
    }

    @Test
    fun testCorrectResult() {
        val parisCoordinates = Pair(48.85341f, 2.3488f)
        val kyivCoordinates = Pair(50.450001f, 30.523333f)

        val correctDistance = 2023.71f

        assertEquals(
            correctDistance,
            DistanceProvider.getDistanceBetween(parisCoordinates, kyivCoordinates),
            DISTANCE_DELTA
        )
    }

    @Test
    fun testCorrectEdgeCase() {
        val somePointCoordinates = Pair(-90.0f, 180.0f)
        val kyivCoordinates = Pair(50.450001f, 30.523333f)

        val correctDistance = 15616.58f

        assertEquals(
            DistanceProvider.getDistanceBetween(somePointCoordinates, kyivCoordinates),
            correctDistance,
            DISTANCE_DELTA
        )
    }

    @Test
    fun testSameCoordinateCase() {
        val kyivCoordinates = Pair(50.450001f, 30.523333f)

        val correctDistance = 0.0f

        assertEquals(
            DistanceProvider.getDistanceBetween(kyivCoordinates, kyivCoordinates),
            correctDistance,
            DISTANCE_DELTA
        )
    }
}