package net.ivpn.client.vpn.local

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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.ivpn.client.ui.dialog.DialogBuilder.createNotificationDialog
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.vpn.ServiceConstants

class KillSwitchPermissionActivity: AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkVpnPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            finish()
            return
        }
        if (requestCode == REQUEST_CODE) {
            startKillSwitch()
        }
        finish()
    }

    private fun startKillSwitch() {
        val killSwitchIntent = Intent(this, KillSwitchService::class.java)
        killSwitchIntent.action = ServiceConstants.START_KILL_SWITCH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(killSwitchIntent)
        } else {
            startService(killSwitchIntent)
        }
    }

    private fun checkVpnPermission() {
        val intent: Intent?
        intent = try {
            VpnService.prepare(this)
        } catch (exception: Exception) {
            exception.printStackTrace()
            createNotificationDialog(this, Dialogs.FIRMWARE_ERROR)
            return
        }
        if (intent != null) {
            try {
                startActivityForResult(intent, REQUEST_CODE)
            } catch (ane: ActivityNotFoundException) {
            }
        } else {
            onActivityResult(REQUEST_CODE, RESULT_OK, null)
        }
    }
}