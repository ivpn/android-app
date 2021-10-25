package net.ivpn.core.common.tile

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
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.v2.viewmodel.AccountViewModel
import net.ivpn.core.v2.viewmodel.ConnectionViewModel
import net.ivpn.core.vpn.controller.DefaultVPNStateListener
import net.ivpn.core.vpn.controller.VpnBehaviorController
import net.ivpn.core.vpn.local.PermissionActivity
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class IVPNTileService: TileService() {

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    @Inject
    lateinit var vpnController: VpnBehaviorController

    var isListening = false

    private val vpnStateListener = object: DefaultVPNStateListener() {
        override fun onConnectionStateChanged(state: ConnectionState?) {
            super.onConnectionStateChanged(state)
            if (!isListening) return

            state?.let {
                val tile = qsTile
                when(state) {
                    ConnectionState.NOT_CONNECTED, ConnectionState.PAUSED -> {
                        tile.state = Tile.STATE_INACTIVE
                        tile.updateTile()
                    }
                    ConnectionState.CONNECTED -> {
                        tile.state = Tile.STATE_ACTIVE
                        tile.updateTile()
                    }
                    else -> {
                        //Do nothing in this cases
                    }
                }
            }
        }
    }

    init {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        account.onResume()
    }

    override fun onStartListening() {
        super.onStartListening()
        isListening = true

        val tile = qsTile
        tile.state = if (connect.isVpnActive()) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }

        tile.updateTile()

        vpnController.addVpnStateListener(vpnStateListener)
    }

    override fun onStopListening() {
        super.onStopListening()
        isListening = false
        vpnController.removeVpnStateListener(vpnStateListener)
    }

    override fun onClick() {
        super.onClick()

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
    }

    private fun openLoginScreen() {
        NavDeepLinkBuilder(IVPNApplication.application)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.loginFragment).createTaskStackBuilder().startActivities()
    }
}