package net.ivpn.client.v2.map

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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.core.graphics.drawable.toBitmap
import kotlin.system.measureTimeMillis

object MapHolder {

    //    private var tiles: HashMap<String, Array<Array<Tile>>> = hashMapOf()
    var memoryCache: LruCache<String, Bitmap>? = null

    //    Array<Array<Tile>>
    fun getTilesFor(path: String, context: Context): LruCache<String, Bitmap> {
        memoryCache?.let {
            return it
        }

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        println("Max memory = $maxMemory in Kb")
        val cacheSize = maxMemory / 8
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
//        if (tiles.containsKey(path)) {
//            return tiles[path]!!
//        }
//
//        var bitmaps: Array<Array<Tile>> = arrayOf()
////        val path = context.resources.getString(R.string.path_to_tiles)
//        val executionTime = measureTimeMillis {
//            var array: Array<Tile>
//            for (i in 1..MapMath.tilesCount) {
//                array = arrayOf()
//                for (j in 1..MapMath.tilesCount) {
//                    array += Tile(
//                            Rect(
//                                    MapMath.tileWidth * (i - 1), MapMath.tileHeight * (j - 1),
//                                    MapMath.tileWidth * i,
//                                    MapMath.tileHeight * j
//                            ),
//                            getBitmapFrom(context,"$path/row-${j}-col-${i}.png")
//                    )
//                }
//                bitmaps += array
//            }
//        }
//        println("Bitmap init time = $executionTime")
//        tiles[path] = bitmaps
//        return bitmaps
    }

    private var thumbnailsKey: String? = null
    private var thumbnails: HashMap<String, Bitmap>? = null
    fun getThumbnails(path: String, context: Context): HashMap<String, Bitmap>? {
        if (thumbnailsKey == path && thumbnails != null) {
            return thumbnails
        }
        if (path != thumbnailsKey) {
            thumbnails?.let { recycleThumbnails(it) }
        }

        thumbnailsKey = path

        var thumbnailsImpl = HashMap<String, Bitmap>()
        val executionTime = measureTimeMillis {
            for (i in 1..MapMath.tilesCount) {
                for (j in 1..MapMath.tilesCount) {
                    thumbnailsImpl["$path/row-${j}-col-${i}.png"] = getBitmapFrom(context,"$path/row-${j}-col-${i}.png")
                }
            }
        }
        println("Bitmap init time = $executionTime")
        thumbnails = thumbnailsImpl
        return thumbnailsImpl
    }

    private fun recycleThumbnails(thumbnails: HashMap<String, Bitmap>) {
        for (bitmap in thumbnails.values) {
            bitmap.recycle()
        }
    }

    private fun getBitmapFrom(context: Context, assetPath: String): Bitmap {
        val drawable = Drawable.createFromStream(
                context.assets.open(assetPath), null
        )

//        val bitmap = BitmapFactory.decodeStream(context.assets.open(assetPath))
//
//        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, MapMath.tileWidth / 4,
//                MapMath.tileHeight / 4, true)
//        bitmap.recycle()
//
//        return scaledBitmap
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            drawable.toBitmap(MapMath.tileWidth, MapMath.tileHeight, Bitmap.Config.HARDWARE)
          return drawable.toBitmap(MapMath.tileWidth / 4, MapMath.tileHeight / 4, null)
//        } else {
//            drawable.toBitmap(MapMath.tileWidth, MapMath.tileHeight, null)
//        }
//        val options = BitmapFactory.Options()
//        options.inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Bitmap.Config.HARDWARE
//        } else {
//            Bitmap.Config.RGB_565
//        }
//
//        return BitmapFactory.decodeStream(context.assets.open(assetPath), null, options)

//        return Bitmap.createScaledBitmap(
//                BitmapFactory.decodeStream(context.assets.open(assetPath), null, options), MapMath.tileWidth,
//                MapMath.tileHeight, true
//        )
    }
}