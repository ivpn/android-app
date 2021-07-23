package net.ivpn.client.signup

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
import android.net.Uri
import androidx.navigation.NavController
import net.ivpn.client.dagger.FDroidScope
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.v2.signup.SignUpController
import javax.inject.Inject

@FDroidScope
class FDroidSignUpViewModel  @Inject constructor(): SignUpController() {
    override fun signUp(navController: NavController?) {
        openIVPNSite()
    }

    override fun signUpWith(navController: NavController?, username: String?) {
        openIVPNSite()
    }

    override fun signUpWithInactiveAccount(navController: NavController?, plan: Plan, isAccountNewStyle: Boolean) {
        openIVPNSite()
    }

    override fun reset() {
    }

    override fun isPurchaseAutoRenewing(): Boolean {
        return false
    }

    private fun openIVPNSite() {
        val url = "https://www.ivpn.net/account/login"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        IVPNApplication.application.startActivity(intent)
    }
}