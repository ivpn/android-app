package net.ivpn.core.v2.map

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

import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import kotlin.system.measureTimeMillis

object MapHolder {

    var memoryCache: LruCache<String, Bitmap>? = null

    fun getTilesFor(): LruCache<String, Bitmap> {
        memoryCache?.let {
            it.evictAll()
            return it
        }

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        println("Max memory = $maxMemory in Kb")
        val cacheSize = maxMemory / 4
        println("Cache size = $cacheSize in Kb")

        val memoryCacheImpl = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }

            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                super.entryRemoved(evicted, key, oldValue, newValue)
                oldValue.recycle()
            }
        }
        memoryCache = memoryCacheImpl

        return memoryCacheImpl
    }

    private var thumbnailsKey: String? = null
    private var thumbnails: HashMap<String, Bitmap?>? = null
    fun getThumbnails(path: String, context: Context): HashMap<String, Bitmap?>? {
        if (thumbnailsKey == path && thumbnails != null) {
            return thumbnails
        }
        if (path != thumbnailsKey) {
            thumbnails?.let { recycleThumbnails(it) }
        }

        thumbnailsKey = path

        val thumbnailsImpl = HashMap<String, Bitmap?>()
        val executionTime = measureTimeMillis {
            for (i in 1..MapMath.tilesCount) {
                for (j in 1..MapMath.visibleYCount) {
                    thumbnailsImpl["ic_row_${j}_col_${i}"] = getBitmapFrom(context, "ic_row_${j}_col_${i}")
                }
            }
        }
        println("Bitmap init time = $executionTime")
        thumbnails = thumbnailsImpl
        return thumbnailsImpl
    }

    private fun recycleThumbnails(thumbnails: HashMap<String, Bitmap?>) {
        for (bitmap in thumbnails.values) {
            bitmap?.recycle()
        }
    }

    private fun getBitmapFrom(context: Context, name: String): Bitmap? {
        try {
            val drawable = ResourcesCompat.getDrawable(
                    context.resources,
                    getIdentifier(context, name),
                    null
            )

            drawable?.setTint(
                    ResourcesCompat.getColor(
                            context.resources,
                            R.color.map_fill,
                            null
                    )
            )

            return drawable?.toBitmap(MapMath.defaultTileWidth / 4, MapMath.defaultTileHeight / 4, null)
        } catch (e: Exception) {
        }
        return null
    }

    private fun getIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", IVPNApplication.application.packageName)
    }
}