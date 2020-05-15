package net.ivpn.client.v2.map

import android.graphics.Canvas
import android.view.SurfaceHolder

class MapSurfaceThread constructor(
        private val surfaceHolder: SurfaceHolder,
        private val surfaceView: MapSurfaceView
) : Thread() {

    var isRunning = false

    override fun run() {
        while (isRunning) {
            var canvas: Canvas? = null
            try {
                if (surfaceHolder.surface.isValid) {
                    canvas = surfaceHolder.lockCanvas()
                    synchronized(surfaceHolder) {
                        if (canvas != null) {
                            surfaceView.drawOn(canvas)
                        }
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}