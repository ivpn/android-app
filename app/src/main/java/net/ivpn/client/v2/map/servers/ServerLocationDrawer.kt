package net.ivpn.client.v2.map.servers

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
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
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.map.servers.model.ServerLocationsData

class ServerLocationDrawer(resources: Resources) {
    private var serverPointPaint = Paint()
    private var serversPaint = TextPaint()
    private var serversPaintStroke = TextPaint()

    val tapRadius = resources.getDimension(R.dimen.server_tap_radius)

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
                location.pointRect?.let { pointRectObj ->
                    canvas.drawCircle(
                            (pointRectObj.exactCenterX() - data.left),
                            (pointRectObj.exactCenterY() - data.top), pointRectObj.width() / 2f, serverPointPaint
                    )
                }
                serversPaint.getTextBounds(location.city, 0, location.city.length, bounds)
                location.labelRect?.let {labelRectObj ->
                    canvas.drawText(
                            location.city, (labelRectObj.left - data.left),
                            (labelRectObj.bottom - data.top), serversPaintStroke
                    )
                    canvas.drawText(
                            location.city, (labelRectObj.left - data.left),
                            (labelRectObj.bottom - data.top), serversPaint
                    )
                }
            }
        }
    }
}