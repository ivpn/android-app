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

class SessionErrorResponse {
    
    @SerializedName("status")
    @Expose
    var status: Int = 0

    @SerializedName("message")
    @Expose
    var message: String = ""

    @SerializedName("captcha_id")
    @Expose
    val captchaId: String? = null

    @SerializedName("captcha_image")
    @Expose
    val captchaImage: String? = null

    @SerializedName("data")
    @Expose
    var data:SessionErrorData? = null

    override fun toString(): String {
        return "SessionErrorResponse(status=$status, message='$message', captchaId=$captchaId, captchaImage=$captchaImage, data=$data)"
    }

}
