package net.ivpn.core.rest.data.session;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2024 IVPN Limited.

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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class SessionNewErrorData {

    @SerializedName("current_plan")
    @Expose
    var currentPlan: String = ""

    @SerializedName("device_management")
    @Expose
    var deviceManagement: Boolean = false

    @SerializedName("device_management_url")
    @Expose
    var deviceManagementUrl: String = ""

    @SerializedName("limit")
    @Expose
    var limit: Int = 0

    @SerializedName("payment_method")
    @Expose
    var paymentMethod: String = ""

    @SerializedName("upgradable")
    @Expose
    var upgradable: Boolean = false

    @SerializedName("upgrade_to_plan")
    @Expose
    var upgradeToPlan: String = ""

    @SerializedName("upgrade_to_url")
    @Expose
    var upgradeToUrl: String = ""

    override fun toString(): String {
        return "SessionNewErrorData(currentPlan='$currentPlan', deviceManagement=$deviceManagement, deviceManagementUrl='$deviceManagementUrl', limit=$limit, paymentMethod='$paymentMethod', upgradable=$upgradable, upgradeToPlan='$upgradeToPlan', upgradeToUrl='$upgradeToUrl')"
    }

}
