package net.ivpn.client.v2.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import net.ivpn.client.R
import net.ivpn.client.v2.map.model.Tile
import kotlin.system.measureTimeMillis

object MapHolder {

    private var tiles: HashMap<String, Array<Array<Tile>>> = hashMapOf()

    fun getTilesFor(path: String, context: Context): Array<Array<Tile>> {
        if (tiles.containsKey(path)) {
            return tiles[path]!!
        }

        var bitmaps: Array<Array<Tile>> = arrayOf()
//        val path = context.resources.getString(R.string.path_to_tiles)
        val executionTime = measureTimeMillis {
            var array: Array<Tile>
            for (i in 1..MapMath.tilesCount) {
                array = arrayOf()
                for (j in 1..MapMath.tilesCount) {
                    array += Tile(
                            Rect(
                                    MapMath.tileWidth * (i - 1), MapMath.tileHeight * (j - 1),
                                    MapMath.tileWidth * i,
                                    MapMath.tileHeight * j
                            ),
                            getBitmapFrom(context,"$path/row-${j}-col-${i}.png")
                    )
                }
                bitmaps += array
            }
        }
        println("Bitmap init time = $executionTime")
        tiles[path] = bitmaps
        return bitmaps
    }

    private fun getBitmapFrom(context: Context, assetPath: String): Bitmap {
        val drawable = Drawable.createFromStream(
                context.assets.open(assetPath), null
        )

        return Bitmap.createScaledBitmap(
                (drawable as BitmapDrawable).bitmap, MapMath.tileWidth,
                MapMath.tileHeight, false
        )
    }
}