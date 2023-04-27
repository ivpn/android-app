package net.ivpn.core.v2.protocol

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

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.SnackbarUtil
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.databinding.FragmentProtocolBinding
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.viewmodel.ProtocolViewModel
import net.ivpn.core.v2.protocol.port.PortAdapter
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.viewmodel.MultiHopViewModel
import net.ivpn.core.common.multihop.MultiHopController;
import net.ivpn.core.rest.data.model.Port
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ProtocolFragment @Inject constructor(
        val settings: Settings
) : Fragment(), ProtocolNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ProtocolFragment::class.java)
    }

    lateinit var binding: FragmentProtocolBinding

    @Inject
    lateinit var viewModel: ProtocolViewModel
    @Inject
    lateinit var multiHopController: MultiHopController

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_protocol, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onStart() {
        super.onStart()

        viewModel.setNavigator(this)
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
            }
        }
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel
        val openVpnPortAdapter = PortAdapter(context, R.layout.port_item, settings.openVpnPorts, multiHopController)
        val openMultihopVpnPortAdapter = PortAdapter(context, R.layout.port_item, Port.valuesForMultiHop, multiHopController)
        binding.contentLayout.openvpnProtocolSettings.openvpnSpinner.adapter = openVpnPortAdapter
        binding.contentLayout.openvpnProtocolSettings.openvpnMultihopSpinner.adapter = openMultihopVpnPortAdapter
        val wgVpnPortAdapter = PortAdapter(context, R.layout.port_item, settings.wireGuardPorts, multiHopController)
        binding.contentLayout.wgProtocolSettings.wgSpinner.adapter = wgVpnPortAdapter

        binding.contentLayout.wgProtocolSettings.wireguardDetails.setOnClickListener {
            openWireGuardDetails()
        }
        binding.contentLayout.protocolSelection.comparisonText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openWireGuardDetails() {
        val action = ProtocolFragmentDirections.actionProtocolFragmentToWireGuardDetailsFragment()
        navigate(action)
    }

    override fun notifyUser(msgId: Int, actionId: Int, listener: View.OnClickListener?) {
        SnackbarUtil.show(binding.coordinator, msgId, actionId, listener)
    }

    override fun openDialogueError(dialog: Dialogs?) {
        DialogBuilder.createNotificationDialog(context, dialog)
    }

    override fun openCustomDialogueError(title: String?, message: String?) {
        DialogBuilder.createFullCustomNotificationDialog(context, title, message)
    }

    override fun openNotifyDialogue(dialog: Dialogs?) {
        DialogBuilder.createNotificationDialog(requireContext(), dialog)
    }
}