package net.ivpn.client.v2.map.servers

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.map.servers.model.ServerLocationsData

class ServerLocationDrawer(resources: Resources) {

    private var serverPointPaint = Paint()
    private var serversPaint = TextPaint()
    private var serversPaintStroke = TextPaint()

    private val pointRadius = resources.getDimension(R.dimen.point_radius)

    var serverLocations: List<ServerLocation>? = null

    init {
        with(serverPointPaint) {
            color = ResourcesCompat.getColor(resources, R.color.map_label, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(serversPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.map_label, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            typeface = Typeface.DEFAULT_BOLD
        }

        with(serversPaintStroke) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.map_label_shadow, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            strokeWidth = 4f
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    fun draw(canvas: Canvas, data: ServerLocationsData) {
        val bounds = Rect()

        serverLocations?.let {
            for (location in it) {
                canvas.drawCircle(
                        (location.x - data.left),
                        (location.y - data.top), pointRadius, serverPointPaint
                )

                serversPaint.getTextBounds(location.city, 0, location.city.length, bounds)
                canvas.drawText(
                        location.city, ((location.x - data.left - bounds.width() / 2)),
                        ((location.y - data.top - bounds.height() / 2 - pointRadius)), serversPaintStroke
                )
                canvas.drawText(
                        location.city, ((location.x - data.left - bounds.width() / 2)),
                        ((location.y - data.top - bounds.height() / 2 - pointRadius)), serversPaint
                )
            }
        }
    }
}