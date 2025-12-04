package net.ivpn.core.v2.appicon

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by IVPN Limited.
 Copyright (c) 2024 IVPN Limited.

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
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.appicon.CustomAppIconData
import net.ivpn.core.common.nightmode.OledModeController
import net.ivpn.core.databinding.FragmentAppIconBinding
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.viewmodel.AppIconViewModel
import javax.inject.Inject

class AppIconFragment : Fragment() {

    private lateinit var binding: FragmentAppIconBinding

    @Inject
    lateinit var viewModel: AppIconViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_icon, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initToolbar()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        updateRadioButtons()
    }

    override fun onStart() {
        super.onStart()
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
        binding.contentLayout.viewModel = viewModel

        binding.contentLayout.iconDefault.setOnClickListener {
            showConfirmationDialog(CustomAppIconData.DEFAULT)
        }

        binding.contentLayout.iconWeather.setOnClickListener {
            showConfirmationDialog(CustomAppIconData.WEATHER)
        }

        binding.contentLayout.iconNotes.setOnClickListener {
            showConfirmationDialog(CustomAppIconData.NOTES)
        }

        binding.contentLayout.iconCalculator.setOnClickListener {
            showConfirmationDialog(CustomAppIconData.CALCULATOR)
        }
    }

    private fun showConfirmationDialog(icon: CustomAppIconData) {
        if (viewModel.isSelected(icon)) {
            return
        }

        AlertDialog.Builder(requireContext(), getDialogStyle())
            .setTitle(R.string.app_icon_change_title)
            .setMessage(R.string.app_icon_change_message)
            .setPositiveButton(R.string.app_icon_change_confirm) { _, _ ->
                viewModel.selectIcon(icon)
                updateRadioButtons()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun updateRadioButtons() {
        val currentIcon = viewModel.currentIcon.get()
        binding.contentLayout.radioDefault.isChecked = currentIcon == CustomAppIconData.DEFAULT
        binding.contentLayout.radioWeather.isChecked = currentIcon == CustomAppIconData.WEATHER
        binding.contentLayout.radioNotes.isChecked = currentIcon == CustomAppIconData.NOTES
        binding.contentLayout.radioCalculator.isChecked = currentIcon == CustomAppIconData.CALCULATOR
    }

    private fun getDialogStyle(): Int {
        return if (OledModeController.isOledModeEnabled()) {
            R.style.AppTheme_AlertDialog_OLED
        } else {
            R.style.AppTheme_AlertDialog
        }
    }
}

