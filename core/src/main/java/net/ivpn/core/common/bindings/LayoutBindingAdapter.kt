package net.ivpn.core.common.bindings

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

import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import net.ivpn.core.R
import net.ivpn.core.v2.login.LoginViewModel.InputState
import net.ivpn.core.vpn.model.NetworkState

@BindingAdapter("currentState", "defaultState")
fun setBackgroundColor(view: LinearLayout?, currentState: NetworkState?, defaultState: NetworkState?) {
}

@BindingAdapter("input_state")
fun setInputState(view: FrameLayout, state: InputState?) {
    when (state) {
        InputState.NORMAL -> {
            view.setBackgroundResource(R.color.login_upper_background)
        }
        InputState.FOCUSED -> {
            view.setBackgroundResource(R.drawable.input_field_focused_background)
        }
        InputState.ERROR -> {
            view.setBackgroundResource(R.drawable.input_field_error_background)
        }
        else -> {}
    }
}
