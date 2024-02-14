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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.R
import net.ivpn.client.StoreIVPNApplication
import net.ivpn.client.databinding.FragmentSignUpProductBinding
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.common.extension.findNavControllerSafely
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.MainActivity
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SignUpProductFragment : Fragment(), SignUpViewModel.SignUpNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SignUpProductFragment::class.java)
    }

    lateinit var binding: FragmentSignUpProductBinding

    @Inject
    lateinit var viewModel: SignUpViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_product, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StoreIVPNApplication.instance.billingComponent.inject(this)
//        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
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

        binding.contentLayout.standardPlanButton.setOnClickListener {
            viewModel.selectedPlan.set(Plan.STANDARD)
            openAddFundAccount()
        }
        binding.contentLayout.proPlanButton.setOnClickListener {
            viewModel.selectedPlan.set(Plan.PRO)
            openAddFundAccount()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openAddFundAccount() {
        val action = SignUpProductFragmentDirections.actionSignUpProductFragmentToSignUpPeriodFragment2()
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onCreateAccountFinish() {
    }

    override fun onAddFundsFinish() {
    }

    override fun onGoogleConnectFailure() {
        if (activity != null) {
            DialogBuilder.createFullCustomNotificationDialog(activity, getString(net.ivpn.core.R.string.dialogs_error),
                    getString(net.ivpn.core.R.string.billing_error_message)) {
                findNavControllerSafely()?.popBackStack()
            }
        }
    }

    override fun createDialog(title: String?, message: String?) {
        if (activity != null) {
            DialogBuilder.createFullCustomNotificationDialog(activity, title,
                message) {
                findNavControllerSafely()?.popBackStack()
            }
        }
    }
}