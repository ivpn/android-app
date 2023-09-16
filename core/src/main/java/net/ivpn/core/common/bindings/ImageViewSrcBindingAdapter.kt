package net.ivpn.core.common.bindings

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import net.ivpn.core.common.utils.BitmapUtil
import net.ivpn.core.common.utils.BitmapUtil.invertColors
import net.ivpn.core.common.utils.BitmapUtil.toGrayscale
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.splittunneling.items.ApplicationItem
import java.io.File
import java.io.IOException

@BindingAdapter("android:src")
fun setImageUri(view: ImageView, imageUri: String?) {
    if (imageUri == null) {
        view.setImageURI(null)
    } else {
        view.setImageURI(Uri.parse(imageUri))
    }
}

@BindingAdapter("android:src")
fun setImageUri(view: ImageView, imageUri: Uri?) {
    view.setImageURI(imageUri)
}

@BindingAdapter("android:src")
fun setImageDrawable(view: ImageView, drawable: Drawable?) {
    view.setImageDrawable(drawable)
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, server: Server?) {
    if (server == null) return
    val context = imageView.context
    var countryCode = server.countryCode
    if (countryCode.equals("uk", ignoreCase = true)) {
        countryCode = "gb"
    }
    val path = ("flag" + File.separator
            + countryCode.lowercase() + ".png")
    val drawable: Drawable
    try {
        drawable = Drawable.createFromStream(context.assets.open(path), null)!!
        imageView.setImageDrawable(drawable)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

@BindingAdapter("app:src")
fun setImageResource(imageView: ImageView, info: ApplicationItem?) {
    if (info == null) return
    imageView.setImageDrawable(info.icon)
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, countryCode: String?) {
    var countryCode = countryCode ?: return
    val context = imageView.context
    if (countryCode.equals("uk", ignoreCase = true)) {
        countryCode = "gb"
    }
    val path = ("flag" + File.separator
            + countryCode.lowercase() + ".png")
    val drawable: Drawable
    try {
        drawable = Drawable.createFromStream(context.assets.open(path), null)!!
        imageView.setImageDrawable(drawable)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

@BindingAdapter("bitmap")
fun setImageBitmap(imageView: ImageView, bitmap: Bitmap?) {
    if (bitmap == null) {
        return
    }
    imageView.setImageBitmap(bitmap)
}

@BindingAdapter("app:srcBase")
fun setBase64(imageView: ImageView, base64: String) {
    Log.d("TAG", "setBase64: $base64")
    val replaced = base64.replaceFirst("data:image/png;base64,".toRegex(), "")
    Log.d("TAG", "replaced: $replaced")
    val decodedString = Base64.decode(replaced, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    val greyBitmap = toGrayscale(bitmap)
    val finalBitmap = if (BitmapUtil.isUsingNightModeResources(imageView.context)) greyBitmap?.invertColors() else greyBitmap
    imageView.setImageBitmap(finalBitmap)
}