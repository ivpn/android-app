package net.ivpn.core.common.extension

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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.VpnService
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs

private fun Fragment.openErrorDialog(dialogs: Dialogs) {
    DialogBuilder.createNotificationDialog(context, dialogs)
}

fun Fragment.checkVPNPermission(requestCode: Int) {
    val intent: Intent?
    intent = try {
        VpnService.prepare(context)
    } catch (exception: Exception) {
        exception.printStackTrace()
        openErrorDialog(Dialogs.FIRMWARE_ERROR)
        return
    }
    if (intent != null) {
        try {
            startActivityForResult(intent, requestCode)
        } catch (ane: ActivityNotFoundException) {
        }
    } else {
        onActivityResult(requestCode, Activity.RESULT_OK, null)
    }
}

fun Fragment.navigate(destination: NavDirections) {
    with(findNavControllerSafely()) {
        this?.currentDestination?.getAction(destination.actionId)
                ?.let { navigate(destination) }
    }
}

var timeGap = 300
var lastAccessTime = 0L
fun Fragment.findNavControllerSafely(): NavController? {
    val accessTime = System.currentTimeMillis()
    return if (isAdded && (accessTime - lastAccessTime > timeGap)) {
        lastAccessTime = accessTime
        findNavController()
    } else {
        null
    }
}

fun Window.getSoftInputMode(): Int {
    return attributes.softInputMode
}

fun Fragment.getNavigationResultBoolean(key: String = "result") =
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(key)

fun Fragment.setNavigationResultBoolean(result: Boolean, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}