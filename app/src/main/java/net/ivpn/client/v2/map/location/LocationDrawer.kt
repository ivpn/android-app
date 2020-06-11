package net.ivpn.client.v2.map.location

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.MapView

class LocationDrawer(resources: Resources) {

    var pointRadius = resources.getDimension(R.dimen.point_radius)
    var locationMaxRadius = resources.getDimension(R.dimen.location_anim_max_radius)
//    var locationRadius = resources.getDimension(R.dimen.location_radius)

    private val pointPaint = Paint()
    private var wavePaint = Paint()

    private val connectedColor =  ResourcesCompat.getColor(resources, R.color.wave_connected, null)
    private val disconnectedColor = ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)

    var firstWave = true

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
        if (data.isMoving) return

        data.location?.let {
            drawMultiWaves(canvas, data)

            pointPaint.color = if (it.isConnected) connectedColor else disconnectedColor
            val location = it.coordinate ?: return

            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    pointRadius,
                    pointPaint
            )
        } ?: return
    }

    private fun drawMultiWaves(canvas: Canvas, data: LocationData) {
        var currentProgress = data.progress

        for (i in 1..MapView.WAVES_COUNT) {
            drawWave(canvas, currentProgress, data)
            if (firstWave && (getNextProgress(currentProgress) > currentProgress)) {
                break
            } else {
                currentProgress = getNextProgress(currentProgress)
            }
        }
    }

    private fun getNextProgress(progress: Float): Float {
        val waveStep = 1f / MapView.WAVES_COUNT

        return if (progress - waveStep > 0) (progress - waveStep) else (progress + (1 - waveStep))
    }

    private fun drawWave(canvas: Canvas, progress: Float, data: LocationData) {
        data.location?.let {
            wavePaint.color = if (it.isConnected) connectedColor else disconnectedColor
            val location = it.coordinate ?: return

            val radius = (locationMaxRadius - pointRadius) * progress + pointRadius
            wavePaint.alpha = ((MapView.MAX_ALPHA * (1 - progress)).toInt())
            canvas.drawCircle(
                    location.first - data.screen.left,
                    location.second - data.screen.top,
                    radius,
                    wavePaint
            )

        } ?: return
    }
}