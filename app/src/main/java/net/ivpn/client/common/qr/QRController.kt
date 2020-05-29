package net.ivpn.client.common.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

object QRController {

    private const val DIMENSION = 256

    init {
    }

    //ToDo Refactor it
    fun getQR(value: String?, foregroundColor: Int, backgroundColor: Int, dimension: Int): Bitmap? {
        if (value == null) {
            return null
        }
        val fDimension = if (dimension == 0) DIMENSION else dimension
        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix: BitMatrix
        try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 0
            bitMatrix = multiFormatWriter.encode(value, BarcodeFormat.QR_CODE, fDimension, fDimension, hints)
        } catch (exception: Exception) {
            return null
        }

        val w: Int = bitMatrix.width
        val h: Int = bitMatrix.height
        val pixels = IntArray(w * h)
        for (i in 0 until h) {
            val offset = i * w
            for (j in 0 until w) {
                pixels[offset + j] = if (bitMatrix.get(i, j)) foregroundColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

}