package net.ivpn.client.v2.map

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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.collection.LruCache
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.ivpn.client.R
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.v2.map.animation.MapAnimator
import net.ivpn.client.v2.map.dialogue.DialogueDrawer
import net.ivpn.client.v2.map.dialogue.DialogueUtil
import net.ivpn.client.v2.map.dialogue.model.DialogueData
import net.ivpn.client.v2.map.location.LocationData
import net.ivpn.client.v2.map.location.LocationDrawer
import net.ivpn.client.v2.map.model.Location
import net.ivpn.client.v2.map.servers.ServerLocationDrawer
import net.ivpn.client.v2.map.servers.model.ServerLocationsData
import net.ivpn.client.v2.viewmodel.LocationViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

class MapView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {

    private var scroller: Scroller = Scroller(this.context)

    private var panelHeight: Float = 0f

    private val bitmapPaint = Paint()

    private var serverLocationDrawer = ServerLocationDrawer(resources)
    private var serverLocationsData = ServerLocationsData()
    private var nearestServers: ArrayList<ServerLocation>? = null

    private var locationDrawer = LocationDrawer(resources)
    private var locationData = LocationData()

    private var isInit = false

    private val math = MapMath()

    private var location: Location? = null
    private var oldLocation: Location? = null
    private var serverLocations: List<ServerLocation>? = null

