package net.ivpn.core.common.views

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

class SecureEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs)

        // Ensure sensitive input type
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Set/override extras
        outAttrs.extras = outAttrs.extras ?: Bundle()

        // Return a wrapped InputConnection to suppress logging
        return object : InputConnectionWrapper(inputConnection, true) {
            override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
                // Suppress sensitive data logging
                return ""
            }

            override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
                // Suppress sensitive data logging
                return ""
            }
        }
    }
}