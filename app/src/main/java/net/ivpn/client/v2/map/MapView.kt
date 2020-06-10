package net.ivpn.client.v2.map

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.rest.data.proofs.LocationResponse
import net.ivpn.client.v2.map.dialogue.DialogueDrawer
import net.ivpn.client.v2.map.dialogue.DialogueUtil
import net.ivpn.client.v2.map.dialogue.model.DialogueData
import net.ivpn.client.v2.map.dialogue.model.LocationData
import net.ivpn.client.v2.map.model.Location
import net.ivpn.client.v2.map.model.Tile
import net.ivpn.client.v2.viewmodel.LocationViewModel
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class MapView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {

    @Inject
    lateinit var locationViewModel: LocationViewModel

    private val pointRadius: Float
    private val locationMaxRadius: Float
    private val locationRadius: Float

    //object that is used for calculation the x,y coordinates after fling gesture.
    //While fling animation is in process we can ask it for the newest and correct coordinates.
    private var scroller: Scroller = Scroller(this.context)

    private var location: Location? = null
    private var homeLocation: Location? = null

    private var panelHeight: Float = 0f

    //Objects that are used for drawing on the canvas
    private val pointPaint = Paint()
    private val bitmapPaint = Paint()
    private var wavePaint = Paint()

    private var bitmaps: Array<Array<Tile>> = arrayOf()

    private var dialogueDrawer: DialogueDrawer
    private var dialogueData = DialogueData()

    private var isInit = false
    private var progressWave = 0f
    private var firstWave = true

    private val math = MapMath()
    private val waveHandler: Handler


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

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            dialogueData.state = DialogueDrawer.DialogState.CHECKING
            locationViewModel.checkLocation(object : LocationViewModel.CheckLocationListener {
                override fun onSuccess(response: LocationResponse?) {
                    if (response == null) {
                        dialogueData.state = DialogueDrawer.DialogState.NONE
                        invalidate()
                        return
                    }

                    dialogueData.state = if (response.isIvpnServer)
                        DialogueDrawer.DialogState.PROTECTED
                    else DialogueDrawer.DialogState.UNPROTECTED

                    dialogueData.locationData = LocationData("${response.city},  ${response.country}", response.countryCode)
//                    curLong = response.longitude.toFloat()
//                    curLat = response.latitude.toFloat()
                }

                override fun onError() {
                    dialogueData.state = DialogueDrawer.DialogState.NONE
                    invalidate()
                }

            })
            return super.onSingleTapConfirmed(e)
        }
    }
    private val gestureDetector = GestureDetector(this.context, gestureListener)

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)

        with(pointPaint) {
            color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(bitmapPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val dialogueUtil = DialogueUtil(resources)
        dialogueDrawer = DialogueDrawer(dialogueUtil, context)

        with(wavePaint) {
            color = ResourcesCompat.getColor(resources, R.color.wave_connected, null)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        waveHandler = Handler()

        pointRadius = resources.getDimension(R.dimen.point_radius)
        locationMaxRadius = resources.getDimension(R.dimen.location_anim_max_radius)
        locationRadius = resources.getDimension(R.dimen.location_radius)
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
        drawMap(canvas)
//        drawCities(canvas)
        drawLocation(canvas)
        dialogueDrawer.draw(canvas, dialogueData)
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

    private fun drawLocation(canvas: Canvas) {
        if (isMoving) return
        if (location == null) return

        drawMultiWaves(canvas, progressWave)

        val srcRect = Rect()

        with(srcRect) {
            left = (math.totalX).toInt()
            right = (math.totalX + width).toInt()
            top = (math.totalY).toInt()
            bottom = (math.totalY + height).toInt()
        }

        pointPaint.color = if (location!!.isConnected) ResourcesCompat.getColor(
                resources,
                R.color.wave_connected,
                null
        ) else ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)

        val location = location?.coordinate ?: return

        canvas.drawCircle(
                location.first - srcRect.left,
                location.second - srcRect.top,
                pointRadius,
                pointPaint
        )
    }

    private fun drawMultiWaves(canvas: Canvas?, progress: Float) {
        var currentProgress = progress

        for (i in 1..WAVES_COUNT) {
            drawWave(canvas, currentProgress)
            if (firstWave && (getNextProgress(currentProgress) > currentProgress)) {
                break
            } else {
                currentProgress = getNextProgress(currentProgress)
            }
        }
    }

    private fun getNextProgress(progress: Float): Float {
        val waveStep = 1f / WAVES_COUNT

        return if (progress - waveStep > 0) (progress - waveStep) else (progress + (1 - waveStep))
    }

    private fun drawWave(canvas: Canvas?, progress: Float) {
        if (location == null) return
        val srcRect = Rect()
        wavePaint.color = if (location!!.isConnected) ResourcesCompat.getColor(
                resources,
                R.color.wave_connected,
                null
        ) else ResourcesCompat.getColor(resources, R.color.wave_disconnected, null)

        with(srcRect) {
            left = (math.totalX).toInt()
            right = (math.totalX + width).toInt()
            top = (math.totalY).toInt()
            bottom = (math.totalY + height).toInt()
        }
        val location = location?.coordinate ?: return

        val radius = (locationMaxRadius - pointRadius) * progress + pointRadius
        wavePaint.alpha = ((MAX_ALPHA * (1 - progress)).toInt())
        canvas?.drawCircle(
                location.first - srcRect.left,
                location.second - srcRect.top,
                radius,
                wavePaint
        )
    }

    fun setHomeLocation(location: Location) {
        println("Set HOME location as $location isInit = $isInit")
        homeLocation = location
        if (isInit) {
            setLocation(homeLocation)
        }
    }

    fun setConnectedLocation(location: Location?) {
        if (location == null) {
            setLocation(homeLocation)
        } else {
            setLocation(location)
        }
    }

    private fun setLocation(location: Location?) {
        println("Set current location as $location")
        this.location = location
        location?.let {
            it.coordinate = math.getCoordinatesBy(it.longitude, it.latitude)

            startMovementAnimation()
        }
    }

    fun setBottomPadding(padding: Float) {
        math.totalY = math.totalY - (panelHeight - padding / 2f)
        this.panelHeight = padding / 2f
        with(dialogueData) {
            x = width / 2f
            y = height / 2f - panelHeight
        }

        invalidate()
    }

    var startX: Float = 0f
    var startY: Float = 0f
    var progressMove = 0f
    var isMoving: Boolean = false
    private fun startMovementAnimation() {
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MOVEMENT_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                firstWave = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                startWaveAnimation()
                isMoving = false
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                println("onAnimationStart")
                waveAnimator?.cancel()
                startX = math.totalX
                startY = math.totalY
                isMoving = true
                firstWave = true
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            progressMove = valueAnimator.animatedValue as Float

            location?.let {
                math.totalY = startY + (it.coordinate!!.second - height / 2f - startY + panelHeight) * progressMove
                math.totalX = startX + (it.coordinate!!.first - width / 2f - startX) * progressMove
                invalidate()
            }
        }
        movementAnimator.start()
    }

    var waveAnimator: ValueAnimator? = null
    private fun startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f)
        waveAnimator?.duration = ANIMATION_DURATION
        waveAnimator?.repeatCount = ValueAnimator.INFINITE
        waveAnimator?.interpolator = LinearInterpolator()
        waveAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                firstWave = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                println("onAnimationStart")
            }

        })
        waveAnimator?.addUpdateListener { valueAnimator ->
            progressWave = valueAnimator.animatedValue as Float
            invalidate()
        }
        waveAnimator?.start()
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
            initTiles()
            isInit = true
        }
        job?.invokeOnCompletion {
            MainScope().launch {
                println("isInit = true")
                if (homeLocation != null) {
                    setLocation(homeLocation)
                }
            }
        }
    }

    private fun initTiles() {
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
                            getBitmapFrom("tiles/row-${j}-col-${i}.png")
                    )
                }
                bitmaps += array
            }
        }
        println("Bitmap init time = $executionTime")
    }

    private fun getBitmapFrom(assetPath: String): Bitmap {
        val drawable = Drawable.createFromStream(
                context.assets.open(assetPath), null
        )

        return Bitmap.createScaledBitmap(
                (drawable as BitmapDrawable).bitmap, MapMath.tileWidth,
                MapMath.tileHeight, false
        )
    }

    companion object {
        const val ANIMATION_DURATION = 8000L
        const val MOVEMENT_ANIMATION_DURATION = 1000L

        const val MAX_ALPHA = 255

        const val WAVES_COUNT = 3
    }
}