package net.ivpn.client.v2.map.location

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

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.MapView
import net.ivpn.client.v2.map.animation.MapAnimator
import kotlin.math.max

class LocationDrawer(resources: Resources) {

    companion object {
        const val BACKGROUND_ALPHA = 0.4f
    }

    var pointRadius = resources.getDimension(R.dimen.location_point_radius)
    var locationMaxRadius = resources.getDimension(R.dimen.location_anim_max_radius)

    private val pointPaint = Paint()
    private val pointBackgroundPaint = Paint()
    private var wavePaint = Paint()

    private val progressColor = ResourcesCompat.getColor(resources, R.color.wave_progress, null)
    private val connectedColor = ResourcesCompat.getColor(resources, R.color.wave_connected, null)
    private val disconnectedColor = ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)

    private var locationPaint = TextPaint()
    private var locationPaintStroke = TextPaint()

    init {
        with(pointPaint) {
            color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(pointBackgroundPaint) {
            color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(wavePaint) {
            color = ResourcesCompat.getColor(resources, R.color.wave_connected, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(locationPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            typeface = Typeface.DEFAULT_BOLD
        }

        with(locationPaintStroke) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.map_label_shadow, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            strokeWidth = 4f
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    fun draw(canvas: Canvas, data: LocationData) {
        if (!data.isReady) return

        drawOldLocation(canvas, data)
        drawCurrentLocation(canvas, data)
    }

    private fun drawCurrentLocation(canvas: Canvas, data: LocationData) {
        if (!data.drawCurrentLocation) return
        if (data.locationAnimationState == MapAnimator.AnimationState.HIDE) {
            return
        }

        data.location?.let {
            drawWave(canvas, data)
            val color = when {
                data.inProgress -> {
                    progressColor
                }
                it.isConnected -> {
                    connectedColor
                }
                else -> {
                    disconnectedColor
                }
            }

            val alpha: Int = if (data.locationAnimationState == MapAnimator.AnimationState.APPEAR) (MapView.MAX_ALPHA * data.appearProgress).toInt() else MapView.MAX_ALPHA
            pointPaint.color = color
            pointPaint.alpha = alpha
            pointBackgroundPaint.color = color
            pointBackgroundPaint.alpha = (alpha * BACKGROUND_ALPHA).toInt()

            val location = it.coordinate ?: return
            val scaleFactor = (data.scale - 1) / 2.5f + 1f

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    pointRadius * data.appearProgress / scaleFactor,
                    pointPaint
            )

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    locationMaxRadius * data.appearProgress / scaleFactor,
                    pointBackgroundPaint
            )

            if (!it.isConnected) {
                val bounds = Rect()
                it.city?.let { city ->
                    locationPaint.getTextBounds(city, 0, city.length, bounds)
                    locationPaint.alpha = alpha
                    locationPaintStroke.alpha = alpha
                    canvas.drawText(
                            city, ((location.first - data.screen.left - bounds.width() / 2)),
                            ((location.second - data.screen.top - bounds.height() / 2 - pointRadius)), locationPaintStroke
                    )
                    canvas.drawText(
                            city, ((location.first - data.screen.left - bounds.width() / 2)),
                            ((location.second - data.screen.top - bounds.height() / 2 - pointRadius)), locationPaint
                    )
                }
            }
        } ?: return
    }

    private fun drawOldLocation(canvas: Canvas, data: LocationData) {
        data.oldLocation?.let {
            if (data.locationAnimationState != MapAnimator.AnimationState.HIDE) {
                return
            }

            val color = when {
                data.locationAnimationState == MapAnimator.AnimationState.HIDE && it.isConnected -> {
                    progressColor
                }
                it.isConnected -> {
                    connectedColor
                }
                else -> {
                    disconnectedColor
                }
            }
            val alpha = ((MapView.MAX_ALPHA * (1 - data.hideAnimationProgress)).toInt())
//            pointPaint.color = if (it.isConnected) connectedColor else disconnectedColor
            pointPaint.color = color
            pointPaint.alpha = alpha
            pointBackgroundPaint.color = color
//            pointBackgroundPaint.color = if (it.isConnected) connectedColor else disconnectedColor
            pointBackgroundPaint.alpha = (alpha * BACKGROUND_ALPHA).toInt()

            val location = it.coordinate ?: return

            val scaleFactor = (data.scale - 1) / 2.5f + 1f
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    pointRadius * (1 - data.hideAnimationProgress) / scaleFactor,
                    pointPaint
            )

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    locationMaxRadius * (1 - data.hideAnimationProgress) / scaleFactor,
                    pointBackgroundPaint
            )

            if (!it.isConnected) {
                val bounds = Rect()
                it.city?.let { city ->
                    locationPaint.getTextBounds(city, 0, city.length, bounds)
                    locationPaint.alpha = alpha
                    locationPaintStroke.alpha = alpha
                    canvas.drawText(
                            city, ((location.first - data.screen.left - bounds.width() / 2)),
                            ((location.second - data.screen.top - bounds.height() / 2 - pointRadius)), locationPaintStroke
                    )
                    canvas.drawText(
                            city, ((location.first - data.screen.left - bounds.width() / 2)),
                            ((location.second - data.screen.top - bounds.height() / 2 - pointRadius)), locationPaint
                    )
                }
            }
        }
    }

    private fun drawWave(canvas: Canvas, data: LocationData) {
        data.location?.let {
            if (!it.isConnected || data.inProgress) {
                return
            }
            wavePaint.color = connectedColor
            val location = it.coordinate ?: return

            val scaleFactor = (data.scale - 1) / 2.5f + 1f

            val radius = locationMaxRadius * data.waveAnimationProgress
            wavePaint.alpha = ((MapView.MAX_ALPHA * 0.5f * (1 - data.waveAnimationProgress)).toInt())
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    radius / scaleFactor,
                    wavePaint
            )

        }
    }
}