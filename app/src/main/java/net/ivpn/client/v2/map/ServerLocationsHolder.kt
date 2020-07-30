package net.ivpn.client.v2.map

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.rest.data.model.ServerLocation
import java.util.*
import kotlin.collections.HashMap

object ServerLocationsHolder {

    private var locations: HashMap<ServerLocation, Rect>? = null
    private var locationTitles: HashMap<ServerLocation, Rect>? = null

    fun fill(serverLocations: ArrayList<ServerLocation>, context: Context) {
        if (locations == null || locationTitles == null) {
            createAndFillRect(serverLocations, context)
        }
    }

    fun createAndFillRect(serverLocations: List<ServerLocation>, context: Context) {
        val textPaint = getServerLocationTextPaint(context.resources)
        val pointRadius = context.resources.getDimension(R.dimen.point_radius)
        val distanceFromDot = context.resources.getDimension(R.dimen.location_distance_from_dot)
        val shift = context.resources.getDimension(R.dimen.location_label_shift)

        val locationDots = HashMap<ServerLocation, Rect>()
        val locationLabels = HashMap<ServerLocation, Rect>()

        Collections.sort(serverLocations, ServerLocation.comparatorByX)

        var locationRect: Rect
        for (location in serverLocations) {
            locationRect = Rect().also {
                it.left = (location.x - pointRadius).toInt()
                it.right = (location.x + pointRadius).toInt()
                it.top = (location.y - pointRadius).toInt()
                it.bottom = (location.y + pointRadius).toInt()
            }
            locationDots[location] = locationRect
            location.pointRect = locationRect
        }

        var labelBound: Rect
        var labelRect: Rect
        var testLabelRect: Rect
        for (location in serverLocations) {
            labelBound = Rect()
            textPaint.getTextBounds(location.city, 0, location.city.length, labelBound)

//          top position, no shift
            labelRect = Rect().also {
                it.left = (location.x - labelBound.width() / 2f).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - distanceFromDot - pointRadius).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(labelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = labelRect
                locationLabels[location] = labelRect
                continue
            }

//          top position, shift right
            testLabelRect = Rect().also {
                it.left = (labelRect.left + shift).toInt()
                it.right = (labelRect.right + shift).toInt()
                it.top = labelRect.top
                it.bottom = labelRect.bottom
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          right position, top shift
            testLabelRect = Rect().also {
                it.left = (location.x + pointRadius + distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f - shift).toInt()
                it.top = (it.bottom - labelBound.height() - shift).toInt()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          right position, no shift
            testLabelRect = Rect().also {
                it.left = (location.x + pointRadius + distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          right position, bottom shift
            testLabelRect = Rect().also {
                it.left = (location.x + pointRadius + distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f + shift).toInt()
                it.top = (it.bottom - labelBound.height() + shift).toInt()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }


//            bottom position, right shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() / 2f + shift).toInt()
                it.right = (it.left + labelBound.width() + shift).toInt()
                it.bottom = (location.y + distanceFromDot + pointRadius + labelBound.height()).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//            bottom position, no shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() / 2f).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y + distanceFromDot + pointRadius + labelBound.height()).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//            bottom position, left shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() / 2f - shift).toInt()
                it.right = (it.left + labelBound.width() - shift).toInt()
                it.bottom = (location.y + distanceFromDot + pointRadius + labelBound.height()).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          left position, bottom shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() - pointRadius - distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f + shift).toInt()
                it.top = (it.bottom - labelBound.height() + shift).toInt()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          left position, no shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() - pointRadius - distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f).toInt()
                it.top = it.bottom - labelBound.height()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//          left position, top shift
            testLabelRect = Rect().also {
                it.left = (location.x - labelBound.width() - pointRadius - distanceFromDot).toInt()
                it.right = it.left + labelBound.width()
                it.bottom = (location.y - labelBound.top / 2f - labelBound.bottom / 2f - shift).toInt()
                it.top = (it.bottom - labelBound.height() - shift).toInt()
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }

//            top position, shift left
            testLabelRect = Rect().also {
                it.left = (labelRect.left - shift).toInt()
                it.right = (labelRect.right - shift).toInt()
                it.top = labelRect.top
                it.bottom = labelRect.bottom
            }
            if (isNotIntersected(testLabelRect, locationDots.values, locationLabels.values)) {
                location.labelRect = testLabelRect
                locationLabels[location] = testLabelRect
                continue
            }
        }
    }

    private fun isNotIntersected(labelRect: Rect, dots: Collection<Rect>, labels: Collection<Rect>): Boolean {
        for (rect in dots) {
            if (Rect.intersects(rect, labelRect)) {
                return false
            }
        }
        for (rect in labels) {
            if (Rect.intersects(rect, labelRect)) {
                return false
            }
        }
        return true
    }

    private fun getServerLocationTextPaint(resources: Resources): TextPaint {
        return TextPaint().also {
            it.isAntiAlias = true
            it.color = ResourcesCompat.getColor(resources, R.color.map_label, null)
            it.textSize = resources.getDimension(R.dimen.location_name)
            it.letterSpacing = 0.03f
            it.typeface = Typeface.DEFAULT_BOLD
        }
    }
}