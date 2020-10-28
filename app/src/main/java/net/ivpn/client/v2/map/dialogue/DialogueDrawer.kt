package net.ivpn.client.v2.map.dialogue

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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.dialogue.model.DialogueData
import net.ivpn.client.v2.map.dialogue.model.DialogueLocationData
import java.io.File
import java.util.*

class DialogueDrawer(private val utils: DialogueUtil, private val context: Context) {

    //Paints for drawing
    private var dialoguePaint = Paint()
    private var titleTextPaint = TextPaint()
    private var locationTextPaint = TextPaint()
    private var connectTextPaint = TextPaint()
    private var connectButton = Paint()

    var infoButtonRect = Rect()
    var connectButtonRect = RectF()

    init {
        val resources = context.resources

        with(dialoguePaint) {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            color = ResourcesCompat.getColor(resources, R.color.dialogue_background, null)
        }
        dialoguePaint.setShadowLayer(7.0f, 0.0f, 2.0f, Color.BLACK)

        with(titleTextPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.dialogue_text, null)
            textSize = resources.getDimension(R.dimen.map_dialog_title_text_size)
            letterSpacing = -0.03f
        }

        with(locationTextPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.dialogue_title, null)
            textSize = resources.getDimension(R.dimen.map_dialog_location_text_size)
        }

        with(connectTextPaint) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.dialogue_button_text, null)
            textSize = resources.getDimension(R.dimen.map_dialog_location_text_size)
            letterSpacing = -0.01f
        }

        with(connectButton) {
            isAntiAlias = true
            color = ResourcesCompat.getColor(resources, R.color.primary, null)
        }
        connectButton.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK)
    }

    fun draw(canvas: Canvas, data: DialogueData) {
        if (data.state == DialogState.NONE) {
            return
        }

        val dialogueRect = RectF()
        with(dialogueRect) {
            left = data.x - utils.contentWidth / 2f
            top = data.y + utils.topMargin + utils.arrowHeight
            right = data.x + utils.contentWidth / 2f
            bottom = data.y + utils.topMargin + utils.arrowHeight + utils.contentHeight
        }
        drawDialogueBackground(canvas, data, dialogueRect)

        when (data.state) {
            DialogState.PROTECTED -> {
                drawContentForProtectedState(canvas, data, dialogueRect)
            }
            DialogState.UNPROTECTED -> {
                drawContentForUnProtectedState(canvas, data, dialogueRect)
            }
            DialogState.NONE -> {
            }
            DialogState.SERVER_CONNECT -> {
                drawContentForGateway(canvas, data, dialogueRect)
            }
        }
    }

    private fun drawDialogueBackground(canvas: Canvas, data: DialogueData, dialogueRect: RectF) {
        val path = Path()
        path.moveTo(data.x, data.y + utils.topMargin)
        path.lineTo(data.x - utils.arrowWidth / 2, data.y + utils.topMargin + utils.arrowHeight)
        path.lineTo(data.x + utils.arrowWidth / 2, data.y + utils.topMargin + utils.arrowHeight)
        path.addRoundRect(dialogueRect,
                utils.radii,
                Path.Direction.CW)

        path.close()

        canvas.drawPath(path, dialoguePaint)

        val locationRect = Rect()
        data.dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }
    }

    private fun drawContentForProtectedState(canvas: Canvas, data: DialogueData, dialogueRect: RectF) {
        val drawable: Drawable? = getCountryDrawable(data.dialogueLocationData)
        val countryBound = Rect()
        val locationRect = Rect()
        data.dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        canvas.drawText(
                utils.protectedLocation,
                dialogueRect.left + utils.dialogueMargin,
                dialogueRect.top + utils.protectedLocationRect.height() + utils.dialogueMargin,
                titleTextPaint
        )
        data.dialogueLocationData.description?.let {
            canvas.drawText(
                    it,
                    dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize + utils.innerHorizontalMargin,
                    dialogueRect.top + utils.dialogueMargin + utils.protectedLocationRect.height()
                            + utils.innerVerticalMargin + locationRect.height() + 0.1f * (utils.dialogueIconSize),
                    locationTextPaint
            )
        }
        drawable?.let {
            with(countryBound) {
                left = (dialogueRect.left + utils.dialogueMargin).toInt()
                right = (left + utils.dialogueIconSize).toInt()
                top = (dialogueRect.top + utils.dialogueMargin + utils.protectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                bottom = (top + utils.dialogueIconSize).toInt()
            }
            it.bounds = countryBound
            it.draw(canvas)
        }

        utils.infoDrawable?.let {
            with(infoButtonRect) {
                right = (dialogueRect.right - utils.dialogueMargin).toInt()
                left = (right - utils.dialogueIconSize).toInt()
                top = (dialogueRect.top + utils.dialogueMargin + utils.protectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                bottom = (top + utils.dialogueIconSize).toInt()
            }
            it.bounds = infoButtonRect
            it.draw(canvas)
        }
    }

    private fun drawContentForUnProtectedState(canvas: Canvas, data: DialogueData, dialogueRect: RectF) {
        val drawable: Drawable? = getCountryDrawable(data.dialogueLocationData)
        val countryBound = Rect()
        val locationRect = Rect()

        data.dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        canvas.drawText(
                utils.unprotectedLocation,
                dialogueRect.left + utils.dialogueMargin,
                dialogueRect.top + utils.unprotectedLocationRect.height() + utils.dialogueMargin,
                titleTextPaint
        )
//                2 * (utils.dialogueIconSize) / 5f
        data.dialogueLocationData.description?.let {
            canvas.drawText(
                    it,
                    dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize + utils.innerHorizontalMargin,
                    dialogueRect.top + utils.dialogueMargin + utils.unprotectedLocationRect.height()
                            + utils.innerVerticalMargin + locationRect.height() + 0.1f * (utils.dialogueIconSize),
                    locationTextPaint
            )
        }
        drawable?.let {
            with(countryBound) {
                left = (dialogueRect.left + utils.dialogueMargin).toInt()
                right = (left + utils.dialogueIconSize).toInt()
                top = (dialogueRect.top + utils.dialogueMargin + utils.unprotectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                bottom = (top + utils.dialogueIconSize).toInt()
            }
            it.bounds = countryBound
            it.draw(canvas)
        }
        utils.infoDrawable?.let {
            with(infoButtonRect) {
                right = (dialogueRect.right - utils.dialogueMargin).toInt()
                left = (right - utils.dialogueIconSize).toInt()
                top = (dialogueRect.top + utils.dialogueMargin + utils.unprotectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                bottom = (top + utils.dialogueIconSize).toInt()
            }
            it.bounds = infoButtonRect
            it.draw(canvas)
        }
    }

    private fun drawContentForGateway(canvas: Canvas, data: DialogueData, dialogueRect: RectF) {
        val drawable: Drawable? = getCountryDrawable(data.dialogueLocationData)
        val countryBound = Rect()
        val locationRect = Rect()
        data.dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        canvas.drawText(
                utils.gateway,
                dialogueRect.left + utils.dialogueMargin,
                dialogueRect.top + utils.gatewayTitleRect.height() + utils.dialogueMargin,
                titleTextPaint
        )
//                2 * (utils.dialogueIconSize) / 5f
        data.dialogueLocationData.description?.let {

            canvas.drawText(
                    it,
                    dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize + utils.innerHorizontalMargin,
                    dialogueRect.top + utils.dialogueMargin + utils.gatewayTitleRect.height()
                            + utils.innerVerticalMargin + (locationRect.height() - locationRect.bottom)
                            + (utils.dialogueIconSize - (locationRect.height() - locationRect.bottom)) / 2f,
                    locationTextPaint
            )
        }
        drawable?.let {
            with(countryBound) {
                left = (dialogueRect.left + utils.dialogueMargin).toInt()
                right = (left + utils.dialogueIconSize).toInt()
                top = (dialogueRect.top + utils.dialogueMargin + utils.gatewayTitleRect.height() + utils.innerVerticalMargin).toInt()
                bottom = (top + utils.dialogueIconSize).toInt()
            }
            it.bounds = countryBound
            it.draw(canvas)
        }

        with(connectButtonRect) {
            left = (dialogueRect.left + utils.dialogueMargin)
            right = (dialogueRect.right - utils.dialogueMargin)
            bottom = (dialogueRect.bottom - utils.dialogueMargin)
            top = (bottom - utils.buttonHeight)
        }

        canvas.drawRoundRect(connectButtonRect, 20f, 20f, connectButton)

        val connectTextRect = Rect()
        connectTextPaint.getTextBounds(utils.connectText, 0, utils.connectText.length, connectTextRect)
        canvas.drawText(utils.connectText,
                connectButtonRect.centerX() - connectTextRect.width() / 2f,
                connectButtonRect.centerY() + connectTextRect.height() / 2f,
                connectTextPaint
        )
    }

    private fun getCountryDrawable(dialogueLocationData: DialogueLocationData): Drawable? {
        if (dialogueLocationData.countryCode == null) {
            return null
        }
        if (dialogueLocationData.countryCode.equals("uk", ignoreCase = true)) {
            dialogueLocationData.countryCode = "gb"
        }

        var path: String
        dialogueLocationData.countryCode?.let {
            path = ("flag" + File.separator
                    + it.toLowerCase(Locale.getDefault()) + ".png")
            return Drawable.createFromStream(context.assets.open(path), null)
        } ?: return null
    }

    fun prepareDimensionsFor(dialogueLocationData: DialogueLocationData, state: DialogState) {
        utils.prepareDimensionsFor(dialogueLocationData, state)
    }

    fun clearElementRect() {
        clearRect(infoButtonRect)
        clearRect(connectButtonRect)
    }

    private fun clearRect(rect: Rect) {
        with(rect) {
            left = -1
            right = -1
            top = -1
            bottom = -1
        }
    }

    private fun clearRect(rect: RectF) {
        with(rect) {
            left = -1f
            right = -1f
            top = -1f
            bottom = -1f
        }
    }

    enum class DialogState {
        NONE,
        PROTECTED,
        UNPROTECTED,
        SERVER_CONNECT
    }
}