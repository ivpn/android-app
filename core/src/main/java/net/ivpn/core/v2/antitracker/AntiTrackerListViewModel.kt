package net.ivpn.core.v2.antitracker

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
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

import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.AntiTracker
import javax.inject.Inject

@ApplicationScope
class AntiTrackerListViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {

    private val basicList = arrayOf("Basic", "Comprehensive", "Restrictive")

    val antiTrackerBasicList: List<AntiTracker>
        get() = settings.antiTrackerList.filter { basicList.contains(it.name) }

    val antiTrackerIndividualList: List<AntiTracker>
        get() = settings.antiTrackerList.filter { !basicList.contains(it.name) }

    val antiTracker: AntiTracker?
        get() =  settings.antiTracker

    fun setAntiTracker(dns: AntiTracker) {
        settings.antiTracker = dns
    }

}