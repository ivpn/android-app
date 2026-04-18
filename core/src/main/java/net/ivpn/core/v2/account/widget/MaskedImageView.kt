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
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import net.ivpn.core.R

class MaskedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var originalDrawable: Drawable? = null
    private var isMasked: Boolean = true
    private var blurredDrawable: Drawable? = null
    private var isAnimating = false
    private var isInitialized = false
    private var hasShownFirstImage = false

    private var visibilityOffIcon: Drawable? = null
    private val iconSize = (32 * resources.displayMetrics.density).toInt()
    private val circleRadius = (iconSize / 2f) + 12f
    private var iconAlpha = 1f

    private var compositeBitmap: Bitmap? = null
    private var compositeCanvas: Canvas? = null

    init {
        loadIcon()
        alpha = 0f
    }

    private fun loadIcon() {
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val iconColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        val colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

        visibilityOffIcon = ContextCompat.getDrawable(context, R.drawable.outline_visibility_off_24)?.mutate()?.apply {
            this.colorFilter = colorFilter
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !isInitialized) {
            isInitialized = true
            if (originalDrawable != null && isMasked) {
                prepareBlur()
                updateDisplay()
            }
        }
    }

    private fun prepareBlur() {
        if (blurredDrawable == null && originalDrawable != null) {
            val bitmap = drawableToBitmap(originalDrawable!!) ?: return
            blurredDrawable = try {
                blur(bitmap, 25)?.toDrawable(resources)
            } catch (e: UnsatisfiedLinkError) {
                createFallbackBlur(bitmap)?.toDrawable(resources)
            }
        }
    }

    private fun createFallbackBlur(bitmap: Bitmap): Bitmap? {
        return try {
            val scaledDown = bitmap.scale(bitmap.width / 8, bitmap.height / 8)
            val result = scaledDown.scale(bitmap.width, bitmap.height)
            scaledDown.recycle()
            result
        } catch (e: Exception) {
            null
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isMasked || isAnimating) {
            super.onDraw(canvas)
        } else {
            blurredDrawable?.let { blurred ->
                blurred.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
                blurred.draw(canvas)
            }
        }
        if (isMasked) drawCenterIcon(canvas)
    }

    private fun drawCenterIcon(canvas: Canvas) {
        visibilityOffIcon?.let { icon ->
            val centerX = (width - iconSize) / 2
            val centerY = (height - iconSize) / 2

            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
            icon.alpha = (iconAlpha * 255).toInt()

            val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val backgroundColor = if (isDarkTheme) Color.BLACK else Color.WHITE
            val backgroundPaint = Paint().apply {
                color = backgroundColor
                alpha = (iconAlpha * 200).toInt()
                isAntiAlias = true
            }

            canvas.drawCircle(
                centerX + iconSize / 2f,
                centerY + iconSize / 2f,
                circleRadius,
                backgroundPaint
            )

            icon.draw(canvas)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (originalDrawable === drawable) return
        originalDrawable = drawable
        blurredDrawable = null
        if (isInitialized && isMasked) prepareBlur()
        updateDisplay()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        originalDrawable = bm?.toDrawable(resources)
        blurredDrawable = null
        if (isInitialized && isMasked) prepareBlur()
        updateDisplay()
    }

    private fun updateDisplay() {
        val displayDrawable = if (isMasked) blurredDrawable else originalDrawable
        setImageDrawableInternal(displayDrawable)

        if (!hasShownFirstImage && displayDrawable != null) {
            hasShownFirstImage = true
            animate()
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun setImageDrawableInternal(drawable: Drawable?) {
        super.setImageDrawable(drawable)
    }

    fun toggleMask() {
        if (isAnimating) return
        isMasked = !isMasked
        if (isMasked && blurredDrawable == null) prepareBlur()
        animateCrossfade()
    }

    private fun animateCrossfade() {
        isAnimating = true
        val startDrawable = if (isMasked) originalDrawable else blurredDrawable
        val endDrawable = if (isMasked) blurredDrawable else originalDrawable

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                iconAlpha = if (isMasked) progress else (1f - progress)
                setImageDrawableInternal(createCompositeDrawable(startDrawable, endDrawable, progress))
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    setImageDrawableInternal(endDrawable)
                    isAnimating = false
                    iconAlpha = 1f
                    invalidate()
                }
                override fun onAnimationCancel(animation: Animator) { isAnimating = false }
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }.start()
    }

    private fun createCompositeDrawable(startDrawable: Drawable?, endDrawable: Drawable?, progress: Float): Drawable? {
        if (startDrawable == null || endDrawable == null) return endDrawable ?: startDrawable

        val w = width.takeIf { it > 0 } ?: 1
        val h = height.takeIf { it > 0 } ?: 1

        if (compositeBitmap == null || compositeBitmap?.width != w || compositeBitmap?.height != h) {
            compositeBitmap?.recycle()
            compositeBitmap = createBitmap(w, h)
            compositeCanvas = Canvas(compositeBitmap!!)
        }

        val canvas = compositeCanvas!!
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        startDrawable.setBounds(0, 0, w, h)
        startDrawable.alpha = ((1f - progress) * 255).toInt()
        startDrawable.draw(canvas)

        endDrawable.setBounds(0, 0, w, h)
        endDrawable.alpha = (progress * 255).toInt()
        endDrawable.draw(canvas)

        startDrawable.alpha = 255
        endDrawable.alpha = 255

        return compositeBitmap!!.toDrawable(resources)
    }

    fun setMasked(mask: Boolean, animate: Boolean = false) {
        if (isAnimating || isMasked == mask) return
        isMasked = mask
        if (animate) toggleMask()
        else {
            if (isMasked && blurredDrawable == null) prepareBlur()
            updateDisplay()
            invalidate()
        }
    }

    fun isImageMasked(): Boolean = isMasked

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (blurredDrawable as? BitmapDrawable)?.bitmap?.recycle()
        blurredDrawable = null
        compositeBitmap?.recycle()
        compositeBitmap = null
        compositeCanvas = null
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) return drawable.bitmap

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private external fun blur(bitmap: Bitmap, radius: Int): Bitmap?
}