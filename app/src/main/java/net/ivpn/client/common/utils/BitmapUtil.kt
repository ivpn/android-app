package net.ivpn.client.common.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.*

object BitmapUtil {

    fun toGrayscale(srcImage: Bitmap): Bitmap? {
        val bmpGrayscale = Bitmap.createBitmap(srcImage.width, srcImage.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(srcImage, 0f, 0f, paint)
        return bmpGrayscale
    }

    fun isUsingNightModeResources(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }

    }

    fun Bitmap.invertColors(): Bitmap? {
        val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        )

        val matrixInvert = ColorMatrix().apply {
            set(
                    floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                    )
            )
        }

        val paint = Paint()
        ColorMatrixColorFilter(matrixInvert).apply {
            paint.colorFilter = this
        }

        Canvas(bitmap).drawBitmap(this, 0f, 0f, paint)
        return bitmap
    }
}