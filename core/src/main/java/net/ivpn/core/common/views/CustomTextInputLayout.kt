package net.ivpn.core.common.views

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
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import net.ivpn.core.R

class CustomTextInputLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : TextInputLayout(context, attrs, defStyleAttr) {
    private var mainHintTextSize: Float
    private var editTextSize = 0f

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        val b = child is EditText && mainHintTextSize > 0
        if (b) {
            val e = child as EditText
            editTextSize = e.textSize
            e.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainHintTextSize)
        }
        super.addView(child, index, params)
        if (b) {
            editText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize)
        }
    }

    // Units are pixels.
    fun getMainHintTextSize(): Float {
        return mainHintTextSize
    }

    // This optional method allows for dynamic instantiation of this class and
    // its EditText, but it cannot be used after the EditText has been added.
    // Units are scaled pixels.
    fun setMainHintTextSize(size: Float) {
        check(editText == null) { "Hint text size must be set before EditText is added" }
        mainHintTextSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, size, resources.displayMetrics)
    }

    init {
        val a: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CustomTextInputLayout)
        mainHintTextSize = a.getDimensionPixelSize(
                R.styleable.CustomTextInputLayout_mainHintTextSize, 0).toFloat()
        a.recycle()
    }
}