    private val animator = MapAnimator(getAnimatorListener())

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            // Aborts any active scroll animations and invalidates.
            scroller.forceFinished(true)
            ViewCompat.postInvalidateOnAnimation(this@MapView)
            return true
        }

        override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
        ): Boolean {
            math.appendX(distanceX)
            math.appendY(distanceY)

            invalidate()
            return true
        }

        override fun onFling(
                e1: MotionEvent, e2: MotionEvent,
                velocityX: Float, velocityY: Float
        ): Boolean {
            fling((-velocityX).toInt(), (-velocityY).toInt())
            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            checkTap(event)
            return super.onSingleTapUp(event)
        }
    }
    private val gestureDetector = GestureDetector(this.context, gestureListener)

    val locationListener = object : LocationViewModel.CheckLocationListener {
        override fun onSuccess(location: Location, connectionState: ConnectionState) {
            println("Location listener on Success connectionState = $connectionState")
            if (connectionState == ConnectionState.NOT_CONNECTED || connectionState == ConnectionState.PAUSED) {
                setLocation(location)
            }
        }

        override fun onError() {
        }
    }

    var mapListener: MapListener? = null

    private var globalPath: String = context.resources.getString(R.string.path_to_tiles)

    init {
        with(bitmapPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    //While fling animation is active this function will be periodically called
    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            math.setX(scroller.currX.toFloat())
            math.setY(scroller.currY.toFloat())

            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun fling(velocityX: Int, velocityY: Int) {
        // Before flinging, aborts the current animation.
        scroller.forceFinished(true)
        // Begins the animation
        scroller.fling(
                // Current scroll position
                math.totalX.toInt(),
                math.totalY.toInt(),
                velocityX,
                velocityY,
                /*
                 * Minimum and maximum scroll positions. The minimum scroll
                 * position is generally zero and the maximum scroll position
                 * is generally the content size less the screen size. So if the
                 * content width is 1000 pixels and the screen width is 200
                 * pixels, the maximum scroll offset should be 800 pixels.
                 */
                0, MapMath.tileWidth * MapMath.tilesCount - width,
                0, MapMath.tileHeight * MapMath.tilesCount - height
        )
        // Invalidates to trigger computeScroll()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInit || location == null) {
            return
        }

        drawMap(canvas)

        with(serverLocationsData) {
            left = math.totalX
            top = math.totalY
        }
        serverLocationDrawer.draw(canvas, serverLocationsData)

        locationData.location = location
        locationData.oldLocation = oldLocation
        with(locationData.screen) {
            left = math.totalX.toInt()
            top = math.totalY.toInt()
            right = (math.totalX + width).toInt()
            bottom = (math.totalY + height).toInt()
        }
        locationData.locationAnimationState = animator.animationState
        locationDrawer.draw(canvas, locationData)
    }

    private fun checkTap(event: MotionEvent) {
        locationData.location?.coordinate?.let {
            val distance: Float = sqrt((it.first - math.totalX - event.x).pow(2)
                    + (it.second - math.totalY - event.y).pow(2))

            if (distance < serverLocationDrawer.tapRadius) {
                animator.centerLocation(math.totalX,
                        math.totalY,
                        DialogueDrawer.DialogState.PROTECTED,
                        MapAnimator.MovementAnimationType.CENTER_LOCATION
                )
                return
            }
        }

        serverLocationDrawer.serverLocations?.let {
            val servers = arrayListOf<ServerLocation>()
            var distance: Float
            for (serverLocation in it) {
                distance = sqrt((serverLocation.x - math.totalX - event.x).pow(2)
                        + (serverLocation.y - math.totalY - event.y).pow(2))

                if (distance < serverLocationDrawer.tapRadius) {
                    servers.add(serverLocation)
                    serverLocation.distanceToTap = distance
                }
            }
            Collections.sort(servers, ServerLocation.tapComparator)
            nearestServers = servers

            nearestServers?.let { nearestServersObj ->
                if (nearestServersObj.isNotEmpty()) {
                    animator.centerGateway(math.totalX,
                            math.totalY,
                            ArrayList(nearestServersObj),
                            DialogueDrawer.DialogState.SERVER_CONNECT
                    )
                    return
                }
            }
        }

        invalidate()
    }

    private fun drawMap(canvas: Canvas) {
        val srcRect = Rect()

        with(srcRect) {
            left = (math.totalX).toInt()
            right = (math.totalX + width).toInt()
            top = (math.totalY).toInt()
            bottom = (math.totalY + height).toInt()
        }

        val intersectionRect = Rect()
        val relativeRect = Rect()

        val fromX: Int = kotlin.math.max(ceil(srcRect.left / MapMath.tileWidth.toFloat()).toInt(), 1)
        val toX: Int = ceil(srcRect.right / MapMath.tileWidth.toFloat()).toInt()

        val fromY: Int = kotlin.math.max(ceil(srcRect.top / MapMath.tileHeight.toFloat()).toInt(), 1)
        val toY: Int = ceil(srcRect.bottom / MapMath.tileHeight.toFloat()).toInt()

        var tileRect: Rect

        for (i in fromX..toX) {
            for (j in fromY..toY) {
                tileRect = Rect(
                        MapMath.tileWidth * (i - 1), MapMath.tileHeight * (j - 1),
                        MapMath.tileWidth * i,
                        MapMath.tileHeight * j
                )

                getBitmap("$globalPath/row-${j}-col-${i}.png")?.also { bitmap ->
                    if (intersectionRect.setIntersect(tileRect, srcRect)) {
                        with(relativeRect) {
                            left = (intersectionRect.left - math.totalX).toInt()
                            right = (intersectionRect.right - math.totalX).toInt()
                            top = (intersectionRect.top - math.totalY).toInt()
                            bottom = (intersectionRect.bottom - math.totalY).toInt()
                        }

                        with(intersectionRect) {
                            left -= tileRect.left
                            left = bitmap.width * left / MapMath.tileWidth
                            right -= tileRect.left
                            right = bitmap.width * right / MapMath.tileWidth
                            top -= tileRect.top
                            top = bitmap.height * top / MapMath.tileHeight
                            bottom -= tileRect.top
                            bottom = bitmap.height * bottom / MapMath.tileHeight
                        }

                        canvas.drawBitmap(bitmap, intersectionRect, relativeRect, bitmapPaint)
                    }
                }
            }
        }
    }

    var connectionState: ConnectionState? = null
    fun setConnectionState(state: ConnectionState?, gateway: Location?) {
        println("Animation Set connection state = ${state}, gateway = ${gateway}")
        if (state == null) {
            return
        }

//        dialogueData.state = DialogueDrawer.DialogState.NONE
        this.connectionState = state
        when (state) {
            ConnectionState.CONNECTED -> {
                locationData.inProgress = false
                if (animator.animationState == MapAnimator.AnimationState.NONE) {
                    animator.startWaveAnimation()
                }
                if (gateway != null) {
                    gateway.isConnected = true
                    setLocation(gateway)
                }
                invalidate()
            }
            ConnectionState.NOT_CONNECTED -> {
                animator.stopWaveAnimation()
                locationData.inProgress = false
                invalidate()
            }
            ConnectionState.CONNECTING -> {
                locationData.inProgress = true
                if (gateway != null) {
                    gateway.isConnected = true
                    setLocation(gateway)
                }
                invalidate()
            }
            ConnectionState.DISCONNECTING -> {
                locationData.inProgress = true
            }
            ConnectionState.PAUSING -> {
            }
            ConnectionState.PAUSED -> {
                animator.stopWaveAnimation()
                locationData.inProgress = false
                invalidate()
            }
        }
    }

    fun setGatewayLocations(serverLocations: List<ServerLocation>?) {
        println("Set servers locations")
        this.serverLocations = serverLocations

        serverLocations?.let {
            var pair: Pair<Float, Float>
            for (location in it) {
                pair = math.getCoordinatesBy(location.longitude.toFloat(), location.latitude.toFloat())
                location.x = pair.first
                location.y = pair.second
            }
            ServerLocationsHolder.createAndFillRect(it, context)
        }
        invalidate()
    }

    fun centerMap() {
        scroller.forceFinished(true)
//        dialogueData.state = DialogueDrawer.DialogState.NONE
        animator.centerLocation(math.totalX,
                math.totalY,
                DialogueDrawer.DialogState.NONE,
                MapAnimator.MovementAnimationType.CENTER_LOCATION
        )
    }

    private fun setLocation(location: Location?) {
        println("Set location as $location")
        println("Previous location is ${this.location}")
        if (this.location == location) {
            return
        }

        if (this.location == null) {
            this.location = location
            location?.let {
                it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)
                math.totalY = (it.coordinate!!.second - height / 2f + panelHeight)
                math.totalX = (it.coordinate!!.first - width / 2f)
                invalidate()
            }
            return
        }

        if (this.location?.isConnected == location?.isConnected) {
            this.location = location
            this.location?.let {
                it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)
            }
            return
        }
        this.oldLocation = this.location
        this.location = location

        if (!isInit) {
            return
        }
        location?.let {
            it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)

            animator.startHideAnimation(math.totalX, math.totalY)
        }
    }

    fun setPanelHeight(padding: Float) {
        math.totalY = math.totalY - (panelHeight - padding / 2f)
        this.panelHeight = padding / 2f

        invalidate()
    }

    private var job: Job? = null
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (width == 0 || height == 0) {
            return
        }

        job = GlobalScope.launch {
            println("Start to init map")
            math.setScreenSize(width.toFloat(), height.toFloat())
            initTiles()
            isInit = true
        }
        job?.invokeOnCompletion {
            MainScope().launch {
                postInit()
            }
        }
    }

    private fun postInit() {
        println("isInit = true")

        serverLocations?.let {
            var pair: Pair<Float, Float>
            for (location in it) {
                pair = math.getCoordinatesBy(location.longitude.toFloat(), location.latitude.toFloat())
                location.x = pair.first
                location.y = pair.second
            }
            ServerLocationsHolder.createAndFillRect(it, context)
        }
        location?.let {
            it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)
            math.totalY = (it.coordinate!!.second - height / 2f + panelHeight)
            math.totalX = (it.coordinate!!.first - width / 2f)
            invalidate()
        }
        serverLocationDrawer.serverLocations = serverLocations
        invalidate()
    }

    var bitmapCache: LruCache<String, Bitmap>? = null
    private var thumbnails: HashMap<String, Bitmap>? = null
    private fun initTiles() {
        val path = resources.getString(R.string.path_to_tiles)
        bitmapCache = MapHolder.getTilesFor(path, context)
        thumbnails = MapHolder.getThumbnails(path, context)
    }

    val tasks = HashMap<String, BitmapWorkerTask>()
    private fun getBitmap(assetPath: String): Bitmap? {
        bitmapCache?.get(assetPath)?.also {
            return it
        } ?: run {
            tasks[assetPath]?.also { taskObject ->
                if (taskObject.status == AsyncTask.Status.FINISHED) {
                    runNewTask(assetPath)
                }
            } ?: run {
                runNewTask(assetPath)
            }
            thumbnails?.get(assetPath)
        }
        return thumbnails?.get(assetPath)
    }

    private fun runNewTask(assetPath: String) {
        val task = BitmapWorkerTask()
        task.execute(assetPath)
        tasks[assetPath] = task
    }

    inner class BitmapWorkerTask : AsyncTask<String, Unit, Bitmap>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            return params[0]?.let { assetPath ->
                getBitmapFrom(context, assetPath).also { bitmap ->
                    bitmapCache?.put(assetPath, bitmap)
                    postInvalidate()
                }
            }
        }
    }

    private fun getBitmapFrom(context: Context, assetPath: String): Bitmap {
        val drawable = Drawable.createFromStream(
                context.assets.open(assetPath), null
        )
        return drawable.toBitmap(MapMath.tileWidth, MapMath.tileHeight, null)
    }

    private fun getAnimatorListener(): MapAnimator.AnimatorListener {
        return object : MapAnimator.AnimatorListener {
            override fun redraw() {
                invalidate()
            }

            override fun onStartMovementAnimation() {
                locationData.drawCurrentLocation = false
            }

            override fun updateHideProgress(progress: Float) {
                locationData.hideAnimationProgress = progress
            }

            override fun updateMovementProgress(progress: Float,
                                                startX: Float,
                                                startY: Float,
                                                animationType: MapAnimator.MovementAnimationType) {
                locationData.moveAnimationProgress = progress
                when (animationType) {
                    MapAnimator.MovementAnimationType.CENTER_LOCATION -> {
                        location?.let {
                            math.totalY = startY + (it.coordinate!!.second - height / 2f - startY + panelHeight) * progress
                            math.totalX = startX + (it.coordinate!!.first - width / 2f - startX) * progress
                            invalidate()
                        }
                    }
                    MapAnimator.MovementAnimationType.CENTER_GATEWAY -> {
                        nearestServers?.let { nearestServersObj ->
                            if (nearestServersObj.isNotEmpty()) {
                                math.totalY = startY + (nearestServersObj.first().y - height / 2f - startY + panelHeight) * progress
                                math.totalX = startX + (nearestServersObj.first().x - width / 2f - startX) * progress
                                invalidate()
                            }
                        }
                    }
                }
            }

            override fun updateCenterGatewayProgress(progress: Float,
                                                     startX: Float,
                                                     startY: Float,
                                                     location: ServerLocation) {
                math.totalY = startY + (location.y - height / 2f - startY + panelHeight) * progress
                math.totalX = startX + (location.x - width / 2f - startX) * progress
                invalidate()
            }

            override fun onEndMovementAnimation() {
                locationData.drawCurrentLocation = true
                oldLocation = null
            }

            override fun updateAppearProgress(progress: Float) {
                locationData.appearProgress = progress
            }

            override fun onEndAppearAnimation() {
                if (connectionState != null && connectionState == ConnectionState.CONNECTED) {
                    animator.startWaveAnimation()
                }
            }

            override fun updateWaveProgress(progress: Float) {
                locationData.waveAnimationProgress = progress
            }

            override fun onCenterAnimationFinish(locations: ArrayList<ServerLocation>?, dialogState: DialogueDrawer.DialogState) {
                when (dialogState) {
                    DialogueDrawer.DialogState.NONE -> {
                        return
                    }
                    DialogueDrawer.DialogState.PROTECTED, DialogueDrawer.DialogState.UNPROTECTED -> {
                        mapListener?.openLocationDialogue(location)
                    }
                    DialogueDrawer.DialogState.SERVER_CONNECT -> {
                        mapListener?.let {
                            locations?.let { locationsObj ->
                                it.openGatewayDialogue(locationsObj)
                            }
                        }

                    }
                }
            }
        }
    }

    interface MapListener {
        fun openLocationDialogue(location: Location?)

        fun openGatewayDialogue(list: ArrayList<ServerLocation>)
    }

    companion object {
        const val WAVE_ANIMATION_DURATION = 2000L
        const val HIDE_ANIMATION_DURATION = 500L
        const val MOVEMENT_ANIMATION_DURATION = 550L
        const val APPEAR_ANIMATION_DURATION = 700L
        const val CENTER_ANIMATION_DURATION = 300L

        const val MAX_ALPHA = 255
    }
}