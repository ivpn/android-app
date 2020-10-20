package net.ivpn.client.common.bindings

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import net.ivpn.client.vpn.model.NetworkState

@BindingAdapter("background")
fun setBackground(view: TextView, backgroundId: Int) {
    view.setBackgroundResource(backgroundId)
}

@BindingAdapter("android:text")
fun setText(view: TextView, state: NetworkState?) {
    state?.let {
        view.setText(it.textRes)
    }
}

@BindingAdapter("isBold")
fun setBold(view: TextView, isBold: Boolean) {
    view.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
}

@BindingAdapter("html")
fun setHtml(view: TextView, html: String) {
    view.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY))
}