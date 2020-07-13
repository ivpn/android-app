package net.ivpn.client.v2.map.dialogue

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.dialogue.model.DialogueData
import net.ivpn.client.v2.map.dialogue.model.DialogueLocationData
import java.io.File

class DialogueDrawer(private val utils: DialogueUtil, private val context: Context) {

    //Paints for drawing
    private var dialoguePaint = Paint()
    private var titleTextPaint = TextPaint()
    private var locationTextPaint = TextPaint()

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

//        utils.calculateTextRects(titleTextPaint)
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
//        println("Draw dialogue centerX = $centerX centerY = $centerY")

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

        val drawable: Drawable? = getCountryDrawable(data.dialogueLocationData)
        val countryBound = Rect()
        val infoBound = Rect()

        when (data.state) {
//            DialogState.CHECKING -> {
//                canvas.drawText(
//                        utils.checkingLocation,
//                        dialogueRect.centerX() - utils.checkingRect.width() / 2,
//                        dialogueRect.centerY() + utils.checkingRect.height() / 2,
//                        titleTextPaint
//                )
//            }
            DialogState.PROTECTED -> {
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
                    with(infoBound) {
                        right = (dialogueRect.right - utils.dialogueMargin).toInt()
                        left = (right - utils.dialogueIconSize).toInt()
                        top = (dialogueRect.top + utils.dialogueMargin + utils.protectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                        bottom = (top + utils.dialogueIconSize).toInt()
                    }
                    it.bounds = infoBound
                    it.draw(canvas)
                }
            }
            DialogState.UNPROTECTED -> {
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
                    with(infoBound) {
                        right = (dialogueRect.right - utils.dialogueMargin).toInt()
                        left = (right - utils.dialogueIconSize).toInt()
                        top = (dialogueRect.top + utils.dialogueMargin + utils.unprotectedLocationRect.height() + utils.innerVerticalMargin).toInt()
                        bottom = (top + utils.dialogueIconSize).toInt()
                    }
                    it.bounds = infoBound
                    it.draw(canvas)
                }
            }
        }
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
                    + it.toLowerCase() + ".png")
            return Drawable.createFromStream(context.assets.open(path), null)
        } ?: return null
    }

    fun prepareDimensionsFor(dialogueLocationData: DialogueLocationData, state: DialogState) {
        utils.prepareDimensionsFor(dialogueLocationData, state)
    }

    enum class DialogState {
        NONE,
        PROTECTED,
        UNPROTECTED
    }
}