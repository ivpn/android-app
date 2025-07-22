package net.ivpn.core.v2.account.widget
/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import net.ivpn.core.R

class MaskedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isMasked = true
    private var blurredBitmap: Bitmap? = null
    private var isAnimating = false
    private var currentBlurAlpha = 1f
    private var isCapturingForBlur = false

    private var visibilityIcon: Drawable? = null
    private var visibilityOffIcon: Drawable? = null

    private val iconSize = (20 * resources.displayMetrics.density).toInt()
    private val iconMargin = (12 * resources.displayMetrics.density).toInt()
    private val iconPadding = (8 * resources.displayMetrics.density).toInt()

    private var transitionBitmap: Bitmap? = null
    private var transitionCanvas: Canvas? = null

    init {
        setOnClickListener { toggleMask() }
        loadIcons()
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight + iconSize + iconMargin + iconPadding,
            paddingBottom
        )

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (isMasked && blurredBitmap == null && width > 0 && height > 0) {
                    prepareBlur()
                    invalidate()
                }
                viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    private fun loadIcons() {
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val iconColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        val colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

        visibilityIcon = ContextCompat.getDrawable(context, R.drawable.outline_visibility_24)?.mutate()?.apply {
            this.colorFilter = colorFilter
        }
        visibilityOffIcon = ContextCompat.getDrawable(context, R.drawable.outline_visibility_off_24)?.mutate()?.apply {
            this.colorFilter = colorFilter
        }
    }

    private fun ensureTransitionCanvas() {
        if (transitionBitmap == null || transitionBitmap?.width != width || transitionBitmap?.height != height) {
            transitionBitmap?.recycle()
            transitionBitmap = createBitmap(width, height)
            transitionCanvas = Canvas(transitionBitmap!!)
        }
    }

    private fun prepareBlur() {
        if (!isMasked || blurredBitmap != null || width == 0 || height == 0) return

        val original = createBitmap(width, height)
        val tempCanvas = Canvas(original)

        isCapturingForBlur = true
        val savedMasked = isMasked
        isMasked = false
        draw(tempCanvas)
        isMasked = savedMasked
        isCapturingForBlur = false

        blurredBitmap = try {
            blur(original, 25)
        } catch (e: UnsatisfiedLinkError) {
            createFallbackBlur(original)
        } finally {
            original.recycle()
        }
    }

    private fun createFallbackBlur(bitmap: Bitmap): Bitmap? {
        return try {
            val scaled = bitmap.scale(bitmap.width / 8, bitmap.height / 8)
            val result = scaled.scale(bitmap.width, bitmap.height)
            scaled.recycle()
            result
        } catch (e: Exception) {
            null
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isMasked) {
            super.onDraw(canvas)
            drawIcon(canvas, visibilityIcon)
            return
        }

        blurredBitmap?.let { blurred ->
            val paint = if (isAnimating && currentBlurAlpha < 1f) {
                Paint().apply {
                    alpha = (currentBlurAlpha * 255).toInt()
                    isAntiAlias = true
                }
            } else null

            canvas.drawBitmap(blurred, 0f, 0f, paint)
        }

        if (isAnimating && currentBlurAlpha < 1f) {
            ensureTransitionCanvas()
            val tempCanvas = transitionCanvas!!
            tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            isCapturingForBlur = true
            val savedMasked = isMasked
            isMasked = false
            draw(tempCanvas)
            isMasked = savedMasked
            isCapturingForBlur = false

            val alphaPaint = Paint().apply {
                alpha = ((1f - currentBlurAlpha) * 255).toInt()
            }
            canvas.drawBitmap(transitionBitmap!!, 0f, 0f, alphaPaint)
        }

        drawIcon(canvas, visibilityOffIcon)
    }

    private fun drawIcon(canvas: Canvas, icon: Drawable?) {
        if (isCapturingForBlur) return

        icon?.let {
            val iconTop = (height - iconSize) / 2
            val iconLeft = width - iconSize - iconPadding
            it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            it.alpha = when {
                isAnimating && isMasked -> (currentBlurAlpha * 255).toInt()
                isAnimating && !isMasked -> ((1f - currentBlurAlpha) * 255).toInt()
                else -> 255
            }
            it.draw(canvas)
        }
    }

    fun toggleMask() {
        if (isAnimating) return
        isMasked = !isMasked
        if (isMasked) prepareBlur()
        animateBlurAlpha()
    }

    private fun animateBlurAlpha() {
        isAnimating = true
        val startAlpha = if (isMasked) 0f else 1f
        val endAlpha = if (isMasked) 1f else 0f

        ValueAnimator.ofFloat(startAlpha, endAlpha).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                currentBlurAlpha = it.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAnimating = false
                    currentBlurAlpha = 1f
                    if (!isMasked) {
                        blurredBitmap?.recycle()
                        blurredBitmap = null
                    }
                    invalidate()
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {
                    isAnimating = false
                }

                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
        }.start()
    }

    fun setMasked(mask: Boolean, animate: Boolean = false) {
        if (isAnimating) return
        isMasked = mask
        if (animate) toggleMask() else invalidate()
    }

    fun isTextMasked(): Boolean = isMasked

    private external fun blur(bitmap: Bitmap, radius: Int): Bitmap?

    companion object {
        init {
            try {
                System.loadLibrary("blurlib")
            } catch (_: UnsatisfiedLinkError) {}
        }
    }
}
