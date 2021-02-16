package net.ivpn.client.common.views

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

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View

class StatusBarSizeView : View {
    companion object {

        // status bar saved size
        var heightSize: Int = 0
    }

    constructor(context: Context) :
            super(context) {
        this.init()
    }

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs) {
        this.init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        this.init()
    }

    private fun init() {
        // do nothing if we already have the size
        if (heightSize != 0) {
            return
        }

        // listen to get the height
        (context as? Activity)?.window?.decorView?.setOnApplyWindowInsetsListener { _, windowInsets ->

            // get the size
            heightSize = windowInsets.systemWindowInsetTop

            // return insets
            windowInsets
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // if height is not zero height is ok
        if (h != 0 || heightSize == 0) {
            return
        }

        // apply the size
        postDelayed(Runnable {
            applyHeight(heightSize)
        }, 0)
    }

    private fun applyHeight(height: Int) {

        // apply the status bar height to the height of the view
        val lp = this.layoutParams
        lp.height = height
        this.layoutParams = lp
    }

}