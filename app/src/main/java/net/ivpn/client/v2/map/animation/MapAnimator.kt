package net.ivpn.client.v2.map.animation

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import net.ivpn.client.v2.map.MapView

class MapAnimator(val listener: AnimatorListener) {

    var waveAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    var startX: Float = 0f
    var startY: Float = 0f
    var movementProgress = 0f
    var waveProgress = 0f

    fun fillAnimationData(data: AnimationData) {
        data.startX = startX
        data.startY = startY
        data.movementProgress = movementProgress
        data.waveProgress = waveProgress
    }

    fun startMovementAnimation(startX: Float, startY: Float) {
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.MOVEMENT_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                listener.updateFirstDraw(false)
//                locationDrawer.firstWave = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                startWaveAnimation()
                listener.updateMovingState(false)
//                locationData.isMoving = false
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                println("onAnimationStart")
                waveAnimator.cancel()
                listener.updateMovingState(true)
                listener.updateFirstDraw(true)
//                locationData.isMoving = true
//                locationDrawer.firstWave = true
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.onMovementProgressUpdate(movementProgress, startX, startY)
//            location?.let {
//                math.totalY = startY + (it.coordinate!!.second - height / 2f - startY + panelHeight) * movementProgress
//                math.totalX = startX + (it.coordinate!!.first - width / 2f - startX) * movementProgress
//                invalidate()
//            }
        }
        movementAnimator.start()
    }

    fun startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f)
        waveAnimator.duration = MapView.ANIMATION_DURATION
        waveAnimator.repeatCount = ValueAnimator.INFINITE
        waveAnimator.interpolator = LinearInterpolator()
        waveAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                listener.updateFirstDraw(false)
//                locationDrawer.firstWave = false
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
        waveAnimator.addUpdateListener { valueAnimator ->
            waveProgress = valueAnimator.animatedValue as Float
            listener.updateWaveProgress(waveProgress)
            listener.redraw()
//            locationData.progress = valueAnimator.animatedValue as Float
//            invalidate()
        }
        waveAnimator.start()
    }

    interface AnimatorListener {
        fun redraw()

        fun onMovementProgressUpdate(progress: Float, startX: Float, startY: Float)

        fun updateWaveProgress(progress: Float)

        fun updateFirstDraw(isFirstDraw: Boolean)

        fun updateMovingState(isMoving: Boolean)
    }
}