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
import net.ivpn.client.dagger.DaggerSiteComponent
import net.ivpn.client.dagger.SiteComponent
import net.ivpn.client.navigation.SiteNavigation
import net.ivpn.client.sentry.SentryController
import net.ivpn.client.signup.SiteSignUpViewModel
import net.ivpn.client.updates.UpdatesViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationComponent
import javax.inject.Inject

class SiteIVPNApplication: MultiDexApplication() {

    companion object {
        lateinit var instance: SiteIVPNApplication
    }

    lateinit var siteComponent: SiteComponent

    @Inject
    lateinit var viewModel: SiteSignUpViewModel

    @Inject
    lateinit var sentry: SentryController

    @Inject
    lateinit var updates: UpdatesViewModel

    override fun onCreate() {
        super.onCreate()
        instance = this
        val appComponent = IVPNApplication.initBy(this)
        initFeatureConfig()
        initComponents(appComponent)
        //Init crash controller at first
        initCrashLogging()
        initUpdatesController()
        IVPNApplication.initBaseComponents()
        initSignUpController()
        IVPNApplication.customNavigation = SiteNavigation()
    }

    private fun initComponents(appComponent: ApplicationComponent) {
        siteComponent = DaggerSiteComponent.factory().create(appComponent)
        siteComponent.inject(this)
    }

    private fun initSignUpController() {
        IVPNApplication.applySignUpController(viewModel)
    }

    private fun initUpdatesController() {
        IVPNApplication.applyUpdatesController(updates)
    }

    private fun initFeatureConfig() {
        IVPNApplication.applyFeatureConfig(SiteFeatureConfig())
    }

    private fun initCrashLogging() {
        sentry.init()
        IVPNApplication.crashLoggingController = sentry
    }
}