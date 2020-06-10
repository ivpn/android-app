package net.ivpn.client.v2.map.dialogue

import android.content.res.Resources
import android.graphics.Rect
import android.text.TextPaint
import net.ivpn.client.R

class DialogueUtil(resources: Resources) {

    var protectedLocationRect = Rect()
    var unprotectedLocationRect = Rect()
    var checkingRect = Rect()

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

    init {
        dialogueMargin = resources.getDimension(R.dimen.map_dialog_side_margin)
        dialogueIconSize = resources.getDimension(R.dimen.map_dialog_icon_size)
        arrowHeight = resources.getDimension(R.dimen.map_dialog_arrow_height)
        arrowWidth = resources.getDimension(R.dimen.map_dialog_arrow_width)
        topMargin = resources.getDimension(R.dimen.map_dialog_outer_top_margin)
        innerVerticalMargin = resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)
        innerHorizontalMargin = resources.getDimension(R.dimen.map_dialog_inner_horizontal_margin)
        cornerRadius = resources.getDimension(R.dimen.map_dialog_corner_radius)

        radii = FloatArray(8) {
            cornerRadius
        }
    }

    fun calculateTextRects(textPaint: TextPaint) {
        textPaint.getTextBounds(checkingLocation, 0, checkingLocation.length, checkingRect)
        textPaint.getTextBounds(protectedLocation, 0, protectedLocation.length, protectedLocationRect)
        textPaint.getTextBounds(unprotectedLocation, 0, unprotectedLocation.length, unprotectedLocationRect)

        contentHeight = 2 * dialogueMargin + innerVerticalMargin + protectedLocationRect.height() + dialogueIconSize
        contentWidth = 2 * dialogueMargin + protectedLocationRect.width() + innerHorizontalMargin + dialogueIconSize
    }

}