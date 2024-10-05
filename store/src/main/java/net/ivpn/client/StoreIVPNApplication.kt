package net.ivpn.client

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2021 IVPN Limited.

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
import net.ivpn.client.config.StoreFeatureConfig
import net.ivpn.client.dagger.BillingComponent
import net.ivpn.client.dagger.DaggerBillingComponent
import net.ivpn.client.navigation.StoreNavigation
import net.ivpn.client.signup.SignUpViewModel
import net.ivpn.client.updates.UpdatesStubViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.dagger.ApplicationComponent
import javax.inject.Inject

class StoreIVPNApplication: MultiDexApplication() {

    companion object {
        lateinit var instance: StoreIVPNApplication
    }

    lateinit var billingComponent: BillingComponent

    @Inject
    lateinit var viewModel: SignUpViewModel

    var updates = UpdatesStubViewModel()

    override fun onCreate() {
        super.onCreate()
        instance = this
        val appComponent = IVPNApplication.initBy(this)
        initFeatureConfig()
        initComponents(appComponent)
        initUpdatesController()
        IVPNApplication.initBaseComponents()
        initSignUpController()
        IVPNApplication.customNavigation = StoreNavigation
    }

    private fun initComponents(appComponent: ApplicationComponent) {
        billingComponent = DaggerBillingComponent.factory().create(appComponent)
        billingComponent.inject(this)
    }

    private fun initSignUpController() {
        IVPNApplication.applySignUpController(viewModel)
    }

    private fun initUpdatesController() {
        IVPNApplication.applyUpdatesController(updates)
    }

    private fun initFeatureConfig() {
        IVPNApplication.applyFeatureConfig(StoreFeatureConfig())
    }
}