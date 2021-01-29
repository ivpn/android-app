package net.ivpn.client.common.qr

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