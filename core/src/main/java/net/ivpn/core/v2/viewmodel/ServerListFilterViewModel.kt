package net.ivpn.core.v2.viewmodel

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
import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.v2.serverlist.dialog.Filters
import javax.inject.Inject

@ApplicationScope
class ServerListFilterViewModel @Inject constructor(
        private val settings: Settings
) : ViewModel() {

    var filterListener = RadioGroup.OnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
        onCheckedChanged(checkedId)
    }

    val filter = ObservableField<Filters>()

    private val selectedFilter = ObservableField<Filters>()

    var listeners = ArrayList<OnFilterChangedListener>()

    fun onResume() {
        filter.set(settings.filter)
        selectedFilter.set(settings.filter)
    }

    fun reset() {
        filter.set(settings.filter)
        selectedFilter.set(settings.filter)
    }

    fun applyMode() {
        filter.set(selectedFilter.get())
        settings.filter = filter.get()
        listeners.forEach { it.onFilterChanged(filter.get()) }
    }

    private fun onCheckedChanged(checkedId: Int) {
        val filter = Filters.getById(checkedId)
        if (filter == this.selectedFilter.get()) {
            return
        }

        this.selectedFilter.set(filter)
    }

    interface OnFilterChangedListener {
        fun onFilterChanged(filter: Filters?)
    }
}