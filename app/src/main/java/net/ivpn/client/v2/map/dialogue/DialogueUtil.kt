package net.ivpn.client.v2.map.dialogue

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.R
import net.ivpn.client.v2.map.dialogue.model.DialogueLocationData
import kotlin.math.max

class DialogueUtil(val resources: Resources) {

    var protectedLocationRect = Rect()
    var unprotectedLocationRect = Rect()
    var checkingRect = Rect()
    var locationRect = Rect()
    var infoButtonRect = Rect()

    var checkingLocation = resources.getString(R.string.map_dialog_checking_location)
    var protectedLocation = resources.getString(R.string.map_dialog_connected_title)
    var unprotectedLocation = resources.getString(R.string.map_dialog_not_connected_title)

    var radii: FloatArray

    var dialogueMargin = 0f
    var dialogueIconSize = 0f
    var arrowHeight = 0f
    var arrowWidth = 0f
    var topMargin = 0f
    var innerVerticalMargin = 0f
    var innerHorizontalMargin = 0f
    var cornerRadius = 0f
    var contentHeight = 0f
    var contentWidth = 0f
    var yShift = 0f

    var infoDrawable: Drawable?

    private var titleTextPaint = TextPaint()
    private var locationTextPaint = TextPaint()

    init {
        dialogueMargin = resources.getDimension(R.dimen.map_dialog_side_margin)
        dialogueIconSize = resources.getDimension(R.dimen.map_dialog_icon_size)
        arrowHeight = resources.getDimension(R.dimen.map_dialog_arrow_height)
        arrowWidth = resources.getDimension(R.dimen.map_dialog_arrow_width)
        topMargin = resources.getDimension(R.dimen.map_dialog_outer_top_margin)
        innerVerticalMargin = resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)
        innerHorizontalMargin = resources.getDimension(R.dimen.map_dialog_inner_horizontal_margin)
        cornerRadius = resources.getDimension(R.dimen.map_dialog_corner_radius)
        yShift = resources.getDimension(R.dimen.map_dialog_y_shift)

        radii = FloatArray(8) {
            cornerRadius
        }

        infoDrawable = getDrawable(R.drawable.ic_info)

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
    }

    private fun getDrawable(resId: Int): Drawable? {
        return ResourcesCompat.getDrawable(
                resources,
                resId,
                null
        )
    }

    fun prepareDimensionsFor(dialogueLocationData: DialogueLocationData, dialogState: DialogueDrawer.DialogState) {
        when (dialogState) {
            DialogueDrawer.DialogState.NONE -> {
                return
            }
            DialogueDrawer.DialogState.PROTECTED -> {
                prepareProtectedState(dialogueLocationData)
            }
            DialogueDrawer.DialogState.UNPROTECTED -> {
                prepareUnprotectedState(dialogueLocationData)
            }
        }
    }

    private fun prepareProtectedState(dialogueLocationData: DialogueLocationData) {
        titleTextPaint.getTextBounds(protectedLocation, 0, protectedLocation.length, protectedLocationRect)
        val titleWidth: Float = protectedLocationRect.width().toFloat()

        dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        val descriptionWidth: Float = dialogueIconSize + locationRect.width() + innerHorizontalMargin

        contentWidth = 3 * dialogueMargin + max(descriptionWidth, titleWidth) + dialogueIconSize
        contentHeight = 2 * dialogueMargin + innerVerticalMargin + protectedLocationRect.height() + dialogueIconSize - yShift
    }

    private fun prepareUnprotectedState(dialogueLocationData: DialogueLocationData) {
        titleTextPaint.getTextBounds(unprotectedLocation, 0, unprotectedLocation.length, unprotectedLocationRect)
        val titleWidth: Float = unprotectedLocationRect.width().toFloat()

        dialogueLocationData.description?.let {
            locationTextPaint.getTextBounds(it, 0, it.length, locationRect)
        }

        val descriptionWidth: Float = dialogueIconSize + locationRect.width() + innerHorizontalMargin

        contentWidth = 3 * dialogueMargin + max(descriptionWidth, titleWidth) + dialogueIconSize
        contentHeight = 2 * dialogueMargin + innerVerticalMargin + unprotectedLocationRect.height() + dialogueIconSize - yShift
    }

}