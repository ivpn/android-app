package net.ivpn.client.dagger

import dagger.Component
import net.ivpn.client.StoreIVPNApplication
import net.ivpn.client.billing.BillingActivity
import net.ivpn.core.common.dagger.ApplicationComponent
import net.ivpn.client.signup.SignUpAccountCreatedFragment
import net.ivpn.client.signup.SignUpPeriodFragment
import net.ivpn.client.signup.SignUpProductFragment

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

@BillingScope
@Component(dependencies = [ApplicationComponent::class])
interface BillingComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: ApplicationComponent): BillingComponent
    }

    fun inject(activity: BillingActivity)

    fun inject(app: StoreIVPNApplication)

    fun inject(fragment: SignUpAccountCreatedFragment)

    fun inject(fragment: SignUpPeriodFragment)

    fun inject(fragment: SignUpProductFragment)
}