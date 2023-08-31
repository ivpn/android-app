package net.ivpn.core.v2.protocol.wireguard

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

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
import net.ivpn.core.common.utils.ToastUtil
import net.ivpn.core.databinding.FragmentWireguardDetailsBinding
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.protocol.ProtocolNavigator
import net.ivpn.core.v2.viewmodel.ProtocolViewModel
import net.ivpn.core.v2.MainActivity
import org.slf4j.LoggerFactory
import javax.inject.Inject

class WireGuardDetailsFragment: Fragment(), ProtocolNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WireGuardDetailsFragment::class.java)
    }

    lateinit var binding: FragmentWireguardDetailsBinding

    @Inject
    lateinit var viewModel: ProtocolViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wireguard_details, container, false)
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
                it.setContentSecure(true)
            }
        }
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel

        binding.contentLayout.wireguardInfo.clipboardCopy.setOnClickListener {
            copyPublicKeyToClipboard()
        }
        binding.contentLayout.wireguardInfo.ipClipboardCopy.setOnClickListener {
            copyIpAddressToClipboard()
        }
        binding.contentLayout.wireguardInfo.wgQuantumResistanceInfo.setOnClickListener {
            openQuantumResistanceInfo()
        }
        binding.contentLayout.regenerate.setOnClickListener {
            reGenerateKeys()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun reGenerateKeys() {
        LOGGER.info("Regenerating WireGuard keys")
        viewModel.reGenerateKeys()
    }

    private fun copyPublicKeyToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        viewModel.copyWgKeyToClipboard(clipboard)
        ToastUtil.toast(R.string.protocol_wg_public_key_copied)
    }

    private fun copyIpAddressToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        viewModel.copyWgIpToClipboard(clipboard)
        ToastUtil.toast(R.string.protocol_wg_ip_address_copied)
    }

    private fun openQuantumResistanceInfo() {
        DialogBuilder.createNotificationDialog(context, Dialogs.WG_QUANTUM_RESISTANCE_INFO)
    }

    override fun notifyUser(msgId: Int, actionId: Int, listener: View.OnClickListener?) {
        findNavController().popBackStack()
        SnackbarUtil.show(binding.coordinator, msgId, actionId, listener)
    }

    override fun openDialogueError(dialog: Dialogs?) {
        findNavController().popBackStack()
        DialogBuilder.createNotificationDialog(context, dialog)
    }

    override fun openCustomDialogueError(title: String?, message: String?) {
        findNavController().popBackStack()
        DialogBuilder.createFullCustomNotificationDialog(context, title, message)
    }

    override fun openNotifyDialogue(dialog: Dialogs?) {
        DialogBuilder.createNotificationDialog(requireContext(), dialog)
    }
}