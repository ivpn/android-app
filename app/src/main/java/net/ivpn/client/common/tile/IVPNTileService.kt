package net.ivpn.client.common.tile

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

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.navigation.NavDeepLinkBuilder
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.v2.viewmodel.AccountViewModel
import net.ivpn.client.v2.viewmodel.ConnectionViewModel
import net.ivpn.client.vpn.local.PermissionActivity
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class IVPNTileService: TileService(){

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        account.onResume()
    }

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile
        tile.state = if (connect.isVpnActive()) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }

        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        println()
        if (!account.authenticated.get()) {
            openLoginScreen()
            return
        }

        val tile = qsTile
        if (connect.isVpnActive()) {
            tile.state = Tile.STATE_INACTIVE
            connect.disconnect()
        } else {
            tile.state = Tile.STATE_ACTIVE

            val vpnIntent = Intent(this, PermissionActivity::class.java)
            vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(vpnIntent)
        }

        tile.updateTile()

//        val vpnIntent = Intent(this, PermissionActivity::class.java)
//        vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(vpnIntent)
        // Called when the user click the tile
    }

    private fun openLoginScreen() {
        NavDeepLinkBuilder(IVPNApplication.getApplication())
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.loginFragment).createTaskStackBuilder().startActivities()
        closeSystemDialogs()
    }

    private fun closeSystemDialogs() {
        val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(intent)
    }
}