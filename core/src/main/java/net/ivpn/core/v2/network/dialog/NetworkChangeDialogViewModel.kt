package net.ivpn.core.v2.network.dialog

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.
 
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
import androidx.databinding.ObservableField
import net.ivpn.core.vpn.model.NetworkState

abstract class NetworkChangeDialogViewModel(
        val currentState: NetworkState
) {
    var selectedState: ObservableField<NetworkState> = ObservableField(currentState)

    var networkStateListener = RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int ->
        onCheckedChanged(checkedId)
    }

    private fun onCheckedChanged(checkedId: Int) {
        val networkState = NetworkState.getById(checkedId)
        if (networkState == this.selectedState.get()) {
            return
        }

        this.selectedState.set(networkState)
    }

    abstract fun apply()

}