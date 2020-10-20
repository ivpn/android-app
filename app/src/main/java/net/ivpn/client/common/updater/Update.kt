package net.ivpn.client.common.updater

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

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Update {
    @SerializedName("latestVersion")
    @Expose
    var latestVersion: String? = null
    @SerializedName("latestVersionCode")
    @Expose
    var latestVersionCode: Int? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("releaseNotes")
    @Expose
    var releaseNotes: List<String>? = null
}