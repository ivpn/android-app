package net.ivpn.client.signup

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.R
import net.ivpn.client.StoreIVPNApplication
import net.ivpn.client.databinding.FragmentSignUpPeriodBinding
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.extension.findNavControllerSafely
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.MainActivity
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SignUpPeriodFragment : Fragment(), SignUpViewModel.SignUpNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SignUpPeriodFragment::class.java)
    }

    lateinit var binding: FragmentSignUpPeriodBinding

    @Inject
    lateinit var viewModel: SignUpViewModel

    private var isAccountCreated = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_period, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StoreIVPNApplication.instance.billingComponent.inject(this)
        initViews()
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        if (isAccountCreated) {
            NavDeepLinkBuilder(IVPNApplication.application)
                    .setGraph(net.ivpn.core.R.navigation.nav_graph)
                    .setDestination(net.ivpn.core.R.id.accountFragment)
                    .createTaskStackBuilder()
                    .startActivities()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.navigator = this
        viewModel.initOffers()
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
            }
        }
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel

        binding.contentLayout.changeButton.setOnClickListener {
            stepBack()
        }
        binding.contentLayout.continuePurchase.setOnClickListener {
            activity?.let {
                viewModel.purchase(it)
            }
        }

    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun stepBack() {
        findNavControllerSafely()?.popBackStack()
    }

    override fun onCreateAccountFinish() {
        isAccountCreated = true
    }

    override fun onAddFundsFinish() {
        isAccountCreated = true
    }

    override fun onGoogleConnectFailure() {
        if (activity != null) {
            DialogBuilder.createFullCustomNotificationDialog(activity, getString(net.ivpn.core.R.string.dialogs_error),
                    getString(net.ivpn.core.R.string.billing_error_message)) {
                findNavControllerSafely()?.popBackStack()
            }
        }
    }
}