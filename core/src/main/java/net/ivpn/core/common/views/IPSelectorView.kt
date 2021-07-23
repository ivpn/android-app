package net.ivpn.core.common.views

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

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
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import net.ivpn.core.R
import net.ivpn.core.v2.viewmodel.LocationViewModel.*

class IPSelectorView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {

    companion object {
        const val IPV4 = "IPv4"
        const val IPV6 = "IPv6"
    }

    private var ipState: IPState? = IPState.IPv4
    private var isInit = false

    private var listener: OnIPStateChangedListener? = null

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.ip_background, null)
    private val buttonColor = ResourcesCompat.getColor(resources, R.color.ip_button, null)
    private val textColor = ResourcesCompat.getColor(resources, R.color.ip_text, null)
    private val shadowColor = ResourcesCompat.getColor(resources, R.color.ip_shadow, null)

    private val cornerRadius = resources.getDimension(R.dimen.ip_selector_corner_radius)
    private val innerCornerRadius = resources.getDimension(R.dimen.ip_selector_inner_corner_radius)
    private val border = resources.getDimension(R.dimen.ip_selector_border)

    private val ipv4Rect = RectF()
    private val ipv6Rect = RectF()
    private val commonRect = RectF()

    private val backgroundPaint = Paint()
    private val buttonPaint = Paint()
    private val textPaint = TextPaint()

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            checkTap(event)
            return super.onSingleTapUp(event)
        }
    }
    private val gestureDetector = GestureDetector(this.context, gestureListener)

    init {
        with(backgroundPaint) {
            color = backgroundColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        with(buttonPaint) {
            color = buttonColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        buttonPaint.setShadowLayer(3.0f, 0.0f, 2.0f, shadowColor)

        with(textPaint) {
            isAntiAlias = true
            color = textColor
            textSize = resources.getDimension(R.dimen.ip_selector_text)
        }

        setOnTouchListener({ _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
        })
    }


//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        println("event = $event")
//        return gestureDetector.onTouchEvent(event)
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInit) return

        drawBackground(canvas)
        drawButton(canvas)
        drawTexts(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRoundRect(commonRect, cornerRadius, cornerRadius, backgroundPaint)
    }

    private fun drawButton(canvas: Canvas) {
        ipState?.let {
            when (it) {
                IPState.IPv4 -> {
                    canvas.drawRoundRect(ipv4Rect, innerCornerRadius, innerCornerRadius, buttonPaint)
                }
                IPState.IPv6 -> {
                    canvas.drawRoundRect(ipv6Rect, innerCornerRadius, innerCornerRadius, buttonPaint)
                }
            }
        }
    }

    private fun drawTexts(canvas: Canvas) {
        val bounds = Rect()

        textPaint.getTextBounds(IPV4, 0, IPV4.length, bounds)
        canvas.drawText(IPV4, ipv4Rect.centerX() - bounds.width() / 2, ipv4Rect.centerY() + bounds.height() / 2, textPaint)

        textPaint.getTextBounds(IPV6, 0, IPV6.length, bounds)
        canvas.drawText(IPV6, ipv6Rect.centerX() - bounds.width() / 2, ipv6Rect.centerY() + bounds.height() / 2, textPaint)
    }

    private fun checkTap(event: MotionEvent) {
        if (ipv4Rect.contains(event.x, event.y)) {
            setState(IPState.IPv4)
            listener?.onIPStateChanged(IPState.IPv4)
        }
        if (ipv6Rect.contains(event.x, event.y)) {
            setState(IPState.IPv6)
            listener?.onIPStateChanged(IPState.IPv6)
        }
    }

    fun setState(state: IPState) {
        this.ipState = state
        invalidate()
    }

    fun setStateListener(listener: OnIPStateChangedListener?) {
        this.listener = listener
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (width == 0 || height == 0) {
            return
        }
        postInit()
        isInit = true
        invalidate()
    }

    private fun postInit() {
        val buttonWidth = (width - 3 * border) / 2f
        val buttonHeight = height - 2 * border

        with(commonRect) {
            left = 0f
            right = width.toFloat()
            top = 0f
            bottom = height.toFloat()
        }

        with(ipv4Rect) {
            left = border
            right = (left + buttonWidth)
            top = border
            bottom = (top + buttonHeight)
        }

        with(ipv6Rect) {
            right = (width - border)
            left = (right - buttonWidth)
            top = border
            bottom = (top + buttonHeight)
        }
    }
}