package net.ivpn.core

import android.app.Application
import android.os.Build
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import net.ivpn.core.common.FeatureConfig
import net.ivpn.core.common.dagger.ApplicationComponent
import net.ivpn.core.common.dagger.DaggerApplicationComponent
import net.ivpn.core.common.logger.CrashLoggingController
import net.ivpn.core.common.navigation.CustomNavigation
import net.ivpn.core.v2.signup.SignUpController
import net.ivpn.core.v2.updates.UpdatesController

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

object IVPNApplication {

    lateinit var appComponent: ApplicationComponent
    lateinit var application: Application
    lateinit var signUpController: SignUpController
    lateinit var updatesController: UpdatesController
    lateinit var customNavigation: CustomNavigation
    lateinit var moduleNavGraph: NavGraph
    lateinit var crashLoggingController: CrashLoggingController
    lateinit var config: FeatureConfig

    fun initBy(application: Application): ApplicationComponent{
        this.application = application
        appComponent = DaggerApplicationComponent.factory().create(application)

        return appComponent
    }

    fun initBaseComponents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appComponent.provideNotificationUtil()?.createNotificationChannels()
        }
        appComponent.provideComponentUtil()?.performBaseComponentsInit()
    }

    fun applySignUpController(controller: SignUpController) {
        this.signUpController = controller
    }

    fun applyUpdatesController(controller: UpdatesController) {
        updatesController = controller
    }

    fun applyCrashController(crashController: CrashLoggingController) {
        this.crashLoggingController = crashController
    }

    fun applyFeatureConfig(config: FeatureConfig) {
        this.config = config
    }

    fun initNavigationWith(navigation: CustomNavigation) {
        this.customNavigation = navigation
    }

    fun expandModuleNavigationController(navigationController: NavController) {
        customNavigation.expandNavigation(application, navigationController)?.also {
            moduleNavGraph = it
        }
    }

}