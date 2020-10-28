package net.ivpn.client.common.bindings

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


import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.v2.serverlist.dialog.Filters
import net.ivpn.client.vpn.model.NetworkState

@BindingAdapter("checked")
fun setMode(view: RadioGroup, nightMode: NightMode) {
    view.check(nightMode.id)
}

@BindingAdapter("listener")
fun setListener(view: RadioGroup, listener: RadioGroup.OnCheckedChangeListener) {
    view.setOnCheckedChangeListener(listener)
}

@BindingAdapter("checked")
fun setNetworkState(view: RadioGroup, networkState: NetworkState) {
    view.check(networkState.id)
}

@BindingAdapter("checked")
fun setFilter(view: RadioGroup, filter: Filters) {
    view.check(filter.id)
}