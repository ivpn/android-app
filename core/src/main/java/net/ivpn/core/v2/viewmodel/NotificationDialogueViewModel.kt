package net.ivpn.core.v2.viewmodel

import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import javax.inject.Inject

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 Privatus Limited.

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

@ApplicationScope
class NotificationDialogueViewModel @Inject constructor(
    val settings: Settings
){

    //We are reusing the Kill Switch field in the persistent storage to show notifications only for users who were using this feature.
    var isKillSwitchDialogueNeedToBeShown: Boolean
        set(value) {
            settings.isKillSwitchEnabled = value
        }
        get() {
            return settings.isKillSwitchEnabled
        }

}