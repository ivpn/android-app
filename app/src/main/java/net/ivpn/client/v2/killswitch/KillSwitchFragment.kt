package net.ivpn.client.v2.killswitch

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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.extension.checkVPNPermission
import net.ivpn.client.databinding.FragmentKillswitchBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.v2.MainActivity
import net.ivpn.client.v2.viewmodel.KillSwitchViewModel
import net.ivpn.client.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import javax.inject.Inject

class KillSwitchFragment : Fragment(), KillSwitchViewModel.KillSwitchNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KillSwitchFragment::class.java)
    }

    private lateinit var binding: FragmentKillswitchBinding

    @Inject
    lateinit var killSwitch: KillSwitchViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_killswitch, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initToolbar()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        killSwitch.update()
    }

    override fun onStart() {
        super.onStart()
        killSwitch.navigator = this
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
            }
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun initViews() {
        binding.contentLayout.killSwitch = killSwitch
        binding.contentLayout.toSettings.setOnClickListener {
            openDeviceSettings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            LOGGER.debug("onActivityResult: RESULT_CANCELED")
            return
        }
        LOGGER.debug("onActivityResult: RESULT_OK")

        when (requestCode) {
            ServiceConstants.ENABLE_KILL_SWITCH -> {
                LOGGER.debug("onActivityResult: ENABLE_KILL_SWITCH")
                killSwitch.enable(true)
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun openDeviceSettings() {
        LOGGER.info("Navigate to VPNs list device settings screen")
        try {
            startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
        } catch (exception: ActivityNotFoundException) {
            exception.printStackTrace()
            LOGGER.error("Error while navigating to VPN device settings")
            DialogBuilder.createNotificationDialog(context, Dialogs.ALWAYS_ON_VPN_NOT_SUPPORTED)
        }
    }

    override fun tryEnableKillSwitch(state: Boolean) {
        LOGGER.info("enableKillSwitch = $state")
        if (state) {
            checkVPNPermission(ServiceConstants.ENABLE_KILL_SWITCH)
        } else {
            killSwitch.enable(false)
        }
    }
}