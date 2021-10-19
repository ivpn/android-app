package net.ivpn.core.v2.viewmodel

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

import net.ivpn.core.IVPNApplication
import net.ivpn.core.v2.network.NetworkViewModel
import net.ivpn.core.v2.signup.SignUpController
import net.ivpn.core.v2.updates.UpdatesController
import javax.inject.Inject

class ViewModelCleaner {

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    @Inject
    lateinit var alwaysOnVPN: AlwaysOnVPNViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    @Inject
    lateinit var killswitch: KillSwitchViewModel

    @Inject
    lateinit var location: LocationViewModel

    @Inject
    lateinit var logging: LoggingViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var filter: ServerListFilterViewModel

    @Inject
    lateinit var serverList: ServerListViewModel

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var startOnBoot: StartOnBootViewModel

    @Inject
    lateinit var protocol: ProtocolViewModel

    @Inject
    lateinit var network: NetworkViewModel

    var signUp: SignUpController = IVPNApplication.signUpController

    init {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)

        account.reset()
        antiTracker.reset()
        alwaysOnVPN.reset()
        connect.reset()
        location.reset()
        logging.reset()
        multihop.reset()
        filter.reset()
        serverList.reset()
        servers.reset()
        startOnBoot.reset()
        protocol.reset()
        signUp.reset()
        network.reset()
    }
}