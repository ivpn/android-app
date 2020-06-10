package net.ivpn.client.v2.map.dialogue

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.dialogue.model.DialogueData
import net.ivpn.client.v2.map.dialogue.model.LocationData
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
        dialoguePaint.setShadowLayer(7.0f, 0.0f, 2.0f, Color.BLACK);

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

        utils.calculateTextRects(titleTextPaint)
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
        data.locationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        if (data.state != DialogState.CHECKING) {
            val drawable: Drawable? = getCountryDrawable(data.locationData)
            drawable?.let {
                it.bounds = Rect((dialogueRect.left + utils.dialogueMargin).toInt(),
                        (dialogueRect.top + utils.dialogueMargin + utils.checkingRect.height() + utils.innerVerticalMargin).toInt(),
                        (dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize).toInt(),
                        (dialogueRect.top + utils.dialogueMargin + utils.checkingRect.height() + utils.innerVerticalMargin + utils.dialogueIconSize).toInt()
                )
                it.draw(canvas)
            }
        }

        when (data.state) {
            DialogState.CHECKING -> {
                canvas.drawText(
                        utils.checkingLocation,
                        dialogueRect.centerX() - utils.checkingRect.width() / 2,
                        dialogueRect.centerY() + utils.checkingRect.height() / 2,
                        titleTextPaint
                )
            }
            DialogState.PROTECTED -> {
                canvas.drawText(
                        utils.protectedLocation,
                        dialogueRect.left + utils.dialogueMargin,
                        dialogueRect.top + utils.checkingRect.height() / 2 + utils.dialogueMargin,
                        titleTextPaint
                )
                data.locationData.description?.let {
                    canvas.drawText(
                            it,
                            dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize + utils.innerHorizontalMargin,
                            dialogueRect.top + utils.dialogueMargin + utils.checkingRect.height() + utils.innerVerticalMargin + 2 * (utils.dialogueIconSize) / 5f + locationRect.height() / 2,
                            locationTextPaint
                    )
                }
            }
            DialogState.UNPROTECTED -> {
                canvas.drawText(
                        utils.unprotectedLocation,
                        dialogueRect.left + utils.dialogueMargin,
                        dialogueRect.top + utils.checkingRect.height() / 2 + utils.dialogueMargin,
                        titleTextPaint
                )
                data.locationData.description?.let {
                    canvas.drawText(
                            it,
                            dialogueRect.left + utils.dialogueMargin + utils.dialogueIconSize + utils.innerHorizontalMargin,
                            dialogueRect.top + utils.dialogueMargin + utils.checkingRect.height() + utils.innerVerticalMargin + 2 * (utils.dialogueIconSize) / 5f + locationRect.height() / 2,
                            locationTextPaint
                    )
                }
//                description?.let {
//                    locationTextPaint.getTextBounds(it, 0, it.length, protectedLocationRect)
//                }
            }
        }
    }

    private fun getCountryDrawable(locationData: LocationData): Drawable? {
        if (locationData.countryCode == null) {
            return null
        }
        if (locationData.countryCode.equals("uk", ignoreCase = true)) {
            locationData.countryCode = "gb"
        }

        var path: String
        locationData.countryCode?.let {
            path = ("flag" + File.separator
                    + it.toLowerCase() + ".png")
            return Drawable.createFromStream(context.assets.open(path), null)
        } ?: return null
    }

    enum class DialogState {
        NONE,
        CHECKING,
        PROTECTED,
        UNPROTECTED
    }
}