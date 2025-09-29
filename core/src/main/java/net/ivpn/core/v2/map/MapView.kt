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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import androidx.collection.LruCache
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import kotlinx.coroutines.*
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.v2.map.animation.MapAnimator
import net.ivpn.core.v2.map.dialogue.DialogueDrawer
import net.ivpn.core.v2.map.location.LocationData
import net.ivpn.core.v2.map.location.LocationDrawer
import net.ivpn.core.v2.map.model.Location
import net.ivpn.core.v2.map.servers.ServerLocationDrawer
import net.ivpn.core.v2.map.servers.model.ServerLocationsData
import net.ivpn.core.v2.viewmodel.LocationViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*

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
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            math.appendX(distanceX)
            math.appendY(distanceY)

            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
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

    private val scaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            serverLocationsData.isReady = false
            locationData.isReady = false
            detector.let {
                math.applyScaleFactor(it.scaleFactor, it.focusX, it.focusY)
            }

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            updateCoordinates()
            super.onScaleEnd(detector)
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            serverLocationsData.isReady = false
            locationData.isReady = false
            return super.onScaleBegin(detector)
        }
    }
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    val locationListener = object : LocationViewModel.CheckLocationListener {
        override fun onSuccess(location: Location, connectionState: ConnectionState) {
            if (connectionState == ConnectionState.NOT_CONNECTED
                    || connectionState == ConnectionState.PAUSED) {
                setLocation(location)
            }
        }

        override fun onError() {
        }
    }

    var mapListener: MapListener? = null

    init {
        with(bitmapPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        return true
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
                (-math.borderGap).toInt(), math.tileWidth * MapMath.tilesCount - width + (math.borderGap).toInt(),
                (-math.borderGap).toInt(), math.tileHeight * MapMath.tilesCount - height + (math.borderGap).toInt()
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
            scale = math.scaleFactor
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
        locationData.scale = math.scaleFactor
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

        val fromX: Int = max(ceil(srcRect.left / math.tileWidth.toFloat()).toInt(), 1)
        val toX: Int = min(ceil(srcRect.right / math.tileWidth.toFloat()).toInt(), MapMath.tilesCount)

        val fromY: Int = max(ceil(srcRect.top / math.tileHeight.toFloat()).toInt(), 1)
        val toY: Int = min(ceil(srcRect.bottom / math.tileHeight.toFloat()).toInt(), MapMath.visibleYCount)

        var tileRect: Rect

        for (i in fromX..toX) {
            for (j in fromY..toY) {
                tileRect = Rect(
                        math.tileWidth * (i - 1), math.tileHeight * (j - 1),
                        math.tileWidth * i,
                        math.tileHeight * j
                )

                getBitmap("ic_row_${j}_col_${i}")?.also { bitmap ->
                    if (intersectionRect.setIntersect(tileRect, srcRect)) {
                        with(relativeRect) {
                            left = (intersectionRect.left - math.totalX).toInt()
                            right = (intersectionRect.right - math.totalX).toInt()
                            top = (intersectionRect.top - math.totalY).toInt()
                            bottom = (intersectionRect.bottom - math.totalY).toInt()
                        }

                        with(intersectionRect) {
                            left -= tileRect.left
                            left = bitmap.width * left / math.tileWidth
                            right -= tileRect.left
                            right = bitmap.width * right / math.tileWidth
                            top -= tileRect.top
                            top = bitmap.height * top / math.tileHeight
                            bottom -= tileRect.top
                            bottom = bitmap.height * bottom / math.tileHeight
                        }

                        canvas.drawBitmap(bitmap, intersectionRect, relativeRect, bitmapPaint)
                    }
                }
            }
        }
    }

    var connectionState: ConnectionState? = null
    fun setConnectionState(state: ConnectionState?, gateway: Location?) {
        if (state == null) {
            return
        }

        this.connectionState = state
        when (state) {
            ConnectionState.CONNECTED -> {
                locationData.inProgress = false
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
        animator.centerLocation(math.totalX,
                math.totalY,
                DialogueDrawer.DialogState.NONE,
                MapAnimator.MovementAnimationType.CENTER_LOCATION
        )
    }

    private fun setLocation(location: Location?) {
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
            this.oldLocation = this.location
            this.location = location
            this.location?.let {
                it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)

                animator.startHideAnimation(math.totalX, math.totalY)
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
        updateCoordinates(true)
    }

    private var coordinateJob: Job? = null
    private fun updateCoordinates(isFirstInit: Boolean = false) {
        coordinateJob?.cancel()

        var locationCopy: Location? = null
        val serverLocationCopy = ArrayList<ServerLocation>()

        coordinateJob = GlobalScope.launch(Dispatchers.Default) {
            serverLocations?.let {

                var pair: Pair<Float, Float>
                var serverLocation: ServerLocation
                for (location in it) {
                    serverLocation = location.copy()
                    pair = math.getCoordinatesBy(location.longitude.toFloat(), location.latitude.toFloat())
                    serverLocation.x = pair.first
                    serverLocation.y = pair.second
                    serverLocationCopy.add(serverLocation)
                }
                ServerLocationsHolder.createAndFillRect(serverLocationCopy, context)
            }
            location?.let {
                locationCopy = it.copy()
                locationCopy?.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)
            }
        }

        coordinateJob?.invokeOnCompletion {
            serverLocations = serverLocationCopy
            serverLocationDrawer.serverLocations = serverLocationCopy


            location = locationCopy
            location?.coordinate?.let {
                if (isFirstInit) {
                    math.totalY = (it.second - height / 2f + panelHeight)
                    math.totalX = (it.first - width / 2f)
                }
            }
            locationData.isReady = true
            serverLocationsData.isReady = true

            postInvalidate()
        }
    }

    private var bitmapCache: LruCache<String, Bitmap>? = null
    private var thumbnails: HashMap<String, Bitmap?>? = null
    private fun initTiles() {
        val path = resources.getString(R.string.path_to_tiles)
        bitmapCache = MapHolder.getTilesFor()
        thumbnails = MapHolder.getThumbnails(path, context)
    }

    val tasks = HashMap<String, Job>()
    private fun getBitmap(name: String): Bitmap? {
        val key = "${name}_${String.format("%.0f", math.scaleFactor)}"
        if (math.scaleFactor >= 4.0f) return thumbnails?.get(name)

        bitmapCache?.get(key)?.also {
            return it
        } ?: run {
            tasks[key]?.also { jobObject ->
                if (jobObject.isCompleted) {
                    runNewTask(name, key)
                }
            } ?: run {
                runNewTask(name, key)
            }
            return thumbnails?.get(name)
        }
        return thumbnails?.get(name)
    }

    private fun runNewTask(name: String, path: String) {
        val prepareBitmapJob = prepareBitmap(name, path)
        tasks[path] = prepareBitmapJob
    }

    private fun prepareBitmap(name: String, path: String): Job {
        val prepareBitmapJob = GlobalScope.launch(Dispatchers.Default) {
            getBitmapFrom(context, name).also { bitmap ->
                bitmap?.let {
                    bitmapCache?.put(path, it)
                }
            }
        }

        prepareBitmapJob.invokeOnCompletion {
            tasks.remove(path)
            postInvalidate()
        }

        return prepareBitmapJob
    }

    private val defaultTransparentDrawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.fill_transparent,
            null
    )

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

            return drawable?.toBitmap(math.tileWidth, math.tileHeight, null)
        } catch (e: Exception) {
            return defaultTransparentDrawable?.toBitmap(math.tileWidth, math.tileHeight, null)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getIdentifier(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", IVPNApplication.application.packageName)
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