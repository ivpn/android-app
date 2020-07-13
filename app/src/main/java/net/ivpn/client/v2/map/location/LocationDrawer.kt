package net.ivpn.client.v2.map.location

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.MapView

class LocationDrawer(resources: Resources) {

    var pointRadius = resources.getDimension(R.dimen.location_point_radius)
    var locationMaxRadius = resources.getDimension(R.dimen.location_anim_max_radius)
//    var locationRadius = resources.getDimension(R.dimen.location_radius)

    private val pointPaint = Paint()
    private var wavePaint = Paint()

    private val progressColor = ResourcesCompat.getColor(resources, R.color.wave_progress, null)
    private val connectedColor = ResourcesCompat.getColor(resources, R.color.wave_connected, null)
    private val disconnectedColor = ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)

//    var firstWave = true

    init {
        with(pointPaint) {
            color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(wavePaint) {
            color = ResourcesCompat.getColor(resources, R.color.wave_connected, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    fun draw(canvas: Canvas, data: LocationData) {
//        println("Draw location data = ${data.toString()}")
        drawOldLocation(canvas, data)
        drawCurrentLocation(canvas, data)
    }

    private fun drawCurrentLocation(canvas: Canvas, data: LocationData) {
        if (!data.drawCurrentLocation) return

        data.location?.let {
//            drawMultiWaves(canvas, data)
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
            pointPaint.color = color
            pointPaint.alpha = ((MapView.MAX_ALPHA * 2 * data.appearProgress).coerceAtMost(MapView.MAX_ALPHA.toFloat())).toInt()
            val location = it.coordinate ?: return

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    pointRadius,
                    pointPaint
            )

            wavePaint.color = color

            val radius = (locationMaxRadius - pointRadius) * (data.appearProgress) + pointRadius
            wavePaint.alpha = (MapView.MAX_ALPHA * (0.6f)).toInt()
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    radius,
                    wavePaint
            )
        } ?: return
    }

    private fun drawOldLocation(canvas: Canvas, data: LocationData) {
        data.oldLocation?.let {
            if (data.moveAnimationProgress > 0.5f) {
                return
            }

            pointPaint.color = if (it.isConnected) connectedColor else disconnectedColor
            pointPaint.alpha = ((MapView.MAX_ALPHA * (1 - 2 * data.moveAnimationProgress)).toInt())
            val location = it.coordinate ?: return

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    pointRadius,
                    pointPaint
            )

            wavePaint.color = if (it.isConnected) connectedColor else disconnectedColor

            val radius = (locationMaxRadius - pointRadius) * (1 - 2 * data.moveAnimationProgress) + pointRadius
            wavePaint.alpha = ((MapView.MAX_ALPHA * (0.6 - data.moveAnimationProgress)).toInt())
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    radius,
                    wavePaint
            )
        }
    }

    private fun drawMultiWaves(canvas: Canvas, data: LocationData) {
        data.location?.let {
            wavePaint.color = if (it.isConnected) connectedColor else disconnectedColor
            val location = it.coordinate ?: return

            val radius = locationMaxRadius
            wavePaint.alpha = ((MapView.MAX_ALPHA * 0.6f).toInt())
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    radius,
                    wavePaint
            )

        }
    }
}