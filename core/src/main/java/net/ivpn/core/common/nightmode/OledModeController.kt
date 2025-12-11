package net.ivpn.core.common.nightmode

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

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R

object OledModeController {

    const val OLED_BLACK = Color.BLACK
    const val OLED_CARD = 0xFF0A0A0A.toInt()
    const val OLED_HANDLE = 0xFF666666.toInt()

    private val darkGrayColors = setOf(
        0xFF202020.toInt(),
        0xFF1C1C1C.toInt(),
        0xFF1C1C1E.toInt(),
        0xFF121212.toInt(),
        0xFF323232.toInt(),
        0xFF383838.toInt(),
        0xFF292929.toInt(),
        0xFF181818.toInt(),
        0xFF343332.toInt(),
        0xFF060606.toInt()
    )

    private val handleColor = 0xFF49494B.toInt()

    fun applyOledTheme(activity: Activity) {
        if (isOledModeEnabled()) {
            activity.setTheme(R.style.AppTheme_OLED)
        }
    }

    fun applyOledColors(window: Window, rootView: View?) {
        if (!isOledModeEnabled()) return
        
        window.statusBarColor = OLED_BLACK
        window.navigationBarColor = OLED_BLACK
        window.decorView.setBackgroundColor(OLED_BLACK)
        rootView?.let { applyOledToViewTree(it) }
    }

    fun applyOledToViewTree(view: View) {
        if (!isOledModeEnabled()) return

        val background = view.background?.mutate()
        when (background) {
            is ColorDrawable -> {
                if (background.color in darkGrayColors) {
                    view.setBackgroundColor(OLED_BLACK)
                }
            }
            is GradientDrawable -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    background.color?.defaultColor?.let { gradientColor ->
                        when (gradientColor) {
                            handleColor -> background.setColor(OLED_HANDLE)
                            in darkGrayColors -> background.setColor(OLED_BLACK)
                        }
                    }
                }
            }
            is LayerDrawable -> {
                for (i in 0 until background.numberOfLayers) {
                    val layer = background.getDrawable(i)?.mutate()
                    if (layer is GradientDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        layer.color?.defaultColor?.let { layerColor ->
                            when (layerColor) {
                                handleColor -> layer.setColor(OLED_HANDLE)
                                in darkGrayColors -> layer.setColor(OLED_BLACK)
                            }
                        }
                    }
                }
            }
        }

        if (view is CardView) {
            val cardColor = view.cardBackgroundColor.defaultColor
            if (cardColor in darkGrayColors) {
                view.setCardBackgroundColor(OLED_CARD)
            }
        }

        if (view is FloatingActionButton) {
            val fabColor = view.backgroundTintList?.defaultColor ?: 0
            if (fabColor in darkGrayColors) {
                view.backgroundTintList = ColorStateList.valueOf(OLED_CARD)
            }
        }

        if (view is Toolbar) {
            view.setBackgroundColor(OLED_BLACK)
        }

        if (view is AppBarLayout) {
            view.setBackgroundColor(OLED_BLACK)
        }

        if (view is TabLayout) {
            view.setBackgroundColor(OLED_BLACK)
        }

        view.backgroundTintList?.let { tintList ->
            val tintColor = tintList.defaultColor
            if (tintColor in darkGrayColors) {
                view.backgroundTintList = ColorStateList.valueOf(OLED_CARD)
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyOledToViewTree(view.getChildAt(i))
            }
        }
    }

    fun getBackgroundColor(): Int {
        return if (isOledModeEnabled()) OLED_BLACK else 0
    }

    fun getCardColor(): Int {
        return if (isOledModeEnabled()) OLED_CARD else 0
    }

    fun isOledModeEnabled(): Boolean {
        return try {
            val settings = IVPNApplication.appComponent.provideSettings()
            settings?.nightMode?.isOledBlack == true
        } catch (e: Exception) {
            false
        }
    }
}

