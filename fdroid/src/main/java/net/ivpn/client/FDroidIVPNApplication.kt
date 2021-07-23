package net.ivpn.client

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

import androidx.multidex.MultiDexApplication
import net.ivpn.client.dagger.DaggerFDroidComponent
import net.ivpn.client.dagger.FDroidComponent
import net.ivpn.client.logging.StubCrashLoggingController
import net.ivpn.client.navigation.FDroidNavigation
import net.ivpn.client.signup.FDroidSignUpViewModel
import net.ivpn.client.updates.StubUpdatesViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationComponent
import javax.inject.Inject

class FDroidIVPNApplication: MultiDexApplication() {

    companion object {
        lateinit var instance: FDroidIVPNApplication
    }

    lateinit var fdroidComponent: FDroidComponent

    @Inject
    lateinit var signup: FDroidSignUpViewModel

    var crashController = StubCrashLoggingController()

    var updates = StubUpdatesViewModel()

    override fun onCreate() {
        super.onCreate()
        instance = this
        val appComponent = IVPNApplication.initBy(this)
        initFeatureConfig()
        initComponents(appComponent)
        initUpdatesController()
        //Init crash controller at first
        initCrashLogging()
        IVPNApplication.initBaseComponents()
        initSignUpController()
        IVPNApplication.customNavigation = FDroidNavigation()
    }

    private fun initComponents(appComponent: ApplicationComponent) {
        fdroidComponent = DaggerFDroidComponent.factory().create(appComponent)
        fdroidComponent.inject(this)
    }

    private fun initSignUpController() {
        IVPNApplication.applySignUpController(signup)
    }

    private fun initUpdatesController() {
        IVPNApplication.applyUpdatesController(updates)
    }

    private fun initFeatureConfig() {
        IVPNApplication.applyFeatureConfig(FDroidFeatureConfig())
    }

    private fun initCrashLogging() {
        crashController.init()
        IVPNApplication.crashLoggingController = crashController
    }
}