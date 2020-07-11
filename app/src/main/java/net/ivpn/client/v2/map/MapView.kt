package net.ivpn.client.v2.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.ivpn.client.IVPNApplication
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
import net.ivpn.client.v2.map.model.Tile
import net.ivpn.client.v2.map.servers.ServerLocationDrawer
import net.ivpn.client.v2.map.servers.model.ServerLocationsData
import net.ivpn.client.v2.viewmodel.LocationViewModel

class MapView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {

//    @Inject
//    lateinit var locationViewModel: LocationViewModel

    //object that is used for calculation the x,y coordinates after fling gesture.
    //While fling animation is in process we can ask it for the newest and correct coordinates.
    private var scroller: Scroller = Scroller(this.context)

    private var panelHeight: Float = 0f

    private val bitmapPaint = Paint()
    private var bitmaps: Array<Array<Tile>> = arrayOf()

    private var dialogueDrawer: DialogueDrawer
    private var dialogueData = DialogueData()

    private var serverLocationDrawer = ServerLocationDrawer(resources)
    private var serverLocationsData = ServerLocationsData()

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
            dialogueData.state = DialogueDrawer.DialogState.NONE

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

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            locationData.location?.coordinate?.let {
                val locationPointerRect = Rect()
                with(locationPointerRect) {
                    left = (it.first - 200 - math.totalX).toInt()
                    right = (it.first + 200 - math.totalX).toInt()
                    top = (it.second - 200 - math.totalY).toInt()
                    bottom = (it.second + 200 - math.totalY).toInt()
                }

                if (locationPointerRect.contains(event.x.toInt(), event.y.toInt())) {
                    animator.centerLocation(math.totalX, math.totalY)
                } else {
                    dialogueData.state = DialogueDrawer.DialogState.NONE
                    invalidate()
                }
            }
            return super.onSingleTapConfirmed(event)
        }
    }
    private val gestureDetector = GestureDetector(this.context, gestureListener)

    val locationListener = object : LocationViewModel.CheckLocationListener {
        override fun onSuccess(location: Location) {
//            if (connectionState == ConnectionState.NOT_CONNECTED) {
            setLocation(location)
//            }
        }

        override fun onError() {
        }
    }

    private var serverPointPaint = Paint()
    private var serversPaint = TextPaint()
    private var serversPaintStroke = TextPaint()

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)

        with(bitmapPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val dialogueUtil = DialogueUtil(resources)
        dialogueDrawer = DialogueDrawer(dialogueUtil, context)

        with(serverPointPaint) {
            color = ResourcesCompat.getColor(resources, R.color.primary, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(serversPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.primary, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            typeface = Typeface.DEFAULT_BOLD
        }

        with(serversPaintStroke) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.map_label_shadow, null)
            textSize = resources.getDimension(R.dimen.location_name)
            letterSpacing = 0.03f
            strokeWidth = 4f
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.FILL_AND_STROKE
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
        locationDrawer.draw(canvas, locationData)

        dialogueDrawer.draw(canvas, dialogueData)
    }

    private fun openLocationDialogue() {
        //TODO fix it.
        dialogueData.state = DialogueDrawer.DialogState.CHECKING
//        locationViewModel.checkLocation(object : LocationViewModel.CheckLocationListener {
//            override fun onSuccess(response: LocationResponse?) {
//                if (response == null) {
//                    dialogueData.state = DialogueDrawer.DialogState.NONE
//                    invalidate()
//                    return
//                }
//
//                dialogueData.state = if (response.isIvpnServer)
//                    DialogueDrawer.DialogState.PROTECTED
//                else DialogueDrawer.DialogState.UNPROTECTED
//
//                dialogueData.dialogueLocationData = DialogueLocationData("${response.city},  ${response.country}", response.countryCode)
//            }
//
//            override fun onError() {
//                dialogueData.state = DialogueDrawer.DialogState.NONE
//                invalidate()
//            }
//
//        })
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

        for (list in bitmaps) {
            for (tile in list) {

                if (intersectionRect.setIntersect(tile.rect, srcRect)) {
                    with(relativeRect) {
                        left = (intersectionRect.left - math.totalX).toInt()
                        right = (intersectionRect.right - math.totalX).toInt()
                        top = (intersectionRect.top - math.totalY).toInt()
                        bottom = (intersectionRect.bottom - math.totalY).toInt()
                    }
                    with(intersectionRect) {
                        left -= tile.rect.left
                        right -= tile.rect.left
                        top -= tile.rect.top
                        bottom -= tile.rect.top
                    }
                    canvas.drawBitmap(tile.bitmap, intersectionRect, relativeRect, bitmapPaint)
                }
            }
        }
    }

    var connectionState: ConnectionState? = null
    fun setConnectionState(state: ConnectionState?, gateway: Location?) {
        println("Set connection state = ${state}, gateway = ${gateway}")
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
                invalidate()
            }
            ConnectionState.PAUSING -> {
                locationData.inProgress = true
                if (gateway != null) {
                    gateway.isConnected = true
                    setLocation(gateway)
                }
                invalidate()
            }
            ConnectionState.PAUSED -> {
                locationData.inProgress = true
                if (gateway != null) {
                    gateway.isConnected = true
                    setLocation(gateway)
                }
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
        }
        invalidate()
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
            return
        }
        this.oldLocation = this.location
        this.location = location

        if (!isInit) {
            return
        }
        location?.let {
            it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)

            animator.startMovementAnimation(math.totalX, math.totalY, it.isConnected)
        }
    }

    fun setPanelHeight(padding: Float) {
        math.totalY = math.totalY - (panelHeight - padding / 2f)
        this.panelHeight = padding / 2f
        with(dialogueData) {
            x = width / 2f
            y = height / 2f - panelHeight
        }

        invalidate()
    }

    private var job: Job? = null
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (width == 0 || height == 0) {
            return
        }

        with(dialogueData) {
            x = width / 2f
            y = height / 2f - panelHeight
        }

        job = GlobalScope.launch {
            println("Start to init map")
            math.setScreenSize(width.toFloat(), height.toFloat())
            location?.let {
                it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)
                math.totalY = (it.coordinate!!.second - height / 2f + panelHeight)
                math.totalX = (it.coordinate!!.first - width / 2f)
                invalidate()
            }
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
        }
        serverLocationDrawer.serverLocations = serverLocations
        invalidate()
    }

    private fun initTiles() {
        bitmaps = MapHolder.getTilesFor(resources.getString(R.string.path_to_tiles), context)
    }

    private fun getAnimatorListener(): MapAnimator.AnimatorListener {
        return object : MapAnimator.AnimatorListener {
            override fun redraw() {
                invalidate()
            }

            override fun onStartMovementAnimation() {
                locationData.drawCurrentLocation = false
            }

            override fun updateMovementProgress(progress: Float, startX: Float, startY: Float) {
                locationData.moveAnimationProgress = progress
                location?.let {
                    math.totalY = startY + (it.coordinate!!.second - height / 2f - startY + panelHeight) * progress
                    math.totalX = startX + (it.coordinate!!.first - width / 2f - startX) * progress
                    invalidate()
                }
            }

            override fun onEndMovementAnimation() {
                locationData.drawCurrentLocation = true
                oldLocation = null
            }

            override fun updateAppearProgress(progress: Float) {
                locationData.appearProgress = progress
            }

            override fun updateWaveProgress(progress: Float) {
                locationData.waveAnimationProgress = progress
            }

            override fun onCenterAnimationFinish() {
                openLocationDialogue()
            }
        }
    }

    companion object {
        const val ANIMATION_DURATION = 2000L
        const val MOVEMENT_ANIMATION_DURATION = 1000L
        const val APPEAR_ANIMATION_DURATION = 500L
        const val CENTER_ANIMATION_DURATION = 300L

        const val MAX_ALPHA = 255

        const val WAVES_COUNT = 3
    }
}