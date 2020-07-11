package net.ivpn.client.v2.map.animation

import android.animation.Animator
import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import net.ivpn.client.v2.map.MapView

class MapAnimator(val listener: AnimatorListener) {

    var waveAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    var startX: Float = 0f
    var startY: Float = 0f
    var appearProgress = 0f
    var movementProgress = 0f
    var waveProgress = 0f

    fun fillAnimationData(data: AnimationData) {
        data.startX = startX
        data.startY = startY
        data.movementProgress = movementProgress
        data.waveProgress = waveProgress
    }

    fun centerLocation(startX: Float, startY: Float) {
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.CENTER_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
//                listener.updateFirstDraw(false)
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                listener.onCenterAnimationFinish()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                println("onAnimationStart")
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.updateMovementProgress(movementProgress, startX, startY)
        }
        movementAnimator.start()
    }

    fun startMovementAnimation(startX: Float, startY: Float, postWaveAnimation: Boolean) {
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.MOVEMENT_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                startAppearAnimation(postWaveAnimation)
//                if (postWaveAnimation) {
//                    startWaveAnimation()
//                }
                listener.onEndMovementAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                println("onAnimationStart")
                waveAnimator.cancel()
                listener.onStartMovementAnimation()
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.updateMovementProgress(movementProgress, startX, startY)
        }
        movementAnimator.start()
    }

    fun startAppearAnimation(postWaveAnimation: Boolean) {
        Log.d("MapAnimator", "startAppearAnimation postWaveAnimation = $postWaveAnimation ")
        val appearAnimator = ValueAnimator.ofFloat(0f, 1f)
        appearAnimator.duration = MapView.APPEAR_ANIMATION_DURATION
        appearAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                if (postWaveAnimation) {
                    startWaveAnimation()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        appearAnimator.addUpdateListener { valueAnimator ->
            appearProgress = valueAnimator.animatedValue as Float
            Log.d("MapAnimator", "Update appear animation appearProgress = $appearProgress ")
            listener.updateAppearProgress(appearProgress)
            listener.redraw()
        }
        appearAnimator.start()
    }

    fun startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f)
        waveAnimator.duration = MapView.ANIMATION_DURATION
        waveAnimator.interpolator = LinearInterpolator()
        waveAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                println("onAnimationEnd")
                waveAnimator.startDelay = 5000
                waveAnimator.start()
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
        }
        waveAnimator.start()
    }

    interface AnimatorListener {
        fun redraw()

        fun onStartMovementAnimation()

        fun updateMovementProgress(progress: Float, startX: Float, startY: Float)

        fun onEndMovementAnimation()

        fun updateAppearProgress(progress: Float)

        fun updateWaveProgress(progress: Float)

        fun onCenterAnimationFinish()
    }
}